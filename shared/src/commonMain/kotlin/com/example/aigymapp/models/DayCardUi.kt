package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Progress takviminin altındaki kart listesi için UI model
 * Planner'daki kartları yansıtır
 */
@Serializable
data class DayCardUi(
    val planItemId: Long,
    val name: String,
    val muscle: String?,
    val scheme: String,          // "3×10"
    val weightLabel: String,     // "15.0 kg"
    val restLabel: String,       // "60s rest"
    val completedSets: Int,
    val plannedSets: Int,
    val percent: Int
) {
    
    /**
     * Kas grubu görüntü adı
     */
    val muscleGroupDisplay: String?
        get() = muscle?.let { group ->
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
    
    /**
     * Tamamlanma durumu metni: "2/3 set"
     */
    val completedStatusText: String
        get() = "$completedSets/$plannedSets set"
    
    /**
     * Yüzde metni: "67%"
     */
    val percentText: String
        get() = "$percent%"
    
    /**
     * Tamamen tamamlanmış mı?
     */
    val isFullyCompleted: Boolean
        get() = completedSets >= plannedSets && plannedSets > 0
}
