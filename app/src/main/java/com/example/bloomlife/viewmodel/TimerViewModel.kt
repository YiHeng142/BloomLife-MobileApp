package com.example.bloomlife.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerMode {
    SIMPLE,
    INTERVAL
}

enum class TimerPhase {
    IDLE,
    WORK,
    REST,
    FINISHED
}

class TimerViewModel : ViewModel() {

    private val _mode = MutableStateFlow(TimerMode.SIMPLE)
    val mode: StateFlow<TimerMode> = _mode.asStateFlow()

    private val _phase = MutableStateFlow(TimerPhase.IDLE)
    val phase: StateFlow<TimerPhase> = _phase.asStateFlow()

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft.asStateFlow()

    private val _currentRound = MutableStateFlow(0)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var totalRounds = 1
    private var workSeconds = 0
    private var restSeconds = 0
    private var timerJob: Job? = null

    fun startSimpleTimer(seconds: Int) {
        _mode.value = TimerMode.SIMPLE
        totalRounds = 1
        workSeconds = seconds
        _currentRound.value = 1
        _phase.value = TimerPhase.WORK
        _secondsLeft.value = seconds
        runCountdown(onPhaseComplete = { _phase.value = TimerPhase.FINISHED })
    }

    fun startIntervalTimer(rounds: Int, work: Int, rest: Int) {
        _mode.value = TimerMode.INTERVAL
        totalRounds = rounds
        workSeconds = work
        restSeconds = rest
        _currentRound.value = 1
        _phase.value = TimerPhase.WORK
        _secondsLeft.value = work
        runCountdown(onPhaseComplete = ::advanceIntervalPhase)
    }

    private fun advanceIntervalPhase() {
        when (_phase.value) {
            TimerPhase.WORK -> {
                // 运动结束，进入休息
                _phase.value = TimerPhase.REST
                _secondsLeft.value = restSeconds
                runCountdown(onPhaseComplete = ::advanceIntervalPhase)
            }
            TimerPhase.REST -> {
                if (_currentRound.value < totalRounds) {
                    // 还有下一轮，回到运动
                    _currentRound.value += 1
                    _phase.value = TimerPhase.WORK
                    _secondsLeft.value = workSeconds
                    runCountdown(onPhaseComplete = ::advanceIntervalPhase)
                } else {
                    // 最后一轮的休息也结束了，全部完成
                    _phase.value = TimerPhase.FINISHED
                }
            }
            else -> Unit
        }
    }

    private fun runCountdown(onPhaseComplete: () -> Unit) {
        timerJob?.cancel()   // 保险起见，先取消前一个还在跑的 job
        timerJob = viewModelScope.launch {
            _isRunning.value = true
            while (_secondsLeft.value > 0) {
                delay(1000)
                _secondsLeft.value = _secondsLeft.value - 1
            }
            _isRunning.value = false
            onPhaseComplete()
        }
    }

    fun pause() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun reset() {
        timerJob?.cancel()
        _isRunning.value = false
        _phase.value = TimerPhase.IDLE
        _secondsLeft.value = 0
        _currentRound.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}