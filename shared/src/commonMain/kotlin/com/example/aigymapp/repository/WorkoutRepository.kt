package com.example.aigymapp.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wrapper Repository for workout planning operations
 * Combines WorkoutPlannerRepository and SchedulingRepository from shared module
 * Refactored for Kotlin Multiplatform using SQLDelight
 */
class WorkoutRepository(
    private val workoutPlannerRepository: WorkoutPlannerRepository,
    private val schedulingRepository: SchedulingRepository
) {

    /**
     * Save a planned workout to multiple dates
     * @param exerciseId ID of the exercise
     * @param name Name of the exercise
     * @param dateEpochDays List of date epoch days to schedule
     * @param sets Number of sets
     * @param reps Number of reps
     * @param weightKg Weight in kg (optional)
     * @param restSec Rest time in seconds (optional)
     * @param notes Additional notes (optional)
     * @return Number of workouts inserted (dates * 1)
     */
    suspend fun savePlannedWorkout(
        exerciseId: Long,
        name: String,
        dateEpochDays: List<Long>,
        sets: Int,
        reps: Int,
        weightKg: Float?,
        restSec: Int?,
        notes: String?
    ): Int {
        return withContext(Dispatchers.Default) {
            try {
                // 1. Get or create a session for each date and insert the workout planner item
                var insertedCount = 0
                
                for (epochDay in dateEpochDays) {
                    // Get or create session for this date
                    val sessionId = workoutPlannerRepository.getOrCreateSessionForDate(epochDay)
                    
                    // Insert workout planner item
                    val itemId = workoutPlannerRepository.insertItem(
                        sessionId = sessionId,
                        exerciseId = exerciseId,
                        sets = sets.toLong(),
                        reps = reps.toLong(),
                        weightKg = weightKg?.toDouble() ?: 0.0,
                        durationMin = 0L,
                        restSec = restSec?.toLong(),
                        notes = notes
                    )
                    
                    // Schedule this item to the date
                    schedulingRepository.insertScheduledWorkouts(itemId, listOf(epochDay))
                    
                    insertedCount++
                }
                
                insertedCount
            } catch (e: Exception) {
                println("WorkoutRepository Error: Error saving planned workout - ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Get all workout planner items for a specific day
     */
    suspend fun getItemsForDay(dateEpochDay: Long): List<com.example.aigymapp.database.GetItemsForDay> {
        return withContext(Dispatchers.Default) {
            // Use the flow method from WorkoutPlannerRepository
            // For now, we'll return empty list as this needs to be collected from flow in the UI
            // The proper usage would be: workoutPlannerRepository.getItemsForDay(dateEpochDay).collect { ... }
            emptyList()
        }
    }
    
    /**
     * Delete a workout planner item
     */
    suspend fun deleteWorkoutPlannerItem(itemId: Long) {
        withContext(Dispatchers.Default) {
            workoutPlannerRepository.deleteItem(itemId)
        }
    }
}

