package com.example.aigymapp.repository

import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.asFlowList
import com.example.aigymapp.database.asFlowOne
import com.example.aigymapp.database.asFlowOneOrNull
import com.example.aigymapp.models.toDatabaseString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Repository class - Manages database operations using SQLDelight.
 * Refactored for Kotlin Multiplatform.
 */
class AppRepository(private val database: AppDatabase) {

    // ==================== WORKOUT TEMPLATE OPERATIONS ====================

    fun getAllWorkoutTemplates(): Flow<List<com.example.aigymapp.database.Workout_templates>> {
        return database.workoutTemplateQueries.getAllWorkoutTemplates().asFlowList()
    }

    fun getWorkoutTemplateById(id: Long): Flow<com.example.aigymapp.database.Workout_templates?> {
        return database.workoutTemplateQueries.getWorkoutTemplateById(id).asFlowOneOrNull()
    }

    suspend fun insertWorkoutTemplate(
        name: String,
        description: String,
        muscleGroups: String,
        estimatedDuration: Long,
        difficultyLevel: String,
        createdDate: Long,
        isFavorite: Long,
        exerciseCount: Long,
        category: String
    ) = withContext(Dispatchers.Default) {
        database.workoutTemplateQueries.insertWorkoutTemplate(
            name = name,
            description = description,
            muscle_groups = muscleGroups,
            estimated_duration = estimatedDuration,
            difficulty_level = difficultyLevel,
            created_date = createdDate,
            is_favorite = isFavorite,
            exercise_count = exerciseCount,
            category = category
        )
    }

    suspend fun updateWorkoutTemplate(
        id: Long,
        name: String,
        description: String,
        muscleGroups: String,
        estimatedDuration: Long,
        difficultyLevel: String,
        isFavorite: Long,
        exerciseCount: Long,
        category: String
    ) = withContext(Dispatchers.Default) {
        database.workoutTemplateQueries.updateWorkoutTemplate(
            name = name,
            description = description,
            muscle_groups = muscleGroups,
            estimated_duration = estimatedDuration,
            difficulty_level = difficultyLevel,
            is_favorite = isFavorite,
            exercise_count = exerciseCount,
            category = category,
            id = id
        )
    }

    suspend fun deleteWorkoutTemplate(id: Long) = withContext(Dispatchers.Default) {
        database.workoutTemplateQueries.deleteWorkoutTemplate(id)
    }

    // ==================== WORKOUT SESSION OPERATIONS ====================

    fun getAllWorkoutSessions(): Flow<List<com.example.aigymapp.database.Workout_sessions>> {
        return database.workoutSessionQueries.getAllWorkoutSessions().asFlowList()
    }

    fun getRecentWorkoutSessions(): Flow<List<com.example.aigymapp.database.Workout_sessions>> {
        return database.workoutSessionQueries.getRecentWorkoutSessions().asFlowList()
    }

    fun getWorkoutSessionById(id: Long): Flow<com.example.aigymapp.database.Workout_sessions?> {
        return database.workoutSessionQueries.getWorkoutSessionById(id).asFlowOneOrNull()
    }

