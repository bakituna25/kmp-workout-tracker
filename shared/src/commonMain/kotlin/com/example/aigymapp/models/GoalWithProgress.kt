package com.example.aigymapp.models

/**
 * Goal With Progress
 * Combines a Goal with its current progress calculated from the database
 * 
 * currentValue is calculated from workout_set_ticks table (only completed workouts count)
 */
data class GoalWithProgress(
    val goal: Goal,
    val currentValue: Int,      // Calculated from database (completed workout days)
    val progressPercent: Float, // currentValue / targetValue * 100
    val exerciseName: String? = null // For EXERCISE_COUNT goals
) {
    /**
     * Check if goal is completed
     */
    fun isCompleted(): Boolean = currentValue >= goal.targetValue
    
    /**
     * Get progress percentage clamped to 0-100
     */
    fun getClampedProgressPercent(): Float = progressPercent.coerceIn(0f, 100f)
    
    /**
     * Get display text for current progress
     * Examples: "15/30", "20/20"
     */
    fun getProgressText(): String = "$currentValue/${goal.targetValue}"
    
    /**
     * Get percentage text
     * Examples: "50%", "100%"
     */
    fun getPercentageText(): String = "${getClampedProgressPercent().toInt()}%"
}
