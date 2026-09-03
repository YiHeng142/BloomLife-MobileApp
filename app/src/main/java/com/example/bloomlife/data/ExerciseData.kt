package com.example.bloomlife.data

import com.example.bloomlife.R
import com.example.bloomlife.model.Exercise
import com.example.bloomlife.model.WorkoutPlan

object ExerciseData {

    val plans = listOf(
        WorkoutPlan(
            id = 1,
            name = "Upper Body Strength",
            category = "Strength",
            description = "Build strength in your chest, back, and arms.",
            exercises = listOf(
                Exercise(
                    id = 1,
                    name = "Push-up",
                    sets = 3,
                    reps = 12,
                    restSeconds = 30,
                    instructions = "Keep your body straight, lower your chest to the floor, then push back up.",
                    imageRes = R.drawable.pushup
                ),
                Exercise(
                    id = 2,
                    name = "Pull-up",
                    sets = 3,
                    reps = 8,
                    restSeconds = 45,
                    instructions = "Grip the bar shoulder-width apart, pull your chin above the bar, then lower slowly.",
                    imageRes = R.drawable.pullup
                )
            )
        ),
        WorkoutPlan(
            id = 2,
            name = "Cardio Blast",
            category = "Cardio",
            description = "Get your heart rate up with these full-body movements.",
            exercises = listOf(
                Exercise(
                    id = 3,
                    name = "Jumping Jacks",
                    sets = 3,
                    reps = 20,
                    restSeconds = 20,
                    instructions = "Jump while spreading your legs and raising your arms overhead, then return.",
                    imageRes = R.drawable.jumpingjacks
                ),
                Exercise(
                    id = 4,
                    name = "High Knees",
                    sets = 3,
                    reps = 20,
                    restSeconds = 20,
                    instructions = "Run in place, driving your knees up toward your chest as high as possible.",
                    imageRes = R.drawable.highknees
                )
            )
        ),
        WorkoutPlan(
            id = 3,
            name = "Full Body Circuit",
            category = "Full Body",
            description = "A balanced mix targeting every major muscle group.",
            exercises = listOf(
                Exercise(
                    id = 5,
                    name = "Squat",
                    sets = 4,
                    reps = 15,
                    restSeconds = 30,
                    instructions = "Feet shoulder-width apart, lower your hips back and down, then stand back up.",
                    imageRes = R.drawable.squat
                ),
                Exercise(
                    id = 6,
                    name = "Plank",
                    sets = 3,
                    reps = 1,
                    restSeconds = 30,
                    instructions = "Hold a straight-body position on your forearms and toes for 30–60 seconds.",
                    imageRes = R.drawable.plank
                )
            )
        )
    )

    fun getPlanById(id: Int): WorkoutPlan? = plans.find { it.id == id }

    fun getExerciseById(exerciseId: Int): Exercise? =
        plans.flatMap { it.exercises }.find { it.id == exerciseId }
}