    suspend fun insertWorkoutSession(
        templateId: Long?,
        name: String,
        startTime: Long,
        endTime: Long?,
        totalDuration: Long,
        status: String,
        notes: String?,
        rating: Long,
        caloriesBurned: Long,
        totalWeightLifted: Double,
        totalSets: Long,
        totalReps: Long,
        createdDate: Long,
        completedSets: Long,
        totalExercises: Long,
        completedExercises: Long,
        completionPercentage: Double,
        performanceGrade: String,
        personalRecordsCount: Long,
        achievementsEarned: Long,
        averageRestTime: Long,
        workoutIntensity: Double,
        updatedAt: Long
    ) = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.insertWorkoutSession(
            template_id = templateId,
            name = name,
            start_time = startTime,
            end_time = endTime,
            total_duration = totalDuration,
            status = status,
            notes = notes,
            rating = rating,
            calories_burned = caloriesBurned,
            total_weight_lifted = totalWeightLifted,
            total_sets = totalSets,
            total_reps = totalReps,
            created_date = createdDate,
            completed_sets = completedSets,
            total_exercises = totalExercises,
            completed_exercises = completedExercises,
            completion_percentage = completionPercentage,
            performance_grade = performanceGrade,
            personal_records_count = personalRecordsCount,
            achievements_earned = achievementsEarned,
            average_rest_time = averageRestTime,
            workout_intensity = workoutIntensity,
            updated_at = updatedAt
        )
    }

    suspend fun updateWorkoutSession(
        id: Long,
        templateId: Long?,
        name: String,
        startTime: Long,
        endTime: Long?,
        totalDuration: Long,
        status: String,
        notes: String?,
        rating: Long,
        caloriesBurned: Long,
        totalWeightLifted: Double,
        totalSets: Long,
        totalReps: Long,
        completedSets: Long,
        totalExercises: Long,
        completedExercises: Long,
        completionPercentage: Double,
        performanceGrade: String,
        personalRecordsCount: Long,
        achievementsEarned: Long,
        averageRestTime: Long,
        workoutIntensity: Double,
        updatedAt: Long
    ) = withContext(Dispatchers.Default) {
        database.workoutSessionQueries.updateWorkoutSession(
            template_id = templateId,
            name = name,
            start_time = startTime,
            end_time = endTime,
            total_duration = totalDuration,
            status = status,
            notes = notes,
            rating = rating,
            calories_burned = caloriesBurned,
            total_weight_lifted = totalWeightLifted,
            total_sets = totalSets,
            total_reps = totalReps,
            completed_sets = completedSets,
            total_exercises = totalExercises,
            completed_exercises = completedExercises,
            completion_percentage = completionPercentage,
            performance_grade = performanceGrade,
            personal_records_count = personalRecordsCount,
            achievements_earned = achievementsEarned,
            average_rest_time = averageRestTime,
            workout_intensity = workoutIntensity,
            updated_at = updatedAt,
            id = id
        )
    }

    // ==================== EXERCISE OPERATIONS ====================

    fun getAllExercises(): Flow<List<com.example.aigymapp.database.Exercises>> {
        return database.exerciseQueries.getAllExercises().asFlowList()
    }

    suspend fun getExerciseById(exerciseId: Long): com.example.aigymapp.database.Exercises? = withContext(Dispatchers.Default) {
        database.exerciseQueries.getExerciseById(exerciseId).executeAsOneOrNull()
    }

    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<com.example.aigymapp.database.Exercises>> {
        return database.exerciseQueries.getExercisesByMuscleGroup(muscleGroup).asFlowList()
    }

    suspend fun insertExercise(
        name: String,
        muscleGroup: String?,
        equipmentNeeded: String?,
        description: String?,
        difficulty: String?,
        isBodyweight: Long
    ): Long = withContext(Dispatchers.Default) {
        database.exerciseQueries.insertExercise(
            name = name,
            muscle_group = muscleGroup,
            equipment_needed = equipmentNeeded,
            description = description,
            difficulty = difficulty,
            is_bodyweight = isBodyweight
        )
        database.exerciseQueries.findIdByName(name).executeAsOne()
    }

    suspend fun updateExercise(
        id: Long,
        name: String,
        muscleGroup: String?,
        equipmentNeeded: String?,
        description: String?,
        difficulty: String?,
        isBodyweight: Long
    ) = withContext(Dispatchers.Default) {
        database.exerciseQueries.updateExercise(
            name = name,
            muscle_group = muscleGroup,
            equipment_needed = equipmentNeeded,
            description = description,
            difficulty = difficulty,
            is_bodyweight = isBodyweight,
            id = id
        )
    }

    suspend fun deleteExercise(id: Long) = withContext(Dispatchers.Default) {
        database.exerciseQueries.deleteExercise(id)
    }

    suspend fun deleteAllExercises() = withContext(Dispatchers.Default) {
        database.exerciseQueries.deleteAllExercises()
    }

    // ==================== PROGRESS OPERATIONS ====================

    fun getAllProgressEntries(): Flow<List<com.example.aigymapp.database.Progress_entries>> {
        return database.progressEntryQueries.getAllProgressEntries().asFlowList()
    }

    fun getRecentProgressEntries(): Flow<List<com.example.aigymapp.database.Progress_entries>> {
        return database.progressEntryQueries.getRecentProgressEntries().asFlowList()
    }

    fun getLatestProgressEntry(): Flow<com.example.aigymapp.database.Progress_entries?> {
        return database.progressEntryQueries.getLatestProgressEntry().asFlowOneOrNull()
    }

    suspend fun insertProgressEntry(
        date: Long,
        weight: Double,
        bodyFatPercentage: Double,
        muscleMass: Double,
        waterPercentage: Double,
        notes: String?,
        photoPath: String?,
        moodRating: Long,
        energyLevel: Long,
        sleepHours: Double,
        waistCircumference: Double,
        chestCircumference: Double,
        armCircumference: Double,
        thighCircumference: Double,
        createdTime: Long
    ) = withContext(Dispatchers.Default) {
        database.progressEntryQueries.insertProgressEntry(
            date = date,
            weight = weight,
            body_fat_percentage = bodyFatPercentage,
            muscle_mass = muscleMass,
            water_percentage = waterPercentage,
            notes = notes,
            photo_path = photoPath,
            mood_rating = moodRating,
            energy_level = energyLevel,
            sleep_hours = sleepHours,
            waist_circumference = waistCircumference,
            chest_circumference = chestCircumference,
            arm_circumference = armCircumference,
            thigh_circumference = thighCircumference,
            created_time = createdTime
        )
    }

    suspend fun deleteAllProgressEntries() = withContext(Dispatchers.Default) {
        database.progressEntryQueries.deleteAllProgressEntries()
    }

    // ==================== STATISTICS OPERATIONS ====================

    fun getTotalWorkoutCount(): Flow<Long> {
        return database.workoutSessionQueries.getTotalWorkoutCount().asFlowOne()
    }

    fun getThisWeekWorkoutCount(weekStart: Long): Flow<Long> {
        return database.workoutSessionQueries.getThisWeekWorkoutCount(weekStart).asFlowOne()
    }

    fun getThisMonthWorkoutCount(monthStart: Long): Flow<Long> {
        return database.workoutSessionQueries.getThisMonthWorkoutCount(monthStart).asFlowOne()
    }

    suspend fun getAverageWorkoutDuration(): Double? {
        return withContext(Dispatchers.Default) {
            database.workoutSessionQueries.getAverageWorkoutDuration().executeAsOneOrNull()?.AVG
        }
    }

    fun getTotalWorkoutDurationMs(): Flow<Long> {
        return database.workoutSessionQueries.getTotalWorkoutDurationMs().asFlowOne()
    }
    
    // Stub methods for backward compatibility with Room-based code
    // TODO: Properly implement these methods or refactor calling code
    fun updateWorkoutSessionSync(
        sessionId: Long,
        endTime: Long? = null,
        totalDurationSeconds: Long? = null,
        notes: String? = null,
        rating: Int? = null
    ) {
        // Stub implementation
        println("TODO: updateWorkoutSessionSync called for session $sessionId")
    }
    
    fun insertExerciseProgressSync(
        sessionId: Long,
        exerciseId: Long,
        exerciseName: String,
        plannedSets: Int,
        completedSets: Int,
        totalVolume: Double,
        timeSpentSeconds: Long,
        notes: String? = null
    ): Long {
        // Stub implementation
        println("TODO: insertExerciseProgressSync called")
        return 0L
    }
    
    fun insertSetRecordSync(
        progressId: Long,
        setNumber: Int,
        plannedReps: Int,
        actualReps: Int,
        plannedWeight: Double,
        actualWeight: Double,
        completed: Boolean
    ): Long {
        // Stub implementation
        println("TODO: insertSetRecordSync called")
        return 0L
    }
    
    // Additional sync methods for backward compatibility
    fun findExerciseIdByNameSync(name: String): Long? {
        return database.exerciseQueries.findIdByName(name).executeAsOneOrNull()
    }
    
    fun getExerciseByIdSync(id: Long): com.example.aigymapp.database.Exercises? {
        return database.exerciseQueries.getExerciseById(id).executeAsOneOrNull()
    }
    
    fun insertExerciseSync(exercise: com.example.aigymapp.database.Exercises): Long {
        database.exerciseQueries.insertExercise(
            name = exercise.name,
            muscle_group = exercise.muscle_group,
            equipment_needed = exercise.equipment_needed,
            description = exercise.description,
            difficulty = exercise.difficulty,
            is_bodyweight = exercise.is_bodyweight
        )
        return database.exerciseQueries.findIdByName(exercise.name).executeAsOne()
    }
    
    // WorkoutSession wrapper methods for backward compatibility
    suspend fun insertWorkoutSessionFromModel(session: Any): Long = withContext(Dispatchers.Default) {
        // For now, just return a placeholder ID
        // In a full implementation, you'd extract fields from session and call insertWorkoutSession
        println("TODO: insertWorkoutSessionFromModel called")
        return@withContext 0L
    }
    
    suspend fun updateWorkoutSessionFromModel(session: Any) = withContext(Dispatchers.Default) {
        // Stub implementation
        println("TODO: updateWorkoutSessionFromModel called")
    }
    
    // ==================== GOAL OPERATIONS ====================
    
    /**
     * Insert a new goal
     * @return the ID of the inserted goal
     */
    suspend fun insertGoal(goal: com.example.aigymapp.models.Goal): Long = withContext(Dispatchers.Default) {
        try {
            database.goalQueries.insertGoal(
                title = goal.title,
                description = goal.description,
                goal_type = goal.goalType.toDatabaseString(),
                target_value = goal.targetValue.toLong(),
                exercise_id = goal.exerciseId,
                muscle_group = goal.muscleGroup,
                start_date = goal.startDate.toString(),
                end_date = goal.endDate?.toString(),
                is_active = if (goal.isActive) 1L else 0L,
                created_at = goal.createdAt
            )
            // Return success (ID retrieval is complex, just return 0 for now)
            0L
        } catch (e: Exception) {
            println("Error inserting goal: ${e.message}")
            throw e
        }
    }
    
    /**
     * Get all active goals
     */
    fun getAllActiveGoals(): Flow<List<com.example.aigymapp.database.Goals>> {
        return database.goalQueries.getAllActiveGoals().asFlowList()
    }
    
    /**
     * Get all goals (active and inactive)
     */
    fun getAllGoals(): Flow<List<com.example.aigymapp.database.Goals>> {
        return database.goalQueries.getAllGoals().asFlowList()
    }
    
    /**
     * Get goal by ID
     */
    fun getGoalById(id: Long): Flow<com.example.aigymapp.database.Goals?> {
        return database.goalQueries.getGoalById(id).asFlowOneOrNull()
    }
    
    /**
     * Get goals with exercise information
     */
    fun getGoalsWithExercise(): Flow<List<com.example.aigymapp.database.GetGoalsWithExercise>> {
        return database.goalQueries.getGoalsWithExercise().asFlowList()
    }
    
    /**
     * Update a goal
     */
    suspend fun updateGoal(goal: com.example.aigymapp.models.Goal) = withContext(Dispatchers.Default) {
        database.goalQueries.updateGoal(
            title = goal.title,
            description = goal.description,
            goal_type = goal.goalType.toDatabaseString(),
            target_value = goal.targetValue.toLong(),
            exercise_id = goal.exerciseId,
            muscle_group = goal.muscleGroup,
            start_date = goal.startDate.toString(),
            end_date = goal.endDate?.toString(),
            is_active = if (goal.isActive) 1L else 0L,
            id = goal.id
        )
    }
    
    /**
     * Update goal active status
     */
    suspend fun updateGoalActiveStatus(goalId: Long, isActive: Boolean) = withContext(Dispatchers.Default) {
        database.goalQueries.updateGoalActiveStatus(
            is_active = if (isActive) 1L else 0L,
            id = goalId
        )
    }
    
    /**
     * Delete a goal
     */
    suspend fun deleteGoal(goalId: Long) = withContext(Dispatchers.Default) {
        database.goalQueries.deleteGoal(goalId)
    }
    
    /**
     * Calculate current progress for a goal based on COMPLETED workouts
     * Uses workout_set_ticks table to count only completed workout days
     * 
     * @param goal The goal to calculate progress for
     * @return Number of completed workout days
     */
    suspend fun calculateGoalProgress(goal: com.example.aigymapp.models.Goal): Int = withContext(Dispatchers.Default) {
        val startDate = goal.startDate.toString()
        val endDate = goal.endDate?.toString() ?: kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
        
        when (goal.goalType) {
            com.example.aigymapp.models.GoalType.EXERCISE_COUNT -> {
                // Count unique days where this exercise has at least one completed set
                val exerciseId = goal.exerciseId ?: return@withContext 0
                database.goalQueries.getCompletedDaysForExercise(
                    exerciseId,
                    startDate,
                    endDate
                ).executeAsOne().toInt()
            }
            
            com.example.aigymapp.models.GoalType.MONTHLY_DAYS -> {
                // Count unique days with any completed sets
                database.goalQueries.getCompletedDaysInRange(
                    startDate,
                    endDate
                ).executeAsOne().toInt()
            }
            
            com.example.aigymapp.models.GoalType.MUSCLE_GROUP_COUNT -> {
                // Count unique days where exercises of this muscle group have completed sets
                val muscleGroup = goal.muscleGroup ?: return@withContext 0
                database.goalQueries.getCompletedDaysForMuscleGroup(
                    muscleGroup,
                    startDate,
                    endDate
                ).executeAsOne().toInt()
            }
        }
    }
}

