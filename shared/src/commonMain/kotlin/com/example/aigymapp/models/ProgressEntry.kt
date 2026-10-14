package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Platform-agnostic ProgressEntry model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class ProgressEntry(
    val id: Long = 0,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val weight: Double = 0.0,
    val bodyFatPercentage: Double = 0.0,
    val muscleMass: Double = 0.0,
    val notes: String? = null,
    val photoPath: String? = null
)

