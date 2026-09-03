package com.example.bloomlife.model

data class WorkoutPlan(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val exercises: List<Exercise>
)

data class Exercise(
    val id: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val instructions: String,
    val imageRes: Int
)