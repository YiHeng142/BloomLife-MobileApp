package com.example.bloomlife.model

import kotlinx.serialization.Serializable

@Serializable
data class WaterLogRow(
    val id: Int = 0,
    val user_id: String,
    val amount_ml: Int,
    val log_time: String
)

@Serializable
data class WaterLogInput(
    val user_id: String,
    val amount_ml: Int,
    val log_time: String
)

@Serializable
data class WaterProfileRow(
    val user_id: String,
    val daily_goal_ml: Int = 2000,
    val tree_stage: Int = 1,
    val tree_growth_percent: Int = 0,
    val last_reward_date: String = ""
)