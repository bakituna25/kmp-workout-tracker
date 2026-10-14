package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutExercise model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class WorkoutExercise(
    val id: Long = 0,
    val exerciseId: Long = 0,
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Float = 0f,
    val isCompleted: Boolean = false
)

