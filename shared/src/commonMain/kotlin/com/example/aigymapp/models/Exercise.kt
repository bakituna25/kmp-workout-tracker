package com.example.aigymapp.models

import kotlinx.serialization.Serializable

/**
 * Type alias for backward compatibility
 */
typealias ExerciseEntity = Exercise

/**
 * Platform-agnostic Exercise model
 * Migrated to commonMain from androidMain
 */
@Serializable
data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleGroup: String,
    val description: String = "",
    val equipmentNeeded: String = "",
    val difficulty: String = "",
    val isBodyweight: Boolean = false,
    val sets: List<ExerciseSet> = emptyList(),
    var isCompleted: Boolean = false
) {
    // Backward compatibility aliases
    val equipment: String get() = equipmentNeeded
    
    // Helper methods
    fun getEquipmentDisplayName(): String = if (equipmentNeeded.isEmpty()) "No equipment" else equipmentNeeded
    fun getMuscleGroupDisplayName(): String = if (muscleGroup.isEmpty()) "General" else muscleGroup
    
    // Secondary constructors for backward compatibility
    constructor(name: String, muscleGroup: String) : this(
        id = 0,
        name = name,
        muscleGroup = muscleGroup,
        description = ""
    )
    
    constructor(name: String, muscleGroup: String, description: String) : this(
        id = 0,
        name = name,
        muscleGroup = muscleGroup,
        description = description
    )
    
    constructor(
        id: Long,
        name: String,
        muscleGroup: String,
        equipment: String,
        description: String,
        difficulty: String,
        isBodyweight: Boolean
    ) : this(
        id = id,
        name = name,
        muscleGroup = muscleGroup,
        description = description,
        equipmentNeeded = equipment,
        difficulty = difficulty,
        isBodyweight = isBodyweight
    )
}

/**
 * Extension functions for Exercise/ExerciseEntity
 */
fun ExerciseEntity.getEquipmentDisplayName(): String = if (equipmentNeeded.isEmpty()) "No equipment" else equipmentNeeded
fun ExerciseEntity.getMuscleGroupDisplayName(): String = if (muscleGroup.isEmpty()) "General" else muscleGroup

