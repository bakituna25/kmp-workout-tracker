package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Seçili günün egzersiz listesi için UI model
 */
@Serializable
data class DayExerciseItem(
    val planItemId: Long,
    val name: String,
    val muscleGroup: String?,
    val plannedSets: Int,
    val completedSets: Int,
    val plannedRepsTotal: Int,
    val percent: Int
) {
    
    /**
     * Set bilgisi formatı: "3/5 set • %60"
     */
    val setInfoDisplay: String
        get() = "$completedSets/$plannedSets set • %$percent"
    
    /**
     * Kas grubu görüntü adı
     */
    val muscleGroupDisplay: String?
        get() = muscleGroup?.let { group ->
            when (group.uppercase()) {
                "CHEST" -> "Göğüs"
                "BACK" -> "Sırt"
                "SHOULDERS" -> "Omuz"
                "ARMS" -> "Kol"
                "LEGS" -> "Bacak"
                "CORE" -> "Karın"
                "CARDIO" -> "Kardio"
                else -> group
            }
        }
}
