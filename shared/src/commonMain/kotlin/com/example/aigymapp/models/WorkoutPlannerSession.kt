package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutPlannerSession model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class WorkoutPlannerSession(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val targetDate: Long? = null,
    val createdDate: Long = Clock.System.now().toEpochMilliseconds()
)

