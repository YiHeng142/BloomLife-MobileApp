package com.example.bloomlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloomlife.data.SupabaseClientProvider
import com.example.bloomlife.data.WaterRepository
import com.example.bloomlife.model.WaterProfileRow
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.example.bloomlife.model.WaterEntry

class WaterViewModel : ViewModel() {

    // Real logged-in user id from Supabase Auth (falls back to "guest_user" if somehow null)
    private val userId: String
        get() = SupabaseClientProvider.client.auth.currentUserOrNull()?.id ?: "guest_user"

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ---- Daily goal (now user-editable, minimum 1000ml / 1L) ----
    private val _dailyGoalMl = MutableStateFlow(2000)
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl.asStateFlow()

    private val MIN_GOAL_ML = 1000

    // Returns an error message if invalid, or null if the value is fine

    private val _goalInput = MutableStateFlow("")
    val goalInput: StateFlow<String> = _goalInput.asStateFlow()

    fun onGoalInputChange(value: String) { _goalInput.value = value }

    // Returns an error message if invalid, or null if the value is fine
    fun validateGoalInput(): String? {
        val value = _goalInput.value.toIntOrNull()
        return when {
            value == null -> "Please enter a valid number."
            value < MIN_GOAL_ML -> "Minimum daily goal is 1.0 L (1000 ml)."
            else -> null
        }
    }

    fun updateDailyGoal() {
        val value = _goalInput.value.toIntOrNull() ?: return
        if (value < MIN_GOAL_ML) return   // safety check, in case UI didn't block it
        _dailyGoalMl.value = value
        _goalInput.value = ""
        checkGoalCompletion()   // re-check in case new goal changes achieved status
        saveProfileToDatabase()
    }

    // ---- Total water drunk today ----
    private val _totalMl = MutableStateFlow(0)
    val totalMl: StateFlow<Int> = _totalMl.asStateFlow()

    // ---- Log history (PRIVATE mutable, PUBLIC read-only — same pattern as Practical 7) ----
    private val _logs = MutableStateFlow<List<WaterEntry>>(emptyList())
    val logs: StateFlow<List<WaterEntry>> = _logs.asStateFlow()

    // ---- Custom amount input field (for the "log" dialog) ----
    private val _customAmount = MutableStateFlow("")
    val customAmount: StateFlow<String> = _customAmount.asStateFlow()

    fun onCustomAmountChange(value: String) { _customAmount.value = value }

