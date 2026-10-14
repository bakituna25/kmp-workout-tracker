package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic ExerciseProgress model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class ExerciseProgress(
    val id: Long = 0,
    var sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val plannedSets: Long,
    val completedSets: Long,
    val totalVolume: Double,
    val timeSpentSeconds: Long,
    val muscleGroup: String = "",
    val avgReps: Long = 0,
    val plannedWeightKg: Double = 0.0,
    val plannedRestSec: Long = 60,
    val plannedRepsTotal: Long = 0
) {
    // Backward compatibility aliases
    val name: String get() = exerciseName
    val targetSets: Long get() = plannedSets
    val totalSets: Long get() = plannedSets
    val timeSpent: Long get() = timeSpentSeconds
    val isPrAchieved: Boolean get() = false // Placeholder
    val completionPercent: Int get() = if (plannedSets > 0) ((completedSets.toDouble() / plannedSets) * 100).toInt() else 0
    
    // Helper method
    fun getImprovementPercentage(): Double = 0.0 // Placeholder
}

