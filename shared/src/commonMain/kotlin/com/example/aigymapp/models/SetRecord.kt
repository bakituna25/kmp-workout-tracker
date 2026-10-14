package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Platform-agnostic SetRecord model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class SetRecord(
    val id: Long = 0,
    var progressId: Long,
    val setNumber: Long,
    val plannedReps: Long,
    val actualReps: Long,
    val plannedWeight: Double,
    val actualWeight: Double,
    val completed: Long,
    val restSec: Long? = null,
    val isChecked: Boolean = completed == 1L,
    val setIndex: Long = setNumber - 1,
    var sessionId: Long = 0
) {
    // Backward compatibility
    val isCompleted: Boolean get() = completed == 1L
    val restDuration: Int get() = (restSec?.toInt() ?: 60) * 1000 // Convert seconds to milliseconds
}

