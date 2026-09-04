package com.example.bloomlife.model

data class UserProfile(
    val userId: String = "",   // now holds the Supabase Auth user ID (UUID string)
    var username: String = "",
    val email: String = "",

    val birthDate: Long? = null,
    val age: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val bmi: String = "",
    val category: String = ""
)