package com.example.aigymapp.utils

import com.example.aigymapp.models.Exercise
import com.example.aigymapp.models.ExerciseSet
import com.example.aigymapp.models.WorkoutSession
import com.example.aigymapp.utils.ProgressCalculationEngine.Achievement.AchievementType
import kotlinx.datetime.*
import kotlin.math.min

/**
 * Advanced progress calculation engine for workout sessions
 * Handles all progress metrics, achievements, and analytics
 */
object ProgressCalculationEngine {
    /**
     * Calculate comprehensive workout progress
     */
    fun calculateWorkoutProgress(
        exercises: List<Exercise>,
        exerciseSets: List<List<ExerciseSet>>,
        session: WorkoutSession,
        previousWorkoutData: Map<String, Any>
    ): ProgressResult {
        val result: ProgressResult = ProgressResult()

        // Basic calculations
        calculateBasicProgress(exercises, exerciseSets, result)

        // Exercise-specific progress
        calculateExerciseProgress(exercises, exerciseSets, result)

        // Time and efficiency metrics
        calculateTimeMetrics(session, exerciseSets, result)

        // Volume and strength metrics
        calculateVolumeMetrics(exerciseSets, result)

        // Achievement detection
        detectAchievements(result, previousWorkoutData)

        // Performance analytics
        calculatePerformanceMetrics(result, previousWorkoutData)

        // Generate motivational message
        generateMotivationalMessage(result)

        return result
    }

    /**
     * Calculate basic progress metrics
     */
    private fun calculateBasicProgress(
        exercises: List<Exercise>,
        exerciseSets: List<List<ExerciseSet>>,
        result: ProgressResult
    ) {
        var totalSets: Int = 0
        var completedSets: Int = 0

        for (sets: List<ExerciseSet> in exerciseSets) {
            for (set: ExerciseSet in sets) {
                totalSets++
                if (set.isCompleted) {
                    completedSets++
                }
            }
        }

        result.totalSets = totalSets
        result.completedSets = completedSets
        result.overallProgress = if (totalSets > 0) (completedSets * 100.0f) / totalSets else 0f
    }

    /**
     * Calculate exercise-specific progress
     */
    private fun calculateExerciseProgress(
        exercises: List<Exercise>,
        exerciseSets: List<List<ExerciseSet>>,
        result: ProgressResult
    ) {
        for (i in exercises.indices) {
            val exercise: Exercise = exercises.get(i)
            val sets: List<ExerciseSet> = exerciseSets.get(i)

            val progress: ExerciseProgress = ExerciseProgress()
            progress.exerciseName = exercise.name
            progress.exerciseIndex = i
            progress.targetSets = sets.size

            var completedSets: Int = 0
            var totalVolume: Float = 0f
            var totalRestTime: Long = 0
            var bestSetIndex: Int = -1
            var bestVolume: Float = 0f

            for (j in sets.indices) {
                val set: ExerciseSet = sets.get(j)
                if (set.isCompleted) {
                    completedSets++
                    val setVolume: Float = set.reps * set.weight
                    totalVolume += setVolume
                    totalRestTime += set.restTime.toLong()

                    // Track best set
                    if (setVolume > bestVolume) {
                        bestVolume = setVolume
                        bestSetIndex = j
                    }
                }
            }

            progress.completedSets = completedSets
            progress.completionPercentage =
                if (progress.targetSets > 0) (completedSets * 100.0f) / progress.targetSets else 0f
            progress.volume = totalVolume
            progress.isCompleted = completedSets == progress.targetSets && progress.targetSets > 0
            progress.bestSet = bestSetIndex
            progress.averageRestTime =
                if (completedSets > 0) totalRestTime / completedSets.toFloat() else 0f

            // Determine status
            if (completedSets == 0) {
                progress.status = "not_started"
            } else if (completedSets < progress.targetSets) {
                progress.status = "in_progress"
            } else {
                progress.status = "completed"
            }

            result.exerciseProgresses.add(progress)
        }
    }

