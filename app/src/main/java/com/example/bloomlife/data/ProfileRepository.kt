package com.example.bloomlife.data

import android.content.Context
import com.example.bloomlife.model.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Stores extra profile details (birthdate, height, weight, BMI) locally,
 * keyed by the Supabase Auth user ID. Login/Register/session are handled
 * by Supabase Auth elsewhere — this repository only manages profile data.
 */
class ProfileRepository(private val context: Context) {
    private val gson = Gson()
    private val fileName = "users_data.json"
    private val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private var usersCache: MutableList<UserProfile>? = null

    suspend fun getAllUsers(): List<UserProfile> = withContext(Dispatchers.IO) {
        getMutableUsers()
    }

    private suspend fun getMutableUsers(): MutableList<UserProfile> = withContext(Dispatchers.IO) {
        if (usersCache != null) return@withContext usersCache!!

        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<UserProfile>>() {}.type
            usersCache = gson.fromJson(json, type) ?: mutableListOf()
        } else {
            usersCache = mutableListOf()
        }
        usersCache!!
    }

    private suspend fun saveAllUsers() = withContext(Dispatchers.IO) {
        val json = gson.toJson(usersCache)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    suspend fun saveProfile(profile: UserProfile) {
        val users = getMutableUsers()
        val index = users.indexOfFirst { it.userId == profile.userId }
        if (index != -1) {
            users[index] = profile
        } else {
            users.add(profile)   // first time saving this user's profile
        }
        saveAllUsers()
    }

    suspend fun loadProfile(userId: String): UserProfile? {
        val users = getMutableUsers()
        return users.find { it.userId == userId } ?: UserProfile(userId = userId)
        // returns a blank profile pre-filled with the userId if none exists yet
    }

    suspend fun updateUsername(userId: String, newUsername: String) {
        val users = getMutableUsers()
        val index = users.indexOfFirst { it.userId == userId }
        if (index != -1) {
            users[index] = users[index].copy(username = newUsername)
            saveAllUsers()
        }
    }

    // --- Dark Mode Management ---
    fun setDarkMode(enabled: Boolean) {
        settingsPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return settingsPrefs.getBoolean("dark_mode", false)
    }
}