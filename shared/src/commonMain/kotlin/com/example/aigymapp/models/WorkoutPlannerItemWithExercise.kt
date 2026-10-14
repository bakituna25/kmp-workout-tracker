package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutPlannerItemWithExercise model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class WorkoutPlannerItemWithExercise(
    val item: WorkoutPlannerItem,
    val exercise: Exercise?,
    val planItemId: Long = item.id,
    val exerciseName: String = exercise?.name ?: item.exerciseName,
    val plannedSets: Int = item.sets,
    val completedSets: Int = 0,
    val dateEpochDay: Long = 0,
    val muscleGroup: String = exercise?.muscleGroup ?: "",
    val restSec: Int = item.restSec ?: 60,
    val isChecked: Boolean = false
) {
    fun getName(): String = exerciseName
    fun getDisplayName(): String = exerciseName
    fun getWeightDisplay(): String = if (item.weightKg > 0) "${item.weightKg} kg" else ""
    fun getSetsRepsDisplay(): String = "${item.sets} x ${item.reps}"
    fun getRestDisplay(): String = "${item.restSec ?: 60}s"
    fun getDurationDisplay(): String = if (item.durationMin > 0) "${item.durationMin} min" else ""
    fun getMuscleGroupDisplay(): String = exercise?.muscleGroup ?: muscleGroup
    fun getMuscleGroupDisplayName(): String = exercise?.muscleGroup ?: muscleGroup
    fun getCurrentWeight(): Double = item.weightKg
    fun hasNotes(): Boolean = !item.notes.isNullOrEmpty()
    fun toDayCardRowList(): List<Any> = emptyList() // Placeholder
}