    /**
     * Calculate time-related metrics
     */
    private fun calculateTimeMetrics(
        session: WorkoutSession,
        exerciseSets: List<List<ExerciseSet>>,
        result: ProgressResult
    ) {
        if (session.startTime != null) {
            val currentTime: Long =
                if (session.endTime != null) session.endTime!! else Clock.System.now().toEpochMilliseconds()
            result.totalDuration = currentTime - session.startTime!!
        }

        // Calculate average rest time across all sets
        var totalRestTime: Long = 0
        var restCount: Int = 0

        for (sets: List<ExerciseSet> in exerciseSets) {
            for (set: ExerciseSet in sets) {
                if (set.isCompleted && set.restTime > 0) {
                    totalRestTime += set.restTime.toLong()
                    restCount++
                }
            }
        }

        result.metrics.averageRestTime =
            if (restCount > 0) totalRestTime / restCount.toFloat() else 0f

        // Calculate time efficiency (target vs actual time)
        val estimatedTime: Long = calculateEstimatedWorkoutTime(exerciseSets)
        if (estimatedTime > 0 && result.totalDuration > 0) {
            result.metrics.timeEfficiency = min(
                100.0,
                ((estimatedTime * 100.0f) / result.totalDuration).toDouble()
            ).toFloat()
        }
    }

    /**
     * Calculate volume and strength metrics
     */
    private fun calculateVolumeMetrics(
        exerciseSets: List<List<ExerciseSet>>,
        result: ProgressResult
    ) {
        var totalVolume: Float = 0f

        for (sets: List<ExerciseSet> in exerciseSets) {
            for (set: ExerciseSet in sets) {
                if (set.isCompleted) {
                    totalVolume += set.reps * set.weight
                }
            }
        }

        result.totalVolume = totalVolume
    }

    /**
     * Detect achievements based on current performance
     */
    private fun detectAchievements(
        result: ProgressResult,
        previousWorkoutData: Map<String, Any>
    ) {
        // Personal Record Achievement

        if (checkPersonalRecord(result, previousWorkoutData)) {
            val pr: Achievement = Achievement()
            pr.id = "personal_record_${Clock.System.now().toEpochMilliseconds()}"
            pr.title = "🏆 Kişisel Rekor!"
            pr.description = "Yeni bir kişisel rekor kırdın!"
            pr.icon = "🏆"
            pr.type = AchievementType.PERSONAL_RECORD
            pr.earnedAt = Clock.System.now().toEpochMilliseconds()
            pr.isNew = true
            result.achievements.add(pr)
            result.hasPersonalRecord = true
        }

        // Volume Milestone Achievement
        if (result.totalVolume >= 1000 && result.totalVolume < 1100) {
            val volume: Achievement = Achievement()
            volume.id = "volume_1000"
            volume.title = "💪 1000kg Kulübü!"
            volume.description = "Toplam ${formatFloat(result.totalVolume, 0)} kg ağırlık kaldırdın!"
            volume.icon = "💪"
            volume.type = AchievementType.VOLUME_MILESTONE
            volume.earnedAt = Clock.System.now().toEpochMilliseconds()
            volume.isNew = true
            result.achievements.add(volume)
        }

        // Completion Rate Achievement
        if (result.overallProgress == 100f) {
            val completion: Achievement = Achievement()
            completion.id = "perfect_completion"
            completion.title = "⭐ Mükemmel Tamamlama!"
            completion.description = "Tüm setleri başarıyla tamamladın!"
            completion.icon = "⭐"
            completion.type = AchievementType.COMPLETION_RATE
            completion.earnedAt = Clock.System.now().toEpochMilliseconds()
            completion.isNew = true
            result.achievements.add(completion)
        }

        // Time Efficiency Achievement
        if (result.metrics.timeEfficiency >= 90f) {
            val efficiency: Achievement = Achievement()
            efficiency.id = "time_efficient"
            efficiency.title = "⚡ Zaman Ustası!"
            efficiency.description = "Antrenmanını çok verimli bir sürede tamamladın!"
            efficiency.icon = "⚡"
            efficiency.type = AchievementType.TIME_EFFICIENCY
            efficiency.earnedAt = Clock.System.now().toEpochMilliseconds()
            efficiency.isNew = true
            result.achievements.add(efficiency)
        }
    }

