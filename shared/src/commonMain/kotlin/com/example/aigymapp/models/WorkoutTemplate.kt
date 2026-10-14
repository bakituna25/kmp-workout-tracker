package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Platform-agnostic WorkoutTemplate model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class WorkoutTemplate(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val exercises: List<Exercise> = emptyList(),
    val createdDate: Long = Clock.System.now().toEpochMilliseconds()
)

