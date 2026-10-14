package com.example.aigymapp.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Platform-agnostic UserProfile model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class UserProfile(
    val id: Long = 1,
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    var height: Double = 0.0,
    var currentWeight: Double = 0.0,
    var targetWeight: Double = 0.0,
    var activityLevel: String = "",
    var fitnessGoal: String = "",
    var profilePhotoPath: String? = null,
    val createdDate: Long = Clock.System.now().toEpochMilliseconds(),
    var dietPreference: String? = null,
    var updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    var bodyFatPercentage: Double = 0.0,
    var goalDurationMonths: Int = 0,
    var weeklyWorkoutGoal: Int = 3,
    var targetBodyFat: Float? = null,
    var birthDate: Long? = null
) {
    // Custom helper methods with special logic
    fun getGenderDisplayName(): String = when (gender.lowercase()) {
        "male", "erkek", "m" -> "Erkek"
        "female", "kadın", "k" -> "Kadın"
        else -> gender.ifEmpty { "Belirtilmemiş" }
    }
    
    fun getMuscleGroupDisplay(): String = ""
    fun getTimezone(): String? = null
    fun getGoalStartWeight(): Double = currentWeight
    fun getTargetBodyFat(): Double = 0.0
    fun getPreferredUnits(): String? = null
    fun getFitnessLevel(): String? = null
    fun getEmail(): String? = null
    
    // No-op setters for backward compatibility
    fun setTimezone(value: String?) { /* no-op */ }
    fun setGoalStartWeight(value: Double) { /* no-op */ }
    fun setPreferredUnits(value: String?) { /* no-op */ }
    fun setFitnessLevel(value: String?) { /* no-op */ }
    fun setEmail(value: String?) { /* no-op */ }
}

