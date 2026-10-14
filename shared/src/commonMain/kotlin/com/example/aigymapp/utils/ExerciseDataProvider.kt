package com.example.aigymapp.utils

import com.example.aigymapp.models.Exercise

/**
 * Utility class to provide exercise data for different muscle groups
 */
object ExerciseDataProvider {
    private val exerciseMap: MutableMap<String, List<Exercise>> = HashMap()

    init {
        initializeExerciseData()
    }

    private fun initializeExerciseData() {
        // Chest exercises
        val chestExercises: MutableList<Exercise> = ArrayList()
        chestExercises.add(Exercise("Chest Press Machine", "CHEST"))
        chestExercises.add(Exercise("Incline Chest Press Machine", "CHEST"))
        chestExercises.add(Exercise("Decline Chest Press Machine", "CHEST"))
        chestExercises.add(Exercise("Pec Deck", "CHEST"))
        chestExercises.add(Exercise("Cable Crossover", "CHEST"))
        chestExercises.add(Exercise("Smith Machine Bench Press", "CHEST"))
        chestExercises.add(Exercise("Smith Machine Incline Bench Press", "CHEST"))
        chestExercises.add(Exercise("Smith Machine Decline Bench Press", "CHEST"))
        chestExercises.add(Exercise("Assisted Dip Machine", "CHEST"))
        exerciseMap["CHEST"] = chestExercises

        // Back exercises
        val backExercises: MutableList<Exercise> = ArrayList()
        backExercises.add(Exercise("Lat Pulldown Machine", "BACK"))
        backExercises.add(Exercise("Assisted Pull-Up Machine", "BACK"))
        // Re-added by request: Seated Row Machine
        backExercises.add(Exercise("Seated Row Machine", "BACK"))
        backExercises.add(Exercise("T-Bar Row Machine", "BACK"))
        backExercises.add(Exercise("Cable Pullover", "BACK"))
        backExercises.add(Exercise("Reverse Fly Machine", "BACK"))
        backExercises.add(Exercise("Back Extension Machine", "BACK"))
        exerciseMap["BACK"] = backExercises

        // Shoulders exercises
        val shouldersExercises: MutableList<Exercise> = ArrayList()
        shouldersExercises.add(Exercise("Shoulder Press Machine", "SHOULDERS"))
        shouldersExercises.add(Exercise("Smith Machine Shoulder Press", "SHOULDERS"))
        shouldersExercises.add(Exercise("Lateral Raise Machine", "SHOULDERS"))
        shouldersExercises.add(Exercise("Cable Lateral Raise", "SHOULDERS"))
        shouldersExercises.add(Exercise("Rear Delt Fly Machine", "SHOULDERS"))
        shouldersExercises.add(Exercise("Cable Face Pull", "SHOULDERS"))
        shouldersExercises.add(Exercise("Smith Machine Upright Row", "SHOULDERS"))
        exerciseMap["SHOULDERS"] = shouldersExercises

        // Arms exercises
        val armsExercises: MutableList<Exercise> = ArrayList()
        armsExercises.add(Exercise("Cable Biceps Curl", "ARMS"))
        armsExercises.add(Exercise("Preacher Curl Machine", "ARMS"))
        armsExercises.add(Exercise("Cable Triceps Pushdown", "ARMS"))
        armsExercises.add(Exercise("Triceps Extension Machine", "ARMS"))
        armsExercises.add(Exercise("Seated Dip Machine", "ARMS"))
        exerciseMap["ARMS"] = armsExercises

        // Legs exercises
        val legsExercises: MutableList<Exercise> = ArrayList()
        legsExercises.add(Exercise("Hack Squat Machine", "LEGS"))
        legsExercises.add(Exercise("Smith Machine Squat", "LEGS"))
        legsExercises.add(Exercise("Smith Machine Lunge", "LEGS"))
        legsExercises.add(Exercise("Leg Press Machine", "LEGS"))
        legsExercises.add(Exercise("Leg Extension Machine", "LEGS"))
        legsExercises.add(Exercise("Leg Curl Machine", "LEGS"))
        legsExercises.add(Exercise("Standing Calf Raise Machine", "LEGS"))
        legsExercises.add(Exercise("Seated Calf Raise Machine", "LEGS"))
        legsExercises.add(Exercise("Hip Abduction Machine", "LEGS"))
        legsExercises.add(Exercise("Hip Adduction Machine", "LEGS"))
        legsExercises.add(Exercise("Hip Thrust Machine", "LEGS"))
        legsExercises.add(Exercise("Glute Kickback Machine", "LEGS"))
        exerciseMap["LEGS"] = legsExercises

        // Abs exercises
        val absExercises: MutableList<Exercise> = ArrayList()
        absExercises.add(Exercise("Ab Crunch Machine", "CORE"))
        absExercises.add(Exercise("Cable Crunch", "CORE"))
        absExercises.add(Exercise("Torso Rotation Machine", "CORE"))
        absExercises.add(Exercise("Cable Woodchopper", "CORE"))
        exerciseMap["CORE"] = absExercises

        // Cardio exercises
        val cardioExercises: MutableList<Exercise> = ArrayList()
        cardioExercises.add(Exercise("Treadmill", "CARDIO"))
        cardioExercises.add(Exercise("Elliptical Trainer", "CARDIO"))
        cardioExercises.add(Exercise("Stationary Bike", "CARDIO"))
        cardioExercises.add(Exercise("Recumbent Bike", "CARDIO"))
        cardioExercises.add(Exercise("Spin Bike", "CARDIO"))
        cardioExercises.add(Exercise("Stair Climber", "CARDIO"))
        cardioExercises.add(Exercise("Stepper Machine", "CARDIO"))
        cardioExercises.add(Exercise("Rowing Machine", "CARDIO"))
        cardioExercises.add(Exercise("Air Bike", "CARDIO"))
        cardioExercises.add(Exercise("VersaClimber", "CARDIO"))
        cardioExercises.add(Exercise("Jacob's Ladder", "CARDIO"))
        cardioExercises.add(Exercise("Ski Ergometer", "CARDIO"))
        cardioExercises.add(Exercise("Arm Ergometer", "CARDIO"))
        cardioExercises.add(Exercise("Curve Treadmill", "CARDIO"))
        exerciseMap["CARDIO"] = cardioExercises
    }

    /**
     * Get exercises for a specific muscle group
     */
    fun getExercisesForMuscleGroup(muscleGroup: String): List<Exercise>? {
        return exerciseMap[muscleGroup] ?: ArrayList()
    }

    val availableMuscleGroups: List<String>
        /**
         * Get all available muscle groups
         */
        get() = ArrayList(exerciseMap.keys)

    /**
     * Get exercise count for a specific muscle group
     */
    fun getExerciseCount(muscleGroup: String): Int {
        val exercises = exerciseMap[muscleGroup]
        return exercises?.size ?: 0
    }
}
