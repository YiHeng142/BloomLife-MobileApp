package com.example.bloomlife.data

import com.example.bloomlife.model.WaterLogInput
import com.example.bloomlife.model.WaterLogRow
import com.example.bloomlife.model.WaterProfileRow
import io.github.jan.supabase.postgrest.from

object WaterRepository {

    private val client = SupabaseClientProvider.client

    // ---- Logs ----
    suspend fun fetchLogs(userId: String): List<WaterLogRow> {
        return client.from("water_logs")
            .select()
            .decodeList<WaterLogRow>()
            .filter { it.user_id == userId }
    }

    suspend fun insertLog(userId: String, amountMl: Int, time: String): WaterLogRow {
        return client.from("water_logs")
            .insert(WaterLogInput(userId, amountMl, time)) { select() }
            .decodeSingle<WaterLogRow>()
    }

    suspend fun updateLog(id: Int, newAmountMl: Int) {
        client.from("water_logs").update({
            set("amount_ml", newAmountMl)
        }) {
            filter { eq("id", id) }
        }
    }

    suspend fun deleteLog(id: Int) {
        client.from("water_logs").delete {
            filter { eq("id", id) }
        }
    }
    // ---- Profile (goal + tree growth) ----
    suspend fun fetchProfile(userId: String): WaterProfileRow {
        return try {
            val results = supabase.from("water_profile")
                .select()
                .decodeList<WaterProfileRow>()
                .filter { it.user_id == userId }

            if (results.isNotEmpty()) {
                results.first()
            } else {
                supabase.from("water_profile")
                    .insert(WaterProfileRow(user_id = userId)) { select() }
                    .decodeSingle<WaterProfileRow>()
            }
        } catch (e: Exception) {
            // Row exists but has bad/null data (or any other decode issue) —
            // return a safe default instead of crashing the app
            WaterProfileRow(user_id = userId)
        }
    }

    suspend fun updateProfile(profile: WaterProfileRow) {
        client.from("water_profile").update({
            set("daily_goal_ml", profile.daily_goal_ml)
            set("tree_stage", profile.tree_stage)
            set("tree_growth_percent", profile.tree_growth_percent)
            set("last_reward_date", profile.last_reward_date)
        }) {
            filter { eq("user_id", profile.user_id) }
        }
    }
}