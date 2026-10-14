package com.example.aigymapp.ui.viewmodels

import com.example.aigymapp.models.Goal
import com.example.aigymapp.models.GoalType
import com.example.aigymapp.models.GoalWithProgress
import com.example.aigymapp.models.toGoalType
import com.example.aigymapp.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * GoalViewModel
 * Manages fitness goals and their progress tracking
 * 
 * Progress is calculated from COMPLETED workouts only (workout_set_ticks table)
 */
class GoalViewModel(
    private val appRepository: AppRepository,
    private val coroutineScope: CoroutineScope
) {
    // UI State for goals with progress
    private val _goalsWithProgress = MutableStateFlow<List<GoalWithProgress>>(emptyList())
    val goalsWithProgress: StateFlow<List<GoalWithProgress>> = _goalsWithProgress.asStateFlow()
    
    // All available exercises for goal creation
    private val _allExercises = MutableStateFlow<List<com.example.aigymapp.database.Exercises>>(emptyList())
    val allExercises: StateFlow<List<com.example.aigymapp.database.Exercises>> = _allExercises.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadGoalsWithProgress()
        loadAllExercises()
    }
    
    /**
     * Load all goals and calculate their progress
     */
    private fun loadGoalsWithProgress() {
        coroutineScope.launch {
            _isLoading.value = true
            try {
                // Get goals with exercise information
                appRepository.getGoalsWithExercise()
                    .catch { exception ->
                        // Handle errors gracefully
                        println("Error loading goals: ${exception.message}")
                        _goalsWithProgress.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { dbGoals ->
                        val goalsWithProgress = dbGoals.map { dbGoal ->
                            // Convert database model to domain model
                            val goal = Goal(
                                id = dbGoal.goal_id,
                                title = dbGoal.goal_title,
                                description = dbGoal.goal_description,
                                goalType = dbGoal.goal_type.toGoalType(),
                                targetValue = dbGoal.target_value.toInt(),
                                exerciseId = dbGoal.exercise_id,
                                muscleGroup = dbGoal.muscle_group,
                                startDate = LocalDate.parse(dbGoal.start_date),
                                endDate = dbGoal.end_date?.let { LocalDate.parse(it) },
                                isActive = dbGoal.is_active == 1L,
                                createdAt = dbGoal.created_at
                            )
                            
                            // Calculate progress from database
                            val currentValue = appRepository.calculateGoalProgress(goal)
                            val progressPercent = if (goal.targetValue > 0) {
                                (currentValue.toFloat() / goal.targetValue.toFloat()) * 100f
                            } else {
                                0f
                            }
                            
                            GoalWithProgress(
                                goal = goal,
                                currentValue = currentValue,
                                progressPercent = progressPercent,
                                exerciseName = dbGoal.exercise_name
                            )
                        }
                        
                        _goalsWithProgress.value = goalsWithProgress
                        // Set loading to false after first emission
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                println("Error in loadGoalsWithProgress: ${e.message}")
                _goalsWithProgress.value = emptyList()
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load all exercises for goal creation
     */
    private fun loadAllExercises() {
        coroutineScope.launch {
            appRepository.getAllExercises()
                .collect { exercises ->
                    _allExercises.value = exercises
                }
        }
    }
    
    /**
     * Add a new goal
     */
    fun addGoal(goal: Goal) {
        coroutineScope.launch {
            try {
                appRepository.insertGoal(goal)
                println("Goal successfully added: ${goal.title}")
                // Manually refresh to update UI immediately
                refreshProgress()
            } catch (e: Exception) {
                println("ERROR adding goal: ${e.message}")
                e.printStackTrace()
                // Don't crash the app, just log the error
            }
        }
    }
    
    /**
     * Update an existing goal
     */
    fun updateGoal(goal: Goal) {
        coroutineScope.launch {
            appRepository.updateGoal(goal)
            // Goals will be automatically refreshed via Flow
        }
    }
    
    /**
     * Delete a goal
     */
    fun deleteGoal(goalId: Long) {
        coroutineScope.launch {
            appRepository.deleteGoal(goalId)
            // Goals will be automatically refreshed via Flow
        }
    }
    
    /**
     * Deactivate a goal (soft delete)
     */
    fun deactivateGoal(goalId: Long) {
        coroutineScope.launch {
            appRepository.updateGoalActiveStatus(goalId, false)
            // Goals will be automatically refreshed via Flow
        }
    }
    
    /**
     * Manually refresh progress calculations
     * (Usually not needed as progress is calculated on each load)
     */
    fun refreshProgress() {
        loadGoalsWithProgress()
    }
    
    /**
     * Get unique muscle groups from all exercises
     */
    fun getMuscleGroups(): List<String> {
        return _allExercises.value
            .mapNotNull { it.muscle_group }
            .distinct()
            .sorted()
    }
    
    /**
     * Get exercises filtered by muscle group
     */
    fun getExercisesByMuscleGroup(muscleGroup: String): List<com.example.aigymapp.database.Exercises> {
        return _allExercises.value.filter { it.muscle_group == muscleGroup }
    }
}
