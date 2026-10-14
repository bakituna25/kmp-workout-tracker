package com.example.aigymapp.repository

import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.Day_progress
import com.example.aigymapp.database.DayCards
import com.example.aigymapp.database.DayExerciseSummary
import com.example.aigymapp.database.asFlowList
import com.example.aigymapp.database.asFlowOneOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

/**
 * Progress takibi için Repository
 * Checkbox tabanlı ilerleme sistemi ve takvim entegrasyonu
 * Refactored for Kotlin Multiplatform with SQLDelight
 */
class ProgressRepository(
    private val database: AppDatabase,
    private val schedulingRepository: SchedulingRepository? = null
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Debounce için delay
    private var recomputeJob: Job? = null
    private val DEBOUNCE_DELAY = 150L

    /**
     * Set checkbox toggle edildiğinde çağrılır
     */
    suspend fun onSetCheckboxToggled(
        planItemId: Long,
        setIndex: Long,
        date: String,
        checked: Boolean,
        repsCompleted: Long? = null,
        weightUsed: Double? = null
    ) {
        if (checked) {
            database.workoutSetTickQueries.insertOrUpdateSetTick(
                plan_item_id = planItemId,
                set_index = setIndex,
                date = date,
                is_checked = 1,
                reps_completed = repsCompleted,
                weight_used = weightUsed,
                ticked_at = currentTimeMillis()
            )
        } else {
            database.workoutSetTickQueries.deleteSetTick(planItemId, setIndex, date)
        }
        
        // Günlük progress'i yeniden hesapla (debounced)
        debouncedRecomputeDay(date)
    }

    /**
     * Debounced günlük progress hesaplama
     */
    private fun debouncedRecomputeDay(date: String) {
        recomputeJob?.cancel()
        recomputeJob = scope.launch {
            delay(DEBOUNCE_DELAY)
            recomputeDay(date)
        }
    }

    /**
     * Aylık progress verilerini observe et (Takvim için)
     */
    fun observeMonthProgress(startDate: String, endDate: String): Flow<List<Day_progress>> {
        println("📡 ProgressRepository: observeMonthProgress Flow başlatıldı - $startDate -> $endDate")
        return database.dayProgressQueries.observeProgressRange(startDate, endDate).asFlowList()
    }

    /**
     * Belirli bir tarih aralığı için progress verilerini al
     */
    suspend fun getProgressRange(startDate: String, endDate: String): List<Day_progress> {
        return withContext(Dispatchers.Default) {
            database.dayProgressQueries.getProgressRange(startDate, endDate).executeAsList()
        }
    }
    
    /**
     * Get day progress details for a specific date (backward compatibility)
     */
    suspend fun getDayProgressDetails(date: String): Day_progress? {
        return withContext(Dispatchers.Default) {
            database.dayProgressQueries.getDayProgress(date).executeAsOneOrNull()
        }
    }
    
    /**
     * Get total workout duration for a date (returns milliseconds)
     */
    suspend fun getDayTotalDurationSeconds(date: String): Long {
        return withContext(Dispatchers.Default) {
            // Önce scheduled_workouts'tan al (yeni sistem)
            if (schedulingRepository != null) {
                try {
                    // LocalDate'e çevir ve epoch day'e çevir
                    val localDate = kotlinx.datetime.LocalDate.parse(date)
                    val epochDay = localDate.toEpochDays().toLong()
                    val result = schedulingRepository.getTotalElapsedTimeForDate(epochDay)
                    println("ProgressRepository.getDayTotalDurationSeconds($date) = $result ms (from scheduled_workouts)")
                    return@withContext result
                } catch (e: Exception) {
                    println("⚠️ ProgressRepository: scheduled_workouts'tan süre alınamadı: ${e.message}")
                }
            }
            
            // Fallback: Eski sistemden al
            val result = database.dayProgressQueries.getTotalWorkoutDurationMsForDate(date).executeAsOneOrNull() ?: 0L
            println("ProgressRepository.getDayTotalDurationSeconds($date) = $result ms (from day_progress - fallback)")
            result
        }
    }

    /**
     * Gün başına planlanan antrenman sayıları (grafik için)
     */
    suspend fun getDailyWorkoutCounts(startDate: String, endDate: String): Map<String, Long> {
        return withContext(Dispatchers.Default) {
            database.dayProgressQueries.getDailyWorkoutCounts(startDate, endDate)
                .executeAsList()
                .associate { it.date to it.count }
        }
    }

    /**
     * Kas grubu dağılımı (label->count)
     */
    suspend fun getMuscleGroupCounts(startDate: String, endDate: String): Map<String, Long> {
        return withContext(Dispatchers.Default) {
            database.dayProgressQueries.getMuscleGroupCounts(startDate, endDate)
                .executeAsList()
                .associate { (it.muscle_group ?: "Diğer") to it.count }
        }
    }

    /**
     * Belirli bir gün için progress detaylarını al
     */
    suspend fun getDayProgress(date: String): Day_progress? {
        return withContext(Dispatchers.Default) {
            database.dayProgressQueries.getDayProgress(date).executeAsOneOrNull()
        }
    }

    /**
     * Progress card'ları için planner-style veri
     */
    suspend fun getDayCards(date: String): List<DayCards> {
        return withContext(Dispatchers.Default) {
            val result = database.dayProgressQueries.dayCards(date, date).executeAsList()
            println("ProgressRepository.getDayCards($date) = ${result.size} cards")
            result.forEach { card ->
                println("  - ${card.exercise_name}: ${card.completed_sets}/${card.planned_sets} sets")
            }
            result
        }
    }
    
    /**
     * Generic recompute day (herhangi bir tarih için)
     */
    suspend fun recomputeDay(date: String) {
        withContext(Dispatchers.Default) {
            println("ProgressRepository.recomputeDay($date) başladı")
            
            // Eğer o gün için plan yoksa, NO_PLAN olarak kaydet
            val hasPlan = database.scheduledWorkoutQueries.hasWorkoutPlanForDate(date).executeAsOne()
            println("  hasPlan = $hasPlan")
            
            if (!hasPlan) {
                println("  -> Plan yok, NO_PLAN kaydediliyor")
                database.dayProgressQueries.insertOrUpdateDayProgress(
                    date = date,
                    planned_sets = 0,
                    completed_sets = 0,
                    completion_percent = 0.0,
                    status = "NO_PLAN",
                    updated_at = currentTimeMillis()
                )
                return@withContext
            }
            
            val plannedSets = database.workoutPlanSetQueries.getPlannedSetsCount(date).executeAsOne()
            val completedSets = database.workoutSetTickQueries.getCompletedSetsCount(date, date).executeAsOne()
            
            println("  plannedSets = $plannedSets, completedSets = $completedSets")
            
            val completionPercent = if (plannedSets > 0) {
                (completedSets.toDouble() / plannedSets.toDouble()) * 100.0
            } else 0.0
            
            val status = when {
                plannedSets == 0L -> "NO_PLAN"
                completedSets >= plannedSets && plannedSets > 0 -> "FULL"
                completedSets > 0 -> "PARTIAL"
                else -> "NONE"
            }
            
            println("  completionPercent = $completionPercent, status = $status")
            
            database.dayProgressQueries.insertOrUpdateDayProgress(
                date = date,
                planned_sets = plannedSets,
                completed_sets = completedSets,
                completion_percent = completionPercent,
                status = status,
                updated_at = currentTimeMillis()
            )
            
            println("✅ ProgressRepository.recomputeDay($date) tamamlandı - Status: $status, Completed: $completedSets/$plannedSets")
        }
    }

    /**
     * Seçili günün egzersiz listesini al
     */
    suspend fun getDayExerciseSummary(date: String): List<DayExerciseSummary> {
        return withContext(Dispatchers.Default) {
            val epochDay = kotlinx.datetime.LocalDate.parse(date).toEpochDays().toLong()
            database.dayProgressQueries.dayExerciseSummary(date, epochDay).executeAsList()
        }
    }

    /**
     * WorkoutPlannerItem için planlanan setleri oluştur
     */
    suspend fun createPlanSetsForItem(planItemId: Long, setCount: Int, reps: Int, weight: Double) {
        withContext(Dispatchers.Default) {
            repeat(setCount) { setIndex ->
                database.workoutPlanSetQueries.insertWorkoutPlanSets(
                    plan_item_id = planItemId,
                    set_index = setIndex.toLong(),
                    planned_reps = reps.toLong(),
                    planned_weight = weight,
                    created_at = currentTimeMillis()
                )
            }
        }
    }

    /**
     * WorkoutPlannerItem silindiğinde set kayıtlarını temizle
     */
    suspend fun cleanupPlanSetsForItem(planItemId: Long) {
        withContext(Dispatchers.Default) {
            database.workoutPlanSetQueries.deletePlanSetsForItem(planItemId)
        }
    }

    /**
     * Belirli bir tarih için WorkoutDayPlan oluştur
     */
    suspend fun createWorkoutDayPlan(date: String, exerciseCount: Long) {
        withContext(Dispatchers.Default) {
            database.workoutDayPlanQueries.insertWorkoutDayPlan(
                date = date,
                planned_exercise_count = exerciseCount,
                created_at = currentTimeMillis()
            )
        }
    }

    /**
     * Belirli bir tarih için plan var mı kontrol et
     */
    suspend fun dayPlanExists(date: String): Boolean {
        return withContext(Dispatchers.Default) {
            database.workoutDayPlanQueries.dayPlanExists(date).executeAsOne() > 0
        }
    }
    
    /**
     * Planner'dan workout kaydedilirken çağrılır
     */
    suspend fun ensureDayPlanExists(date: String, itemsCount: Long) {
        if (!dayPlanExists(date)) {
            createWorkoutDayPlan(date, itemsCount)
        }
    }

    /**
     * Orphan scheduled_workouts kayıtlarını temizle
     */
    suspend fun cleanupOrphanScheduledWorkouts() {
        schedulingRepository?.cleanupOrphanScheduledWorkouts()
    }
    
    /**
     * Orphan day_progress kayıtlarını temizle
     * (scheduled_workouts'ta olmayan ama day_progress'te olan kayıtlar)
     */
    suspend fun cleanupOrphanDayProgress() {
        withContext(Dispatchers.Default) {
            try {
                database.dayProgressQueries.deleteOrphanDayProgress()
                println("✅ ProgressRepository: Orphan day_progress kayıtları temizlendi")
            } catch (e: Exception) {
                println("❌ ProgressRepository: Orphan day_progress cleanup hatası: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Repository'yi temizle
     */
    fun cleanup() {
        scope.cancel()
    }

    // Platform-agnostic time function
    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}

