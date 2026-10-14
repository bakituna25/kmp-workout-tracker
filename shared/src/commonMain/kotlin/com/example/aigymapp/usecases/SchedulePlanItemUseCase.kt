package com.example.aigymapp.usecases

import com.example.aigymapp.models.WorkoutPlannerItem
import com.example.aigymapp.repository.SchedulingRepository

/**
 * Use case for scheduling workout plan items to specific dates
 */
class SchedulePlanItemUseCase(private val repository: SchedulingRepository) {

    /**
     * Schedule a workout plan item to multiple dates
     * @param item The workout planner item to schedule
     * @param dates List of epoch day values representing the dates
     */
    suspend fun schedule(item: WorkoutPlannerItem, dates: List<Long>) {
        repository.scheduleToMultipleDatesSync(
            item.id,
            dates
        )
    }

    /**
     * Get all scheduled dates for a specific plan item
     * @param planItemId The ID of the plan item
     * @return List of epoch day values
     */
    suspend fun getScheduledDates(planItemId: Long): List<Long> {
        return repository.getDatesForPlanItem(planItemId)
    }
}

