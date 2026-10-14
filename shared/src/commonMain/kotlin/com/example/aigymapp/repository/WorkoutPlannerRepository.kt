package com.example.aigymapp.repository

import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.asFlowList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for Workout Planner operations
 * Refactored for Kotlin Multiplatform with SQLDelight
 */
class WorkoutPlannerRepository(
    private val database: AppDatabase,
    private val progressRepository: ProgressRepository? = null
) {

    /**
     * Get or create session for a specific date
     */
    suspend fun getOrCreateSessionForDate(dateEpochDay: Long): Long {
        return withContext(Dispatchers.Default) {
            // Check if session already exists for this date
            val existingSession = database.workoutPlannerSessionQueries.getSessionByDate(dateEpochDay).executeAsOneOrNull()
            if (existingSession != null) {
                return@withContext existingSession.id
            }

            // Create new session if doesn't exist
            database.workoutPlannerSessionQueries.insertSession(
                date_epoch_day = dateEpochDay,
                title = "Workout Session",
                created_at = currentTimeMillis(),
                updated_at = currentTimeMillis()
            )
            
            // Return the newly created session ID
            database.workoutPlannerSessionQueries.getSessionByDate(dateEpochDay).executeAsOne().id
        }
    }

    /**
     * Insert a workout planner item
     */
    suspend fun insertItem(
        sessionId: Long,
        exerciseId: Long,
        sets: Long,
        reps: Long,
        weightKg: Double,
        durationMin: Long = 0,
        restSec: Long? = null,
        notes: String? = null
    ): Long {
        return withContext(Dispatchers.Default) {
            database.workoutPlannerItemQueries.insertItem(
                session_id = sessionId,
                exercise_id = exerciseId,
                sets = sets,
                reps = reps,
                weight_kg = weightKg,
                duration_min = durationMin,
                rest_sec = restSec,
                notes = notes,
                created_at = currentTimeMillis()
            )
            
            // Get the last inserted ID
            val itemId = database.workoutPlannerItemQueries.getItemsBySessionId(sessionId)
                .executeAsList()
                .maxByOrNull { it.id }?.id ?: 0L
            
            // Create workout plan sets for this item
            progressRepository?.createPlanSetsForItem(itemId, sets.toInt(), reps.toInt(), weightKg)
            
            itemId
        }
    }

    /**
     * Update a workout planner item
     */
    suspend fun updateItem(
        id: Long,
        sessionId: Long,
        exerciseId: Long,
        sets: Long,
        reps: Long,
        weightKg: Double,
        durationMin: Long,
        restSec: Long?,
        notes: String?
    ) {
        withContext(Dispatchers.Default) {
            database.workoutPlannerItemQueries.updateItem(
                session_id = sessionId,
                exercise_id = exerciseId,
                sets = sets,
                reps = reps,
                weight_kg = weightKg,
                duration_min = durationMin,
                rest_sec = restSec,
                notes = notes,
                id = id
            )
        }
    }

    /**
     * Delete a workout planner item
     */
    suspend fun deleteItem(itemId: Long) {
        withContext(Dispatchers.Default) {
            // Clean up related plan sets and ticks
            progressRepository?.cleanupPlanSetsForItem(itemId)
            
            // Delete the item itself
            database.workoutPlannerItemQueries.deleteItemById(itemId)
        }
    }

    /**
     * Get items for a specific session
     */
    fun getItemsBySessionId(sessionId: Long): Flow<List<com.example.aigymapp.database.Workout_planner_items>> {
        return database.workoutPlannerItemQueries.getItemsBySessionId(sessionId).asFlowList()
    }

    /**
     * Get all workout planner items
     */
    fun getAllItemsFlow(): Flow<List<com.example.aigymapp.database.Workout_planner_items>> {
        return database.workoutPlannerItemQueries.getAllItems().asFlowList()
    }

    /**
     * Get items with exercise details for a specific day
     */
    fun getItemsForDay(dateEpochDay: Long): Flow<List<com.example.aigymapp.database.GetItemsForDay>> {
        return database.workoutPlannerItemQueries.getItemsForDay(dateEpochDay).asFlowList()
    }

    /**
     * Insert exercise if it doesn't exist, return its ID
     */
    suspend fun upsertExercise(
        name: String,
        muscleGroup: String?,
        equipmentNeeded: String?
    ): Long {
        return withContext(Dispatchers.Default) {
            val existingId = database.exerciseQueries.findExerciseIdByName(name).executeAsOneOrNull()
            if (existingId != null) {
                return@withContext existingId
            }

            // Create new exercise
            database.exerciseQueries.insertExercise(
                name = name,
                muscle_group = muscleGroup,
                equipment_needed = equipmentNeeded,
                description = null,
                difficulty = null,
                is_bodyweight = 0
            )
            
            database.exerciseQueries.findExerciseIdByName(name).executeAsOne()
        }
    }

    /**
     * Check if exercise exists
     */
    suspend fun ensureExerciseExists(exerciseId: Long): Long? {
        return withContext(Dispatchers.Default) {
            database.exerciseQueries.ensureExerciseExists(exerciseId).executeAsOneOrNull()
        }
    }

    /**
     * Check if session exists
     */
    suspend fun ensureSessionExists(sessionId: Long): Long? {
        return withContext(Dispatchers.Default) {
            database.workoutPlannerSessionQueries.ensureSessionExists(sessionId).executeAsOneOrNull()
        }
    }

    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: Long): com.example.aigymapp.database.Workout_planner_sessions? {
        return withContext(Dispatchers.Default) {
            database.workoutPlannerSessionQueries.getSessionById(sessionId).executeAsOneOrNull()
        }
    }

    /**
     * Clean up orphan records
     */
    suspend fun cleanupOrphanRecords(): Long {
        return withContext(Dispatchers.Default) {
            var totalDeleted = 0L
            database.workoutPlannerItemQueries.deleteOrphanItemsBySession()
            database.workoutPlannerItemQueries.deleteOrphanItemsByExercise()
            database.scheduledWorkoutQueries.deleteOrphanScheduledWorkouts()
            totalDeleted
        }
    }

    // Platform-agnostic time function
    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}

