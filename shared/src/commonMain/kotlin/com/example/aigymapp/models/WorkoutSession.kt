package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutSession model
 * Migrated to commonMain from androidMain
 * 
 * Note: For Android Intent compatibility, we keep a marker interface.
 * The actual serialization uses kotlinx.serialization.
 */
@Serializable
data class WorkoutSession(
    val id: Long = 0,
    val templateId: Long? = null,
    var name: String? = null,
    var startTime: Long? = null,
    var endTime: Long? = null,
    var totalDuration: Long = 0,
    var status: String? = null,
    var notes: String? = null,
    var rating: Long = 0,
    var caloriesBurned: Long = 0,
    var totalWeightLifted: Double = 0.0,
    var totalSets: Long = 0,
    var totalReps: Long = 0,
    val createdDate: Long = Clock.System.now().toEpochMilliseconds(),
    var completedSets: Long = 0,
    var totalExercises: Long = 0,
    var completedExercises: Long = 0,
    var completionPercentage: Double = 0.0,
    var performanceGrade: String = "D",
    var personalRecordsCount: Long = 0,
    var achievementsEarned: Long = 0,
    var averageRestTime: Long = 0,
    var workoutIntensity: Double = 0.0,
    var updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    // Helper methods for backward compatibility
    fun toInt(value: Long): Int = value.toInt()
    fun toFloat(value: Double): Float = value.toFloat()
}