    /**
     * Calculate performance metrics and analytics
     */
    private fun calculatePerformanceMetrics(
        result: ProgressResult,
        previousWorkoutData: Map<String, Any>?
    ) {
        // Volume improvement calculation

        if (previousWorkoutData != null && previousWorkoutData.containsKey("totalVolume")) {
            val previousVolume: Float = previousWorkoutData.get("totalVolume") as Float
            if (previousVolume > 0) {
                result.metrics.volumeImprovement =
                    ((result.totalVolume - previousVolume) / previousVolume) * 100f
            }
        }

        // Consistency score based on completion rates
        result.metrics.consistencyScore = result.overallProgress

        // Workout intensity calculation
        result.metrics.workoutIntensity = calculateWorkoutIntensity(result)

        // Performance grade
        result.metrics.performanceGrade = calculatePerformanceGrade(result)

        // Generate insights
        generateInsights(result)
    }

    /**
     * Generate motivational messages based on progress
     */
    private fun generateMotivationalMessage(result: ProgressResult) {
        if (result.overallProgress == 100f) {
            result.motivationalMessage = "🎉 Harika! Antrenmanını mükemmel tamamladın!"
        } else if (result.overallProgress >= 80f) {
            result.motivationalMessage = "💪 Çok iyi gidiyorsun! Bitirmene az kaldı!"
        } else if (result.overallProgress >= 60f) {
            result.motivationalMessage = "🔥 Yarıyı geçtin! Devam et!"
        } else if (result.overallProgress >= 40f) {
            result.motivationalMessage = "⚡ İyi başlangıç! ivme kazanıyorsun!"
        } else if (result.overallProgress > 0f) {
            result.motivationalMessage = "🚀 Başladın! Her adım önemli!"
        } else {
            result.motivationalMessage = "💪 Hadi başlayalım! Bunu yapabilirsin!"
        }

        // Add achievement-based messages
        if (result.hasPersonalRecord) {
            result.motivationalMessage += " Yeni rekorun harika! 🏆"
        }

        if (result.achievements.size > 1) {
            result.motivationalMessage += " ${result.achievements.size} başarım kazandın! 🌟"
        }
    }

    /**
     * Helper methods
     */
    private fun checkPersonalRecord(
        result: ProgressResult,
        previousData: Map<String, Any>?
    ): Boolean {
        if (previousData == null) return false


        // Check if current total volume is higher than previous best
        val previousBestVolume: Float? = previousData.get("bestVolume") as Float?
        return previousBestVolume != null && result.totalVolume > previousBestVolume
    }

    private fun calculateEstimatedWorkoutTime(exerciseSets: List<List<ExerciseSet>>): Long {
        // Estimate: 45 seconds per set + 90 seconds rest between sets
        var estimatedTime: Long = 0
        for (sets: List<ExerciseSet> in exerciseSets) {
            estimatedTime += (sets.size * (45 + 90) * 1000).toLong() // Convert to milliseconds
        }
        return estimatedTime
    }

    private fun calculateWorkoutIntensity(result: ProgressResult): Float {
        // Intensity based on volume per minute
        if (result.totalDuration > 0) {
            val volumePerMinute: Float = result.totalVolume / (result.totalDuration / 60000f)
            // Normalize to 0-100 scale (assuming 10 kg/min is high intensity)
            return min(100.0, (volumePerMinute * 10f).toDouble()).toFloat()
        }
        return 0f
    }

