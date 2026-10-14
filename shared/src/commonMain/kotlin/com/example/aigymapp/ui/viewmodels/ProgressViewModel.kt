package com.example.aigymapp.ui.viewmodels

import androidx.compose.runtime.*
import com.example.aigymapp.database.Day_progress
import com.example.aigymapp.repository.ProgressRepository
import com.example.aigymapp.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Zaman periyodu seçimi için enum
 */
enum class TimePeriod {
    WEEKLY,    // Haftalık
    MONTHLY,   // Aylık
    YEARLY     // Yıllık
}

/**
 * Progress ekranı için ViewModel
 * Takvim ve istatistikleri yönetir
 */
class ProgressViewModel(
    private val progressRepository: ProgressRepository,
    private val workoutRepository: WorkoutRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Flow collection job - eski flow'u iptal etmek için
    private var monthFlowJob: kotlinx.coroutines.Job? = null
    
    // Seçili ay ve yıl
    private val _selectedMonth = MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
    val selectedMonth: StateFlow<LocalDateTime> = _selectedMonth.asStateFlow()
    
    // Aylık progress verileri
    private val _monthProgress = MutableStateFlow<List<Day_progress>>(emptyList())
    val monthProgress: StateFlow<List<Day_progress>> = _monthProgress.asStateFlow()
    
    // İstatistikler
    private val _totalWorkouts = MutableStateFlow(0)
    val totalWorkouts: StateFlow<Int> = _totalWorkouts.asStateFlow()
    
    private val _completedWorkouts = MutableStateFlow(0)
    val completedWorkouts: StateFlow<Int> = _completedWorkouts.asStateFlow()
    
    private val _totalSets = MutableStateFlow(0L)
    val totalSets: StateFlow<Long> = _totalSets.asStateFlow()
    
    private val _totalPlannedSets = MutableStateFlow(0L)
    val totalPlannedSets: StateFlow<Long> = _totalPlannedSets.asStateFlow()
    
    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()
    
    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Seçili gün
    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()
    
    // Seçili günün detaylı verileri
    private val _selectedDayMuscleGroups = MutableStateFlow<List<String>>(emptyList())
    val selectedDayMuscleGroups: StateFlow<List<String>> = _selectedDayMuscleGroups.asStateFlow()
    
    private val _selectedDayTotalSets = MutableStateFlow(0L)
    val selectedDayTotalSets: StateFlow<Long> = _selectedDayTotalSets.asStateFlow()
    
    private val _selectedDayDuration = MutableStateFlow(0L)
    val selectedDayDuration: StateFlow<Long> = _selectedDayDuration.asStateFlow()
    
    private val _selectedDayCalories = MutableStateFlow(0)
    val selectedDayCalories: StateFlow<Int> = _selectedDayCalories.asStateFlow()
    
    // Pop-up için egzersiz detayları (Gerçek veritabanı verisi - NO MOCK!)
    data class ExerciseDetail(
        val name: String,
        val completedSets: Long,
        val plannedSets: Long,
        val reps: Long,
        val weightKg: Double
    )
    
    private val _selectedDayExerciseDetails = MutableStateFlow<List<ExerciseDetail>>(emptyList())
    val selectedDayExerciseDetails: StateFlow<List<ExerciseDetail>> = _selectedDayExerciseDetails.asStateFlow()
    
    // Pop-up görünürlük durumu
    private val _showExerciseDialog = MutableStateFlow(false)
    val showExerciseDialog: StateFlow<Boolean> = _showExerciseDialog.asStateFlow()
    
    // İstatistikler sekmesi için aylık veriler
    private val _monthlyDailyWorkoutCounts = MutableStateFlow<Map<String, Long>>(emptyMap())
    val monthlyDailyWorkoutCounts: StateFlow<Map<String, Long>> = _monthlyDailyWorkoutCounts.asStateFlow()
    
    private val _muscleGroupDistribution = MutableStateFlow<Map<String, Long>>(emptyMap())
    val muscleGroupDistribution: StateFlow<Map<String, Long>> = _muscleGroupDistribution.asStateFlow()
    
    private val _monthDateRangeText = MutableStateFlow("")
    val monthDateRangeText: StateFlow<String> = _monthDateRangeText.asStateFlow()
    
    // Period seçimi (Haftalık/Aylık/Yıllık)
    private val _selectedPeriod = MutableStateFlow(TimePeriod.WEEKLY)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()
    
    init {
        loadCurrentMonth()
        
        // Bugünü otomatik seç
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val todayDay = today.dayOfMonth
        println("📅 ProgressViewModel INIT: Bugünün günü otomatik seçiliyor - $todayDay")
        selectDay(todayDay)
        
        // Tüm ay için progress'i yeniden hesapla (ilk açılışta)
        scope.launch {
            // Önce orphan kayıtları temizle
            cleanupOrphanRecords()
            recomputeAllDaysInCurrentMonth()
            
            // İstatistikler için başlangıçta haftalık verileri hesapla
            calculateWeeklyStatistics()
        }
    }
    
    /**
     * Orphan (sahipsiz) kayıtları temizle
     * 1. Silinmiş plan item'larına ait scheduled_workouts kayıtlarını sil
     * 2. Plan olmayan günlerin eski day_progress kayıtlarını sil
     */
    private suspend fun cleanupOrphanRecords() {
        try {
            println("🧹 ProgressViewModel: Orphan kayıtlar temizleniyor...")
            
            // 1. Orphan scheduled_workouts'ları temizle
            progressRepository.cleanupOrphanScheduledWorkouts()
            
            // 2. Orphan day_progress kayıtlarını temizle (eski simple tasarımdan kalanlar)
            progressRepository.cleanupOrphanDayProgress()
            
            println("✅ ProgressViewModel: Orphan kayıtlar temizlendi")
        } catch (e: Exception) {
            println("⚠️ ProgressViewModel: Orphan temizleme hatası: ${e.message}")
        }
    }
    
    /**
     * Bir önceki aya geç
     */
    fun previousMonth() {
        val current = _selectedMonth.value
        val newMonth = if (current.monthNumber == 1) {
            LocalDateTime(current.year - 1, 12, 1, 0, 0)
        } else {
            LocalDateTime(current.year, current.monthNumber - 1, 1, 0, 0)
        }
        _selectedMonth.value = newMonth
        loadCurrentMonth()
    }
    
    /**
     * Bir sonraki aya geç
     */
    fun nextMonth() {
        val current = _selectedMonth.value
        val newMonth = if (current.monthNumber == 12) {
            LocalDateTime(current.year + 1, 1, 1, 0, 0)
        } else {
            LocalDateTime(current.year, current.monthNumber + 1, 1, 0, 0)
        }
        _selectedMonth.value = newMonth
        loadCurrentMonth()
    }
    
    /**
     * Seçili ayın verilerini yükle (Reactive Flow ile)
     */
    private fun loadCurrentMonth() {
        // Eski flow'u iptal et
        monthFlowJob?.cancel()
        
        // Yeni flow başlat
        monthFlowJob = scope.launch {
            _isLoading.value = true
            try {
                val current = _selectedMonth.value
                val startDate = LocalDate(current.year, current.month, 1)
                val endDate = startDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                
                println("📅 ProgressViewModel: Ay yükleniyor (Reactive) - $startDate -> $endDate")
                
                // Reactive Flow ile progress verilerini dinle
                progressRepository.observeMonthProgress(
                    startDate.toString(),
                    endDate.toString()
                ).collect { progressData ->
                    println("📊 ProgressViewModel: Progress data güncellendi - ${progressData.size} gün")
                    
                    _monthProgress.value = progressData
                    
                    // İstatistikleri hesapla
                    calculateStats(startDate.toString(), endDate.toString(), progressData)
                    
                    _isLoading.value = false
                    
                    // Eğer bir gün seçiliyse, onun detaylarını da güncelle
                    _selectedDay.value?.let { day ->
                        println("🔄 ProgressViewModel: Seçili gün ($day) yeniden yükleniyor...")
                        loadDayDetails(day)
                    }
                }
                
            } catch (e: Exception) {
                println("❌ ProgressViewModel: Veri yüklenirken hata: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }
    
    /**
     * İstatistikleri hesapla
     */
    private suspend fun calculateStats(startDate: String, endDate: String, progressData: List<Day_progress>) {
        try {
            // Toplam planlanan antrenman sayısı (NO_PLAN olmayanlar)
            val totalWorkoutsCount = progressData.count { it.status != "NO_PLAN" }
            _totalWorkouts.value = totalWorkoutsCount
            
            // Tamamlanan antrenman sayısı (FULL ve PARTIAL)
            val completedWorkoutsCount = progressData.count { 
                it.status == "FULL" || it.status == "PARTIAL"
            }
            _completedWorkouts.value = completedWorkoutsCount
            
            // Toplam tamamlanan set sayısı
            val totalSetsCount = progressData.sumOf { it.completed_sets }
            _totalSets.value = totalSetsCount
            
            // Toplam planlanan set sayısı
            val totalPlannedSetsCount = progressData.sumOf { it.planned_sets }
            _totalPlannedSets.value = totalPlannedSetsCount
            
            // Toplam süre hesaplama (her gün için ayrı ayrı çek)
            var totalDurationMs = 0L
            progressData.forEach { dayProgress ->
                if (dayProgress.status != "NO_PLAN") {
                    val duration = progressRepository.getDayTotalDurationSeconds(dayProgress.date)
                    totalDurationMs += duration
                }
            }
            _totalDuration.value = totalDurationMs
            
            // Kalori tahmini (basit formula: completed_sets * 15 kalori)
            val estimatedCalories = (totalSetsCount * 15).toInt()
            _totalCalories.value = estimatedCalories
            
            // İstatistikler sekmesi için seçili periyodun verilerini hesapla
            when (_selectedPeriod.value) {
                TimePeriod.WEEKLY -> calculateWeeklyStatistics()
                TimePeriod.MONTHLY -> calculateMonthlyStatistics()
                TimePeriod.YEARLY -> calculateYearlyStatistics()
            }
            
        } catch (e: Exception) {
            println("ProgressViewModel: İstatistik hesaplama hatası: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * İstatistikler sekmesi için aylık verileri hesapla
     */
    private suspend fun calculateMonthlyStatistics() {
        try {
            val current = _selectedMonth.value
            val startDate = LocalDate(current.year, current.month, 1)
            val endDate = startDate.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            
            println("📊 ProgressViewModel: Aylık istatistikler hesaplanıyor - $startDate -> $endDate")
            
            // Günlük antrenman sayıları
            val dailyCounts = progressRepository.getDailyWorkoutCounts(
                startDate.toString(),
                endDate.toString()
            )
            _monthlyDailyWorkoutCounts.value = dailyCounts
            println("  Günlük sayılar: ${dailyCounts.size} gün")
            
            // Kas grubu dağılımı
            val muscleGroups = progressRepository.getMuscleGroupCounts(
                startDate.toString(),
                endDate.toString()
            )
            // Türkçe'ye çevir
            val translatedMuscleGroups = muscleGroups.mapKeys { (key, _) ->
                translateMuscleGroup(key)
            }
            _muscleGroupDistribution.value = translatedMuscleGroups
            println("  Kas grupları: ${translatedMuscleGroups.keys.joinToString()}")
            
            // Tarih aralığı text'i
            val monthNames = listOf(
                "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
            )
            val monthName = monthNames[current.monthNumber - 1]
            _monthDateRangeText.value = "$monthName ${current.year}"
            
            println("✅ ProgressViewModel: Aylık istatistikler hesaplandı")
            
        } catch (e: Exception) {
            println("❌ ProgressViewModel: Aylık istatistik hesaplama hatası: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Haftalık istatistikleri hesapla
     */
    private suspend fun calculateWeeklyStatistics() {
        try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekStart = getWeekStart(today)
            val weekEnd = getWeekEnd(today)
            
            println("📊 ProgressViewModel: Haftalık istatistikler hesaplanıyor - $weekStart -> $weekEnd")
            
            // Günlük antrenman sayıları
            val dailyCounts = progressRepository.getDailyWorkoutCounts(
                weekStart.toString(),
                weekEnd.toString()
            )
            _monthlyDailyWorkoutCounts.value = dailyCounts
            println("  Günlük sayılar: ${dailyCounts.size} gün")
            
            // Kas grubu dağılımı
            val muscleGroups = progressRepository.getMuscleGroupCounts(
                weekStart.toString(),
                weekEnd.toString()
            )
            // Türkçe'ye çevir
            val translatedMuscleGroups = muscleGroups.mapKeys { (key, _) ->
                translateMuscleGroup(key)
            }
            _muscleGroupDistribution.value = translatedMuscleGroups
            println("  Kas grupları: ${translatedMuscleGroups.keys.joinToString()}")
            
            // Tarih aralığı text'i: "1-7 Aralık 2025"
            _monthDateRangeText.value = formatWeekRange(weekStart, weekEnd)
            
            println("✅ ProgressViewModel: Haftalık istatistikler hesaplandı")
            
        } catch (e: Exception) {
            println("❌ ProgressViewModel: Haftalık istatistik hesaplama hatası: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Yıllık istatistikleri hesapla
     */
    private suspend fun calculateYearlyStatistics() {
        try {
            val current = _selectedMonth.value
            val startDate = LocalDate(current.year, 1, 1)
            val endDate = LocalDate(current.year, 12, 31)
            
            println("📊 ProgressViewModel: Yıllık istatistikler hesaplanıyor - $startDate -> $endDate")
            
            // Aylık antrenman sayıları (12 ay)
            val dailyCounts = progressRepository.getDailyWorkoutCounts(
                startDate.toString(),
                endDate.toString()
            )
            _monthlyDailyWorkoutCounts.value = dailyCounts
            println("  Günlük sayılar: ${dailyCounts.size} gün")
            
            // Kas grubu dağılımı
            val muscleGroups = progressRepository.getMuscleGroupCounts(
                startDate.toString(),
                endDate.toString()
            )
            // Türkçe'ye çevir
            val translatedMuscleGroups = muscleGroups.mapKeys { (key, _) ->
                translateMuscleGroup(key)
            }
            _muscleGroupDistribution.value = translatedMuscleGroups
            println("  Kas grupları: ${translatedMuscleGroups.keys.joinToString()}")
            
            // Tarih aralığı text'i: "2025"
            _monthDateRangeText.value = "${current.year}"
            
            println("✅ ProgressViewModel: Yıllık istatistikler hesaplandı")
            
        } catch (e: Exception) {
            println("❌ ProgressViewModel: Yıllık istatistik hesaplama hatası: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Period değiştir (Hafta/Ay/Yıl)
     */
    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        scope.launch {
            when (period) {
                TimePeriod.WEEKLY -> calculateWeeklyStatistics()
                TimePeriod.MONTHLY -> calculateMonthlyStatistics()
                TimePeriod.YEARLY -> calculateYearlyStatistics()
            }
        }
    }
    
    /**
     * Haftanın başlangıcını hesapla (Pazartesi)
     */
    private fun getWeekStart(date: LocalDate): LocalDate {
        val dayOfWeek = date.dayOfWeek.ordinal // 0 = Monday, 6 = Sunday
        return date.minus(dayOfWeek, DateTimeUnit.DAY)
    }
    
    /**
     * Haftanın bitişini hesapla (Pazar)
     */
    private fun getWeekEnd(date: LocalDate): LocalDate {
        return getWeekStart(date).plus(6, DateTimeUnit.DAY)
    }
    
    /**
     * Haftalık tarih aralığını formatla: "1-7 Aralık 2025"
     */
    private fun formatWeekRange(start: LocalDate, end: LocalDate): String {
        val monthNames = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        val monthName = monthNames[start.monthNumber - 1]
        return "${start.dayOfMonth}-${end.dayOfMonth} $monthName ${start.year}"
    }
    
    /**
     * Belirli bir gün için progress durumunu al
     */
    fun getDayStatus(day: Int): String? {
        val current = _selectedMonth.value
        val date = LocalDate(current.year, current.month, day).toString()
        val progress = _monthProgress.value.find { it.date == date }
        return progress?.status
    }
    
    /**
     * Belirli bir gün için tamamlanma yüzdesini al
     */
    fun getDayCompletionPercent(day: Int): Double {
        val current = _selectedMonth.value
        val date = LocalDate(current.year, current.month, day).toString()
        val progress = _monthProgress.value.find { it.date == date }
        return progress?.completion_percent ?: 0.0
    }
    
    /**
     * Süreyi formatlı string olarak döndür (dakika)
     */
    fun getFormattedDuration(): String {
        val durationMinutes = (_totalDuration.value / 1000 / 60).toInt()
        return "${durationMinutes}dk"
    }
    
    /**
     * Belirli bir gün için tamamlanan antrenman sayısını al (ısı haritası için)
     */
    fun getDayWorkoutCount(day: Int): Int {
        val current = _selectedMonth.value
        val date = LocalDate(current.year, current.month, day).toString()
        val progress = _monthProgress.value.find { it.date == date }
        
        // SADECE gerçekten set tamamlanmışsa say
        // "NONE" status'ü (plan var ama hiç yapılmamış) = 0 workout count
        return if (progress != null && progress.completed_sets > 0) {
            (progress.completed_sets / 4).toInt().coerceIn(1, 5) // En az 1
        } else {
            0
        }
    }
    
    /**
     * Isı haritası rengi al (0-5 arası yoğunluk)
     * 0: Gri (antrenman yok)
     * 1-2: Açık yeşil (az antrenman)
     * 3-4: Orta yeşil (orta antrenman)
     * 5: Koyu yeşil/Neon (yüksek antrenman)
     */
    fun getHeatmapColor(day: Int): androidx.compose.ui.graphics.Color {
        val count = getDayWorkoutCount(day)
        return when (count) {
            0 -> androidx.compose.ui.graphics.Color(0xFF2C2C2C) // Gri
            1 -> androidx.compose.ui.graphics.Color(0xFF4A5A3F) // Çok açık yeşil
            2 -> androidx.compose.ui.graphics.Color(0xFF6B8A4F) // Açık yeşil
            3 -> androidx.compose.ui.graphics.Color(0xFF8BBD5F) // Orta yeşil
            4 -> androidx.compose.ui.graphics.Color(0xFFAAD576) // Koyu yeşil
            else -> androidx.compose.ui.graphics.Color(0xFFCCFF00) // Neon (Primary color)
        }
    }
    
    /**
     * Gün seç ve detaylarını yükle
     */
    fun selectDay(day: Int) {
        _selectedDay.value = day
        // Önceki günün verisini hemen temizle - modal senkron açılıyor,
        // yoksa bir önceki günün egzersizleri kısa süreliğine görünür.
        _selectedDayExerciseDetails.value = emptyList()
        _selectedDayMuscleGroups.value = emptyList()
        loadDayDetails(day)
    }
    
    /**
     * Seçili günün detaylarını yükle
     */
    private fun loadDayDetails(day: Int) {
        scope.launch {
            try {
                val current = _selectedMonth.value
                val date = LocalDate(current.year, current.month, day).toString()
                
                println("📅 ProgressViewModel: Gün detayları yükleniyor - Tarih: $date")
                
                // Günün workout kartlarını al
                val dayCards = progressRepository.getDayCards(date)
                
                println("📊 ProgressViewModel: ${dayCards.size} workout kartı bulundu")
                
                if (dayCards.isNotEmpty()) {
                    // Kas gruplarını topla (unique ve Türkçe'ye çevir)
                    val muscleGroups = dayCards
                        .mapNotNull { card -> 
                            val group = card.muscle_group
                            println("  - Kas grubu: $group")
                            group?.let { translateMuscleGroup(it) }
                        }
                        .distinct()
                    
                    println("🎯 ProgressViewModel: Çevrilen kas grupları: $muscleGroups")
                    _selectedDayMuscleGroups.value = muscleGroups
                    
                    // EGZERSİZ DETAYLARINI ÇIKAR (GERÇEK VERİ - NO MOCK!)
                    val exerciseDetails = dayCards.map { card ->
                        ExerciseDetail(
                            name = card.exercise_name,
                            completedSets = card.completed_sets,
                            plannedSets = card.planned_sets,
                            reps = card.avg_reps,
                            weightKg = card.planned_weight_kg ?: 0.0
                        )
                    }
                    _selectedDayExerciseDetails.value = exerciseDetails
                    println("🏋️ ProgressViewModel: ${exerciseDetails.size} egzersiz detayı yüklendi")
                    
                    // Tamamlanan set sayısı
                    val completedSets = dayCards.sumOf { it.completed_sets }
                    _selectedDayTotalSets.value = completedSets
                    println("💪 ProgressViewModel: Tamamlanan set: $completedSets")
                    
                    // Süre
                    val duration = progressRepository.getDayTotalDurationSeconds(date)
                    _selectedDayDuration.value = duration
                    println("⏱️ ProgressViewModel: Süre: ${duration}ms")
                    
                    // Kalori (set başına 15 kalori tahmini)
                    val calories = (completedSets * 15).toInt()
                    _selectedDayCalories.value = calories
                    println("🔥 ProgressViewModel: Kalori: $calories kcal")
                } else {
                    println("⚠️ ProgressViewModel: Bu gün için workout yok")
                    // Veri yoksa sıfırla
                    _selectedDayTotalSets.value = 0
                    _selectedDayDuration.value = 0
                    _selectedDayCalories.value = 0
                    _selectedDayMuscleGroups.value = emptyList()
                    _selectedDayExerciseDetails.value = emptyList()
                }
                
            } catch (e: Exception) {
                println("❌ ProgressViewModel: Gün detayları yüklenirken hata: ${e.message}")
                e.printStackTrace()
                
                // Hata durumunda sıfırla
                _selectedDayTotalSets.value = 0
                _selectedDayDuration.value = 0
                _selectedDayCalories.value = 0
                _selectedDayMuscleGroups.value = emptyList()
                _selectedDayExerciseDetails.value = emptyList()
            }
        }
    }
    
    /**
     * Seçili günün süresini formatla
     */
    fun getSelectedDayFormattedDuration(): String {
        val durationMinutes = (_selectedDayDuration.value / 1000 / 60).toInt()
        return if (durationMinutes > 0) {
            "${durationMinutes}dk"
        } else {
            "00:00"
        }
    }
    
    /**
     * Seçili günün süresini güncelle (WorkoutViewModel'den çağrılır - timer için)
     */
    fun updateSelectedDayDuration(durationMs: Long) {
        _selectedDayDuration.value = durationMs
    }
    
    /**
     * Pop-up aç (Egzersiz listesi göster)
     * NOT: UI tarafında hasWorkout kontrolü yapılıyor, burada kontrol yok
     */
    fun openExerciseDialog() {
        _showExerciseDialog.value = true
        println("✅ ProgressViewModel: Pop-up açıldı")
    }
    
    /**
     * Pop-up kapat
     */
    fun closeExerciseDialog() {
        _showExerciseDialog.value = false
        println("❌ ProgressViewModel: Pop-up kapatıldı")
    }
    
    /**
     * Kas grubu adını Türkçe'ye çevir
     */
    private fun translateMuscleGroup(muscleGroup: String): String {
        // Önce trim ve uppercase yap
        val normalized = muscleGroup.trim().uppercase()
        
        return when {
            normalized.contains("CHEST") || normalized.contains("GÖĞÜS") || normalized.contains("GÖGÜS") -> "Göğüs"
            normalized.contains("BACK") || normalized.contains("SIRT") -> "Sırt"
            normalized.contains("SHOULDER") || normalized.contains("OMUZ") -> "Omuz"
            normalized.contains("ARM") || normalized.contains("KOL") || normalized.contains("BICEPS") || normalized.contains("TRICEPS") -> "Kol"
            normalized.contains("LEG") || normalized.contains("BACAK") || normalized.contains("QUAD") || normalized.contains("HAMSTRING") -> "Bacak"
            normalized.contains("CORE") || normalized.contains("ABS") || normalized.contains("KARIN") -> "Karın"
            normalized.contains("CARDIO") || normalized.contains("KARDİYO") -> "Kardiyo"
            normalized.contains("FULL") || normalized.contains("TÜM") || normalized.contains("TUM") -> "Tüm Vücut"
            else -> muscleGroup // Orijinal halini döndür
        }
    }
    
    /**
     * Manuel refresh - Workout değişikliklerinden sonra çağrılır
     */
    fun refreshCurrentMonth() {
        println("🔄 ProgressViewModel: Manuel refresh tetiklendi")
        
        // Eski flow'u iptal et ve yeni flow başlat
        monthFlowJob?.cancel()
        
        // Mevcut ayı yeniden yükle
        loadCurrentMonth()
        
        // Progress'i yeniden hesapla (önce orphan temizle!)
        scope.launch {
            // ÖNCE orphan kayıtları temizle
            cleanupOrphanRecords()
            
            // SONRA recompute yap
            recomputeAllDaysInCurrentMonth()
            
            // İstatistikler için aylık verileri de yeniden hesapla
            calculateMonthlyStatistics()
        }
    }
    
    /**
     * Mevcut aydaki tüm günler için progress'i yeniden hesapla
     * NOT: Orphan kayıtlar temizlendikten SONRA çağrılmalı!
     */
    private suspend fun recomputeAllDaysInCurrentMonth() {
        try {
            val current = _selectedMonth.value
            val startDate = LocalDate(current.year, current.month, 1)
            
            // Ayın kaç günü olduğunu bul
            val daysInMonth = when (current.monthNumber) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (current.year % 4 == 0 && (current.year % 100 != 0 || current.year % 400 == 0)) 29 else 28
                else -> 30
            }
            
            println("🔄 ProgressViewModel: ${current.month} ${current.year} için tüm günler yeniden hesaplanıyor...")
            println("   Toplam $daysInMonth gün işlenecek...")
            
            // Her gün için recompute (plan varsa durumunu güncelle, yoksa NO_PLAN yaz)
            for (day in 1..daysInMonth) {
                val date = LocalDate(current.year, current.month, day).toString()
                println("   📅 $date recompute ediliyor...")
                progressRepository.recomputeDay(date)
            }
            
            println("✅ ProgressViewModel: Tüm günler yeniden hesaplandı")
            
        } catch (e: Exception) {
            println("❌ ProgressViewModel: Recompute hatası - ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Cleanup
     */
    fun cleanup() {
        monthFlowJob?.cancel()
        // Scope otomatik olarak temizlenecek
    }
}
