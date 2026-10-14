package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutPlannerItem model  
 * Migrated to commonMain from androidMain
 */
@Serializable
data class WorkoutPlannerItem(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long = 0,
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weightKg: Double = 0.0,
    val durationMin: Int = 0,
    val restSec: Int? = null,
    val notes: String? = null,
    val orderIndex: Int = 0
)

