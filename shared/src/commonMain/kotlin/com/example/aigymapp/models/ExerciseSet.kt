package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic ExerciseSet model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class ExerciseSet(
    val id: Long = 0,
    var setNumber: Int = 0,
    var reps: Int = 0,
    var weight: Float = 0f,
    var isCompleted: Boolean = false,
    var restTime: Int = 60
) {
    // Backward compatibility properties
    var setReps: Int
        get() = reps
        set(value) { reps = value }
    
    var setWeight: Float
        get() = weight
        set(value) { weight = value }
    
    // Helper methods for backward compatibility
    fun getRepsValue(): Int = reps
    fun getWeightValue(): Float = weight
}

