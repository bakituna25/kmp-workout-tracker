package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic ExerciseProgressSummary model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class ExerciseProgressSummary(
    val exerciseId: Long = 0,
    val exerciseName: String = "",
    val planItemId: Long = 0,
    val plannedSets: Int = 0,
    val completedSets: Int = 0,
    val dateEpochDay: Long = 0,
    val totalVolume: Double = 0.0,
    val avgWeight: Double = 0.0,
    val totalReps: Int = 0,
    val duration: Long = 0
)

