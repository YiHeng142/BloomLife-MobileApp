package com.example.bloomlife.model

data class RewardState(
    val goalAchieved: Boolean = false,
    val showCongrats: Boolean = false,
    val canFeedPlant: Boolean = false,
    val plantStage: Int = 1,
    val plantGrowthPercent: Int = 0,
    val lastRewardDate: String = ""
)

