package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Goal Type Enum
 * Defines the type of fitness goal
 */
enum class GoalType {
    EXERCISE_COUNT,    // Belirli egzersizi X kere tamamla (unique days)
    MONTHLY_DAYS,      // Ayda X gün antrenman yap (any exercise)
    MUSCLE_GROUP_COUNT // Belirli kas grubuna X antrenman yap (unique days)
}

/**
 * Goal Model
 * Represents a fitness goal set by the user
 * 
 * Progress is calculated based on COMPLETED workouts from workout_set_ticks table,
 * not just scheduled workouts.
 */
@Serializable
data class Goal(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val goalType: GoalType,
    val targetValue: Int, // Target count (e.g., 20 days, 30 workouts)
    
    // Optional: For exercise-specific or muscle-group-specific goals
    val exerciseId: Long? = null,
    val muscleGroup: String? = null,
    
    // Date range for the goal
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    
    val isActive: Boolean = true,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    /**
     * Generate a display title if custom title is not provided
     */
    fun getDisplayTitle(exerciseName: String? = null): String {
        if (title.isNotBlank()) return title
        
        return when (goalType) {
            GoalType.EXERCISE_COUNT -> {
                val exName = exerciseName ?: "Egzersiz"
                "$targetValue $exName Antrenmanı"
            }
            GoalType.MONTHLY_DAYS -> "Ayda $targetValue Gün Antrenman"
            GoalType.MUSCLE_GROUP_COUNT -> {
                val mgName = muscleGroup ?: "Kas Grubu"
                "$targetValue $mgName Antrenmanı"
            }
        }
    }
}

/**
 * Convert GoalType enum to String for database storage
 */
fun GoalType.toDatabaseString(): String = this.name

/**
 * Convert String from database to GoalType enum
 */
fun String.toGoalType(): GoalType = GoalType.valueOf(this)