    private fun calculatePerformanceGrade(result: ProgressResult): String {
        var score: Float = 0f


        // Progress weight: 40%
        score += result.overallProgress * 0.4f


        // Time efficiency weight: 30%
        score += result.metrics.timeEfficiency * 0.3f


        // Volume improvement weight: 20%
        if (result.metrics.volumeImprovement > 0) {
            score += min(20.0f, result.metrics.volumeImprovement * 2f)
        }


        // Consistency weight: 10%
        score += result.metrics.consistencyScore * 0.1f

        if (score >= 95f) return "A+"
        else if (score >= 90f) return "A"
        else if (score >= 85f) return "B+"
        else if (score >= 80f) return "B"
        else if (score >= 75f) return "C+"
        else if (score >= 70f) return "C"
        else return "D"
    }

    private fun generateInsights(result: ProgressResult) {
        val insights: MutableList<String> = result.metrics.insights


        // Progress insights
        if (result.overallProgress == 100f) {
            insights.add("Mükemmel tamamlama oranı! 💯")
        } else if (result.overallProgress >= 80f) {
            insights.add("Yüksek tamamlama oranı 👏")
        }


        // Time insights
        if (result.metrics.timeEfficiency >= 90f) {
            insights.add("Çok verimli zaman kullanımı ⚡")
        } else if (result.metrics.timeEfficiency < 70f) {
            insights.add("Dinlenme sürelerini optimize edebilirsin ⏰")
        }


        // Volume insights
        if (result.metrics.volumeImprovement > 10f) {
            insights.add("%${formatFloat(result.metrics.volumeImprovement, 1)} hacim artışı! 📈")
        } else if (result.metrics.volumeImprovement < -10f) {
            insights.add("Önceki antrenmana göre daha az hacim 📉")
        }


        // Rest time insights
        if (result.metrics.averageRestTime > 120) { // More than 2 minutes
            insights.add("Dinlenme sürelerin uzun, daha yoğun çalışabilirsin 🔥")
        } else if (result.metrics.averageRestTime < 60) { // Less than 1 minute
            insights.add("Kısa dinlenme süreleri, yüksek yoğunluk! 💪")
        }


        // Achievement insights
        if (result.achievements.size > 0) {
            insights.add("${result.achievements.size} yeni başarım kazanıldı! 🏆")
        }
    }

    /**
     * Calculate set-specific progress metrics
     */
    fun calculateSetProgress(set: ExerciseSet, targetSet: ExerciseSet?): Float {
        if (targetSet == null) return if (set.isCompleted) 100f else 0f

        var progress: Float = 0f


        // Reps progress (50% weight)
        if (targetSet.reps > 0) {
            progress += min(
                50.0f,
                (set.reps * 50f) / targetSet.reps
            )
        }


        // Weight progress (50% weight)
        if (targetSet.weight > 0) {
            progress += min(
                50.0f,
                (set.weight * 50f) / targetSet.weight
            )
        }

        return min(100.0, progress.toDouble()).toFloat()
    }

    /**
     * Calculate exercise difficulty score
     */
    fun calculateExerciseDifficulty(sets: List<ExerciseSet>): Float {
        if (sets.isEmpty()) return 0f

        var totalVolume: Float = 0f
        var completedSets: Int = 0

        for (set: ExerciseSet in sets) {
            if (set.isCompleted) {
                totalVolume += set.reps * set.weight
                completedSets++
            }
        }

        if (completedSets == 0) return 0f

        val averageVolume: Float = totalVolume / completedSets


        // Normalize difficulty score (assuming 100 kg volume per set is high difficulty)
        return min(100.0, (averageVolume / 100f * 100f).toDouble()).toFloat()
    }

