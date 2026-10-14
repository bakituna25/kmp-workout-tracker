package com.example.aigymapp.models

import com.example.aigymapp.database.GetScheduledForDate
import com.example.aigymapp.database.Workout_planner_items
import com.example.aigymapp.repository.AppRepository
import kotlinx.coroutines.runBlocking

/**
 * Extension functions to convert SQLDelight generated classes to model classes
 * Platform-agnostic extensions migrated to commonMain
 */

fun List<GetScheduledForDate>.toScheduledWithDetailsList(): List<ScheduledWithDetails> {
    return this.map { it.toScheduledWithDetails() }
}

fun GetScheduledForDate.toScheduledWithDetails(): ScheduledWithDetails {
    return ScheduledWithDetails(
        scheduledWorkoutId = this.sw_id,
        planItemId = this.sw_plan_item_id,
        exerciseId = this.wpi_exercise_id,
        exerciseName = this.exercise_name,
        sets = this.wpi_sets.toInt(),
        reps = this.wpi_reps.toInt(),
        weightKg = this.wpi_weight_kg,
        restSec = this.wpi_rest_sec?.toInt(),
        notes = this.wpi_notes,
        muscleGroup = this.exercise_muscle_group ?: "",
        epochDay = this.sw_date_epoch_day
    )
}

fun List<Workout_planner_items>.toWorkoutPlannerItemWithExerciseList(appRepository: AppRepository): List<WorkoutPlannerItemWithExercise> {
    return this.map { it.toWorkoutPlannerItemWithExercise(appRepository) }
}

fun Workout_planner_items.toWorkoutPlannerItemWithExercise(appRepository: AppRepository): WorkoutPlannerItemWithExercise {
    val exercise = runBlocking {
        appRepository.getExerciseById(exercise_id)
    }
    
    val exerciseModel = exercise?.let {
        Exercise(
            id = it.id,
            name = it.name,
            muscleGroup = it.muscle_group ?: "",
            description = it.description ?: "",
            equipmentNeeded = it.equipment_needed ?: "",
            difficulty = it.difficulty ?: "",
            isBodyweight = it.is_bodyweight == 1L
        )
    }
    
    val item = WorkoutPlannerItem(
        id = this.id,
        sessionId = this.session_id,
        exerciseId = this.exercise_id,
        exerciseName = exercise?.name ?: "",
        sets = this.sets.toInt(),
        reps = this.reps.toInt(),
        weightKg = this.weight_kg,
        durationMin = this.duration_min.toInt(),
        restSec = this.rest_sec?.toInt(),
        notes = this.notes,
        orderIndex = 0 // Default value since order_index doesn't exist in workout_planner_items table
    )
    
    return WorkoutPlannerItemWithExercise(
        item = item,
        exercise = exerciseModel
    )
}

