package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic ScheduledWithDetails model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class ScheduledWithDetails(
    val scheduledWorkoutId: Long = 0,
    val planItemId: Long = 0,
    val exerciseId: Long = 0,
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0,
    val restSec: Int? = null,
    val notes: String? = null,
    val muscleGroup: String = "",
    val epochDay: Long = 0,
    var plannedSets: Int = sets,
    var completedSets: Int = 0,
    val dateEpochDay: Long = epochDay,
    val restDuration: Int = restSec ?: 60,
    val setIndex: Long = 0,
    val sessionId: Long = 0,
    val exercise: Exercise? = null,
    val performanceGrade: String = ""
) {
    // Backward compatibility methods
    val id: Long get() = scheduledWorkoutId
    
    fun getScheduledWorkout(): ScheduledWorkoutWrapper = ScheduledWorkoutWrapper(scheduledWorkoutId)
    fun getDisplayName(): String = exerciseName
    fun getName(): String = exerciseName
    fun getSetsRepsDisplay(): String = "$sets × $reps"
    fun getWeightDisplay(): String = if (weightKg > 0) "${weightKg}kg" else "-"
    fun getRestDisplay(): String = if (restSec != null && restSec > 0) "${restSec}s" else "-"
    fun getDurationDisplay(): String = "~${sets * (reps / 5 + (restSec ?: 60) / 60)} min"
    fun getMuscleGroupDisplay(): String = muscleGroup.ifEmpty { "General" }
    fun getMuscleGroupDisplayName(): String = muscleGroup.ifEmpty { "General" }
    fun hasNotes(): Boolean = !notes.isNullOrEmpty()
    fun toDayCardRowList(): List<Any> = emptyList() // Placeholder
}

/**
 * Simple wrapper for scheduled workout ID
 */
@Serializable
data class ScheduledWorkoutWrapper(val id: Long)