    private fun currentTimeFormatted(): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return formatter.format(Calendar.getInstance().time)
    }

    init {
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = withContext(Dispatchers.IO) { WaterRepository.fetchProfile(userId) }
                _dailyGoalMl.value = profile.daily_goal_ml
                _treeStage.value = profile.tree_stage
                _treeGrowthPercent.value = profile.tree_growth_percent
                _lastRewardDate.value = profile.last_reward_date

                val rows = withContext(Dispatchers.IO) { WaterRepository.fetchLogs(userId) }
                _logs.value = rows.map { WaterEntry(it.id, it.amount_ml, it.log_time) }
                _totalMl.value = rows.sumOf { it.amount_ml }

                checkGoalCompletion()
            } catch (e: Exception) {
                android.util.Log.e("WaterViewModel", "loadFromDatabase failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Quick add button (+250ml)
    fun addWater(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            val time = currentTimeFormatted()
            try {
                val inserted = withContext(Dispatchers.IO) {
                    WaterRepository.insertLog(userId, amountMl, time)
                }
                val entry = WaterEntry(inserted.id, inserted.amount_ml, inserted.log_time)
                _logs.update { it + entry }
                _totalMl.update { it + amountMl }
                checkGoalCompletion()
            } catch (e: Exception) {
                android.util.Log.e("WaterViewModel", "addWater failed", e)
            }
        }
    }

    // Called from the "log" dialog's Save button
    fun addFromCustomInput() {
        val amount = _customAmount.value.toIntOrNull() ?: return
        addWater(amount)
        _customAmount.value = ""
    }

    fun removeEntry(entry: WaterEntry) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { WaterRepository.deleteLog(entry.id) }
                _logs.update { it - entry }
                _totalMl.update { (it - entry.amountMl).coerceAtLeast(0) }
                checkGoalCompletion()
            } catch (e: Exception) { }
        }
    }

    // ---- UPDATE ----
    fun updateEntry(entry: WaterEntry, newAmountMl: Int) {
        if (newAmountMl <= 0) return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { WaterRepository.updateLog(entry.id, newAmountMl) }
                val oldAmount = entry.amountMl
                _logs.update { current ->
                    current.map { if (it.id == entry.id) it.copy(amountMl = newAmountMl) else it }
                }
                _totalMl.update { (it - oldAmount + newAmountMl).coerceAtLeast(0) }
                checkGoalCompletion()
            } catch (e: Exception) { }
        }
    }

    // ---- REWARD SYSTEM: Plant growth ----

    // True once the user has hit today's goal
    private val _goalAchieved = MutableStateFlow(false)
    val goalAchieved: StateFlow<Boolean> = _goalAchieved.asStateFlow()

    // Controls the congratulation popup
    private val _showCongrats = MutableStateFlow(false)
    val showCongrats: StateFlow<Boolean> = _showCongrats.asStateFlow()

    // True when the reward is ready to be claimed (Feed Tree button appears)
    private val _canFeedTree = MutableStateFlow(false)
    val canFeedTree: StateFlow<Boolean> = _canFeedTree.asStateFlow()

    // Current stage: 1 (Seedling), 2 (Sprout), 3 (Full Tree) — caps at 3
    private val _treeStage = MutableStateFlow(1)
    val treeStage: StateFlow<Int> = _treeStage.asStateFlow()

    // Progress WITHIN the current stage: 0-100%. Resets to 0 when stage advances.
    private val _treeGrowthPercent = MutableStateFlow(0)
    val treeGrowthPercent: StateFlow<Int> = _treeGrowthPercent.asStateFlow()

    private val MAX_STAGE = 3

    // Records which day (yyyy-MM-dd) the reward was last claimed, so it's max once per day
    private val _lastRewardDate = MutableStateFlow("")

    private fun todayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        return formatter.format(Calendar.getInstance().time)
    }

    // Call this after any change to totalMl (add / update / delete)
    private fun checkGoalCompletion() {
        val reachedGoal = _totalMl.value >= _dailyGoalMl.value
        if (reachedGoal && !_goalAchieved.value) {
            _goalAchieved.value = true
            _showCongrats.value = true
            _canFeedTree.value = (_lastRewardDate.value != todayDateString())
        } else if (!reachedGoal && _goalAchieved.value) {
            _goalAchieved.value = false
            _canFeedTree.value = false
        }
    }

    fun dismissCongrats() {
        _showCongrats.value = false }

    fun feedTree() {
        if (!_canFeedTree.value) return
        val today = todayDateString()
        if (_lastRewardDate.value == today) {
            _canFeedTree.value = false
            return
        }
        // Already fully grown (max stage + 100%) — nothing more to gain
        if (_treeStage.value >= MAX_STAGE && _treeGrowthPercent.value >= 100) {
            _lastRewardDate.value = today
            _canFeedTree.value = false
            saveProfileToDatabase()
            return
        }
        val newProgress = _treeGrowthPercent.value + 10
        if (newProgress >= 100) {
            if (_treeStage.value < MAX_STAGE) {
                // Stage complete — advance to next stage's picture, reset progress
                _treeStage.update { it + 1 }
                _treeGrowthPercent.value = 0
            } else {
                // Already at final stage — cap at 100%, don't reset
                _treeGrowthPercent.value = 100
            }
        } else {
            _treeGrowthPercent.value = newProgress
        }
        _lastRewardDate.value = today
        _canFeedTree.value = false
        saveProfileToDatabase()
    }

    private fun saveProfileToDatabase() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    WaterRepository.updateProfile(
                        WaterProfileRow(
                            user_id = userId,
                            daily_goal_ml = _dailyGoalMl.value,
                            tree_stage = _treeStage.value,
                            tree_growth_percent = _treeGrowthPercent.value,
                            last_reward_date = _lastRewardDate.value
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("WaterViewModel", "saveProfileToDatabase failed", e)  // ADD THIS
            }
        }
    }
}