    /**
     * Generate workout summary
     */
    fun generateWorkoutSummary(result: ProgressResult): String {
        val summary: StringBuilder = StringBuilder()

        summary.append("📊 Antrenman Özeti\n\n")
        summary.append("✅ İlerleme: %${formatFloat(result.overallProgress, 1)} (${result.completedSets}/${result.totalSets} set)\n")
        summary.append("💪 Toplam Hacim: ${formatFloat(result.totalVolume, 1)} kg\n")

        if (result.totalDuration > 0) {
            val minutes: Long = result.totalDuration / 60000
            summary.append("⏱️ Süre: $minutes dakika\n")
        }

        summary.append("⭐ Performance Grade: ${result.metrics.performanceGrade}\n")

        if (result.achievements.size > 0) {
            summary.append("\n🏆 Achievements (${result.achievements.size}):\n")
            for (achievement: Achievement in result.achievements) {
                summary.append("• ${achievement.icon} ${achievement.title}\n")
            }
        }

        if (!result.metrics.insights.isEmpty()) {
            summary.append("\n💡 İçgörüler:\n")
            for (insight: String in result.metrics.insights) {
                summary.append("• $insight\n")
            }
        }

        return summary.toString()
    }

    /**
     * Format float with specified decimal places
     */
    private fun formatFloat(value: Float, decimals: Int): String {
        val multiplier = when (decimals) {
            0 -> 1
            1 -> 10
            2 -> 100
            else -> 100
        }
        val rounded = (value * multiplier).toInt().toFloat() / multiplier
        
        return if (decimals == 0) {
            rounded.toInt().toString()
        } else {
            val intPart = rounded.toInt()
            val fracPart = ((rounded - intPart) * multiplier).toInt()
            "$intPart.${fracPart.toString().padStart(decimals, '0')}"
        }
    }

    /**
     * Progress result container
     */
    class ProgressResult {
        var overallProgress: Float = 0f
        var totalSets: Int = 0
        var completedSets: Int = 0
        var totalVolume: Float = 0f
        var totalDuration: Long = 0
        var exerciseProgresses: MutableList<ExerciseProgress>
        var achievements: MutableList<Achievement>
        var metrics: ProgressMetrics
        var hasPersonalRecord: Boolean = false
        var motivationalMessage: String? = null

        init {
            exerciseProgresses = ArrayList()
            achievements = ArrayList()
            metrics = ProgressMetrics()
        }
    }

    /**
     * Individual exercise progress
     */
    class ExerciseProgress {
        var exerciseName: String? = null
        var exerciseIndex: Int = 0
        var completionPercentage: Float = 0f
        var targetSets: Int = 0
        var completedSets: Int = 0
        var volume: Float = 0f
        var isCompleted: Boolean = false
        var hasPersonalRecord: Boolean = false
        var status: String? = null // "not_started", "in_progress", "completed", "skipped"
        var timeSpent: Long = 0
        var averageRestTime: Float = 0f
        var bestSet: Int = 0 // Index of best performing set
        var improvement: Float = 0f // Compared to previous workout
    }

    /**
     * Achievement data
     */
    class Achievement {
        var id: String? = null
        var title: String? = null
        var description: String? = null
        var icon: String? = null
        var type: AchievementType? = null
        var earnedAt: Long? = null // epoch milliseconds
        var isNew: Boolean = false

        enum class AchievementType {
            PERSONAL_RECORD, VOLUME_MILESTONE, CONSISTENCY, TIME_EFFICIENCY, COMPLETION_RATE
        }
    }

    /**
     * Progress metrics
     */
    class ProgressMetrics {
        var timeEfficiency: Float = 0f // 0-100%
        var volumeImprovement: Float = 0f // Compared to previous session
        var consistencyScore: Float = 0f // Based on completion rates
        var streakCount: Int = 0 // Consecutive completed workouts
        var averageRestTime: Float = 0f
        var workoutIntensity: Float = 0f // Based on weight/reps progression
        var performanceGrade: String? = null // A+, A, B+, B, C+, C, D
        var insights: MutableList<String>

        init {
            insights = ArrayList()
        }
    }
}
