package com.example.aigymapp.repository

import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.GetScheduledForDate
import com.example.aigymapp.database.GetScheduledForRange
import com.example.aigymapp.database.asFlowList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for scheduling workouts
 * Refactored for Kotlin Multiplatform with SQLDelight
 */
class SchedulingRepository(
    private val database: AppDatabase,
    private val progressRepository: ProgressRepository? = null
) {
    
    /**
     * Get scheduled workouts for a specific date
     */
    fun getScheduledForDateFlow(epochDay: Long): Flow<List<GetScheduledForDate>> {
        return database.scheduledWorkoutQueries.getScheduledForDate(epochDay).asFlowList()
    }

    /**
     * Get scheduled workouts for a date range
     */
    fun getScheduledForRangeFlow(startDay: Long, endDay: Long): Flow<List<GetScheduledForRange>> {
        return database.scheduledWorkoutQueries.getScheduledForRange(startDay, endDay).asFlowList()
    }

    /**
     * Get scheduled workouts for a specific date (suspend)
     */
    suspend fun getScheduledForDate(epochDay: Long): List<GetScheduledForDate> {
        return withContext(Dispatchers.Default) {
            database.scheduledWorkoutQueries.getScheduledForDate(epochDay).executeAsList()
        }
    }

    /**
     * Get scheduled workouts for a date range (suspend)
     */
    suspend fun getScheduledForRange(startDay: Long, endDay: Long): List<GetScheduledForRange> {
        return withContext(Dispatchers.Default) {
            database.scheduledWorkoutQueries.getScheduledForRange(startDay, endDay).executeAsList()
        }
    }

    /**
     * Schedule workout to multiple dates (sync version for backward compatibility)
     */
    fun scheduleToMultipleDatesSync(planItemId: Long, epochDays: List<Long>) {
        // Ensure the plan item exists before scheduling
        require(database.workoutPlannerItemQueries.getItemById(planItemId).executeAsOneOrNull() != null) {
            "WorkoutPlannerItem with ID $planItemId does not exist"
        }
        
        for (epochDay in epochDays) {
            database.scheduledWorkoutQueries.insertScheduledWorkout(
                plan_item_id = planItemId,
                date_epoch_day = epochDay,
                elapsed_time_ms = 0L
            )
        }
    }
    
    /**
     * Schedule workout to multiple dates
     */
    suspend fun scheduleToMultipleDates(planItemId: Long, epochDays: List<Long>) {
        withContext(Dispatchers.Default) {
            // Delete existing scheduled workouts for this plan item
            database.scheduledWorkoutQueries.deleteScheduledWorkoutsByPlanItemId(planItemId)
            
            // Insert new scheduled workouts
            epochDays.forEach { day ->
                database.scheduledWorkoutQueries.insertScheduledWorkout(
                    plan_item_id = planItemId,
                    date_epoch_day = day,
                    elapsed_time_ms = 0L
                )
            }
        }
    }

    /**
     * Delete scheduled workout by ID
     */
    suspend fun deleteScheduledWorkout(id: Long) {
        withContext(Dispatchers.Default) {
            // Clean ticks for that item's date
            val row = database.scheduledWorkoutQueries.getScheduledById(id).executeAsOneOrNull()
            if (row != null) {
                val dateStr = epochDayToDateString(row.date_epoch_day)
                try {
                    database.workoutSetTickQueries.deleteTicksForItemDate(row.plan_item_id, dateStr)
                } catch (_: Exception) {}
            }
            database.scheduledWorkoutQueries.deleteScheduledWorkoutById(id)
        }
    }

    /**
     * Delete scheduled workout by plan item ID and date
     */
    suspend fun deleteScheduledByPlanAndDate(planItemId: Long, epochDay: Long) {
        withContext(Dispatchers.Default) {
            try {
                val dateStr = epochDayToDateString(epochDay)
                try {
                    database.workoutSetTickQueries.deleteTicksForItemDate(planItemId, dateStr)
                } catch (_: Exception) {}
            } catch (_: Exception) {}
            database.scheduledWorkoutQueries.deleteScheduledByPlanAndDate(planItemId, epochDay)
        }
    }

    /**
     * Delete all scheduled workouts for a specific date
     */
    suspend fun deleteScheduledForDate(epochDay: Long) {
        withContext(Dispatchers.Default) {
            // Delete ticks for all plan items on that day
            val planItemIds = try {
                database.scheduledWorkoutQueries.getPlanItemIdsForDate(epochDay).executeAsList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val dateStr = epochDayToDateString(epochDay)
            if (planItemIds.isNotEmpty()) {
                // Note: SQLDelight doesn't support IN with list parameter easily in multiplatform
                // So we delete one by one
                planItemIds.forEach { planItemId ->
                    try {
                        database.workoutSetTickQueries.deleteTicksForItemDate(planItemId, dateStr)
                    } catch (_: Exception) {}
                }
            }
            database.scheduledWorkoutQueries.deleteScheduledForDate(epochDay)
        }
    }

    /**
     * Get all dates for a specific plan item
     */
    suspend fun getDatesForPlanItem(planItemId: Long): List<Long> {
        return withContext(Dispatchers.Default) {
            database.scheduledWorkoutQueries.getDatesForPlanItem(planItemId).executeAsList()
        }
    }

    /**
     * Insert scheduled workouts
     */
    suspend fun insertScheduledWorkouts(planItemId: Long, epochDays: List<Long>) {
        withContext(Dispatchers.Default) {
            epochDays.forEach { day ->
                database.scheduledWorkoutQueries.insertScheduledWorkout(
                    plan_item_id = planItemId,
                    date_epoch_day = day,
                    elapsed_time_ms = 0L
                )
                
                // Recompute progress for this date
                val dateStr = epochDayToDateString(day)
                progressRepository?.recomputeDay(dateStr)
            }
        }
    }

    /**
     * Delete scheduled workouts by plan item ID
     */
    suspend fun deleteScheduledWorkoutsByPlanItemId(planItemId: Long) {
        withContext(Dispatchers.Default) {
            // Get all dates for this plan item before deleting
            val dates = database.scheduledWorkoutQueries.getDatesForPlanItem(planItemId).executeAsList()
            
            // Delete the scheduled workouts
            database.scheduledWorkoutQueries.deleteScheduledWorkoutsByPlanItemId(planItemId)
            
            // Recompute progress for affected dates
            dates.forEach { epochDay ->
                val dateStr = epochDayToDateString(epochDay)
                progressRepository?.recomputeDay(dateStr)
            }
        }
    }
    
    /**
     * Update elapsed time for a scheduled workout
     */
    suspend fun updateElapsedTime(scheduledWorkoutId: Long, elapsedTimeMs: Long) {
        withContext(Dispatchers.Default) {
            database.scheduledWorkoutQueries.updateElapsedTime(
                elapsed_time_ms = elapsedTimeMs,
                id = scheduledWorkoutId
            )
        }
    }
    
    /**
     * Get total elapsed time for a specific date
     */
    suspend fun getTotalElapsedTimeForDate(epochDay: Long): Long {
        return withContext(Dispatchers.Default) {
            database.scheduledWorkoutQueries.getTotalElapsedTimeForDate(epochDay).executeAsOne()
        }
    }

    /**
     * Persist a new display order for a day's scheduled workouts (drag-to-reorder).
     * `orderedIds` is the full list of scheduled_workout ids in their new top-to-bottom order.
     */
    suspend fun reorderScheduledWorkouts(orderedIds: List<Long>) {
        withContext(Dispatchers.Default) {
            database.transaction {
                orderedIds.forEachIndexed { index, id ->
                    database.scheduledWorkoutQueries.updateSortOrder(sort_order = index.toLong(), id = id)
                }
            }
        }
    }

    

    /**
     * Convert epoch day to date string (yyyy-MM-dd)
     */
    private fun epochDayToDateString(epochDay: Long): String {
        // Convert epoch day to Instant
        val epochMillis = epochDay * 24 * 60 * 60 * 1000
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis)
        // Convert to LocalDate
        val localDate = instant.toEpochMilliseconds().let { millis ->
            kotlinx.datetime.LocalDate.fromEpochDays((millis / (24 * 60 * 60 * 1000)).toInt())
        }
        return localDate.toString() // Returns yyyy-MM-dd format
    }
    
    /**
     * Get the most recent past scheduled workout for a given exercise name (Son Performans)
     */
    suspend fun getLastPerformanceForExercise(exerciseName: String, beforeEpochDay: Long): com.example.aigymapp.database.GetLastPerformanceForExercise? {
        return withContext(Dispatchers.Default) {
            try {
                database.scheduledWorkoutQueries
                    .getLastPerformanceForExercise(exerciseName, beforeEpochDay)
                    .executeAsOneOrNull()
            } catch (_: Exception) { null }
        }
    }

    /**
     * Cleanup orphan scheduled_workouts records
     * Delete scheduled_workouts belonging to deleted workout_planner_items
     */
    suspend fun cleanupOrphanScheduledWorkouts() {
        withContext(Dispatchers.Default) {
            try {
                database.scheduledWorkoutQueries.deleteOrphanScheduledWorkouts()
                println("✅ SchedulingRepository: Orphan scheduled_workouts cleaned")
            } catch (e: Exception) {
                println("❌ SchedulingRepository: Orphan cleanup error: ${e.message}")
                throw e
            }
        }
    }
}

