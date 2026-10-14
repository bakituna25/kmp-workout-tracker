package com.example.aigymapp.models

import com.example.aigymapp.database.Day_progress
import com.example.aigymapp.database.DayCards
import com.example.aigymapp.database.DayExerciseSummary
import kotlinx.serialization.Serializable

/**
 * Completion status enum for backward compatibility
 * Platform-agnostic enum migrated to commonMain
 */
@Serializable
enum class CompletionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED,
    FAILED,
    FULL,
    PARTIAL,
    NONE,
    NO_PLAN;
    
    companion object {
        fun fromString(status: String): CompletionStatus {
            return when (status.uppercase()) {
                "FULL" -> FULL
                "PARTIAL" -> PARTIAL
                "NONE" -> NONE
                "NO_PLAN" -> NO_PLAN
                "COMPLETED" -> FULL // Backward compatibility
                "IN_PROGRESS" -> PARTIAL // Backward compatibility
                "NOT_STARTED" -> NOT_STARTED
                "SKIPPED" -> SKIPPED
                "FAILED" -> FAILED
                else -> NOT_STARTED
            }
        }
    }
}

/**
 * Extension property to get status as enum
 */
val Day_progress.statusEnum: CompletionStatus
    get() = CompletionStatus.fromString(status)

/**
 * Extension property for backward compatibility - entities placeholder
 */
val Day_progress.entities: List<Any>
    get() = emptyList()

/**
 * Extension property to get overall progress
 */
val Day_progress.overallProgress: OverallProgress
    get() = OverallProgress(
        plannedSets = planned_sets.toInt(),
        completedSets = completed_sets.toInt(),
        completionPercent = completion_percent.toInt(),
        status = statusEnum
    )

/**
 * Extension property to get exercise progress list
 */
val Day_progress.exerciseProgressList: List<ExerciseProgressDetail>
    get() = emptyList() // Placeholder - would need actual exercise data

/**
 * Extension properties for DayCards to match expected schema
 */
val DayCards.plannedSets: Long
    get() = planned_sets

val DayCards.completedSets: Long
    get() = completed_sets ?: 0L

val DayCards.planItemId: Long
    get() = plan_item_id

val DayCards.exerciseName: String
    get() = exercise_name ?: ""

val DayCards.muscleGroup: String
    get() = muscle_group ?: ""

val DayCards.plannedWeightKg: Double
    get() = planned_weight_kg ?: 0.0

val DayCards.plannedRestSec: Long
    get() = planned_rest_sec ?: 60L

val DayCards.avgReps: Long
    get() = (planned_reps_total ?: 0L) / maxOf(1L, planned_sets)

/**
 * Extension properties for DayExerciseSummary
 */
val DayExerciseSummary.plannedSets: Long
    get() = planned_sets

val DayExerciseSummary.completedSets: Long
    get() = completed_sets ?: 0L

val DayExerciseSummary.planItemId: Long
    get() = plan_item_id

val DayExerciseSummary.exerciseName: String
    get() = exercise_name ?: ""

val DayExerciseSummary.muscleGroup: String
    get() = muscle_group ?: ""

val DayExerciseSummary.plannedRepsTotal: Long
    get() = planned_reps_total ?: 0L

/**
 * Wrapper class for day progress details with more info
 * Platform-agnostic model
 */
@Serializable
data class DayProgressDetails(
    val date: String,
    val plannedSets: Long,
    val completedSets: Long,
    val completionPercent: Double,
    val status: CompletionStatus,
    val overallProgress: OverallProgress,
    val exerciseProgressList: List<ExerciseProgressDetail> = emptyList()
)

@Serializable
data class OverallProgress(
    val plannedSets: Int,
    val completedSets: Int,
    val completionPercent: Int,
    val status: CompletionStatus
)

@Serializable
data class ExerciseProgressDetail(
    val exerciseName: String,
    val muscleGroup: String,
    val plannedSets: Int,
    val completedSets: Int,
    val completionPercent: Int,
    val restSec: Long? = null,
    val isChecked: Boolean = false,
    val setIndex: Long = 0
)

/**
 * Convert Day_progress to DayProgressDetails
 */
fun Day_progress.toDayProgressDetails(): DayProgressDetails {
    val statusEnum = CompletionStatus.fromString(status)
    return DayProgressDetails(
        date = date,
        plannedSets = planned_sets,
        completedSets = completed_sets,
        completionPercent = completion_percent,
        status = statusEnum,
        overallProgress = OverallProgress(
            plannedSets = planned_sets.toInt(),
            completedSets = completed_sets.toInt(),
            completionPercent = completion_percent.toInt(),
            status = statusEnum
        )
    )
}

/**
 * Extension function to get day progress details
 * Alias for toDayProgressDetails()
 */
fun Day_progress.getDayProgressDetails(): DayProgressDetails = toDayProgressDetails()

