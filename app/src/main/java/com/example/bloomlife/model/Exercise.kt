package com.example.bloomlife.model

data class WorkoutPlan(
    val id: Int,
    val name: String,
    val category: String,       // e.g. "Upper Body", "Cardio", "Full Body"
    val description: String,
    val exercises: List<Exercise>
)

data class Exercise(
    val id: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,       // 组间休息秒数，Timer 会用到
    val instructions: String,
    val imageRes: Int           // 对应 res/drawable 里的图片资源 ID
)