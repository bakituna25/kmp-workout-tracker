package com.example.aigymapp.utils

import com.example.aigymapp.repository.AppRepository

/**
 * ExerciseSeeder - Adds default exercises if database is empty
 * 
 * Contains 58 exercises ported from legacy Android app.
 * Categories are mapped to English (CHEST → Chest, BACK → Back, etc.)
 */
object ExerciseSeeder {
    
    /**
     * Category mapping - Converts legacy categories to English categories
     */
    private val categoryMapping = mapOf(
        "CHEST" to "Göğüs",
        "BACK" to "Sırt",
        "SHOULDERS" to "Omuz",
        "ARMS" to "Kol",
        "LEGS" to "Bacak",
        "CORE" to "Karın",
        "CARDIO" to "Kardiyo"
    )
    
    /**
     * Adds all exercises to the database
     * 
     * @param repository AppRepository instance
     */
    suspend fun seedExercises(repository: AppRepository) {
        println("🌱 ExerciseSeeder: Egzersizler veritabanına ekleniyor...")
        
        val exercises = getAllExercises()
        
        exercises.forEachIndexed { index, (name, legacyCategory) ->
            val englishCategory = categoryMapping[legacyCategory] ?: "Diğer"
            
            try {
                repository.insertExercise(
                    name = name,
                    muscleGroup = englishCategory,
                    equipmentNeeded = determineEquipment(name),
                    description = null,
                    difficulty = "Medium",
                    isBodyweight = isBodyweightExercise(name)
                )
            } catch (e: Exception) {
                println("⚠️ ExerciseSeeder: '$name' eklenirken hata: ${e.message}")
            }
        }
        
        println("✅ ExerciseSeeder: ${exercises.size} egzersiz başarıyla eklendi!")
    }
    
    /**
     * Returns all exercises (ported from legacy ExerciseDataProvider)
     */
    private fun getAllExercises(): List<Pair<String, String>> {
        return listOf(
            // CHEST - Chest (9 exercises)
            "Chest Press Machine" to "CHEST",
            "Incline Chest Press Machine" to "CHEST",
            "Decline Chest Press Machine" to "CHEST",
            "Pec Deck" to "CHEST",
            "Cable Crossover" to "CHEST",
            "Smith Machine Bench Press" to "CHEST",
            "Smith Machine Incline Bench Press" to "CHEST",
            "Smith Machine Decline Bench Press" to "CHEST",
            "Assisted Dip Machine" to "CHEST",
            
            // BACK - Back (7 exercises)
            "Lat Pulldown Machine" to "BACK",
            "Assisted Pull-Up Machine" to "BACK",
            "Seated Row Machine" to "BACK",
            "T-Bar Row Machine" to "BACK",
            "Cable Pullover" to "BACK",
            "Reverse Fly Machine" to "BACK",
            "Back Extension Machine" to "BACK",
            
            // SHOULDERS - Shoulders (7 exercises)
            "Shoulder Press Machine" to "SHOULDERS",
            "Smith Machine Shoulder Press" to "SHOULDERS",
            "Lateral Raise Machine" to "SHOULDERS",
            "Cable Lateral Raise" to "SHOULDERS",
            "Rear Delt Fly Machine" to "SHOULDERS",
            "Cable Face Pull" to "SHOULDERS",
            "Smith Machine Upright Row" to "SHOULDERS",
            
            // ARMS - Arms (5 exercises)
            "Cable Biceps Curl" to "ARMS",
            "Preacher Curl Machine" to "ARMS",
            "Cable Triceps Pushdown" to "ARMS",
            "Triceps Extension Machine" to "ARMS",
            "Seated Dip Machine" to "ARMS",
            
            // LEGS - Legs (12 exercises)
            "Hack Squat Machine" to "LEGS",
            "Smith Machine Squat" to "LEGS",
            "Smith Machine Lunge" to "LEGS",
            "Leg Press Machine" to "LEGS",
            "Leg Extension Machine" to "LEGS",
            "Leg Curl Machine" to "LEGS",
            "Standing Calf Raise Machine" to "LEGS",
            "Seated Calf Raise Machine" to "LEGS",
            "Hip Abduction Machine" to "LEGS",
            "Hip Adduction Machine" to "LEGS",
            "Hip Thrust Machine" to "LEGS",
            "Glute Kickback Machine" to "LEGS",
            
            // CORE - Core (4 exercises)
            "Ab Crunch Machine" to "CORE",
            "Cable Crunch" to "CORE",
            "Torso Rotation Machine" to "CORE",
            "Cable Woodchopper" to "CORE",
            
            // CARDIO - Cardio (14 exercises)
            "Treadmill" to "CARDIO",
            "Elliptical Trainer" to "CARDIO",
            "Stationary Bike" to "CARDIO",
            "Recumbent Bike" to "CARDIO",
            "Spin Bike" to "CARDIO",
            "Stair Climber" to "CARDIO",
            "Stepper Machine" to "CARDIO",
            "Rowing Machine" to "CARDIO",
            "Air Bike" to "CARDIO",
            "VersaClimber" to "CARDIO",
            "Jacob's Ladder" to "CARDIO",
            "Ski Ergometer" to "CARDIO",
            "Arm Ergometer" to "CARDIO",
            "Curve Treadmill" to "CARDIO"
        )
    }
    
    /**
     * Determines equipment type from exercise name
     */
    private fun determineEquipment(exerciseName: String): String {
        return when {
            exerciseName.contains("Machine", ignoreCase = true) -> "Machine"
            exerciseName.contains("Cable", ignoreCase = true) -> "Cable"
            exerciseName.contains("Smith Machine", ignoreCase = true) -> "Smith Machine"
            exerciseName.contains("Treadmill", ignoreCase = true) -> "Treadmill"
            exerciseName.contains("Bike", ignoreCase = true) -> "Bike"
            exerciseName.contains("Rowing", ignoreCase = true) -> "Rowing Machine"
            exerciseName.contains("Elliptical", ignoreCase = true) -> "Elliptical"
            exerciseName.contains("Climber", ignoreCase = true) -> "Climbing Machine"
            else -> "Other"
        }
    }
    
    /**
     * Determines if exercise is a bodyweight exercise
     */
    private fun isBodyweightExercise(exerciseName: String): Long {
        // All these exercises require machines/equipment, so not bodyweight
        return 0L
    }
}
