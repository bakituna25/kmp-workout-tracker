package com.example.aigymapp.ui.viewmodels

import com.example.aigymapp.database.Exercises
import com.example.aigymapp.database.GetScheduledForDate
import com.example.aigymapp.repository.AppRepository
import com.example.aigymapp.repository.ProgressRepository
import com.example.aigymapp.repository.SchedulingRepository
import com.example.aigymapp.repository.WorkoutPlannerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

// Bir antrenman silindikten sonra "GERİ AL" çubuğunun açık kaldığı süre —
// bu süre içinde geri al'a basılmazsa silme veritabanına kalıcı olarak yazılır.
private const val PENDING_DELETE_UNDO_WINDOW_MS = 4000L

/**
 * Wizard Step Enum - Add Workout akışının adımları
 */
enum class WizardStep {
    CATEGORY,    // Step 1: Select muscle group
    EXERCISE,    // Step 2: Select exercise
    CONFIG       // Step 3: Configure details
}

/**
 * Son Performans - last recorded performance for an exercise
 */
data class LastPerformance(
    val sets: Long,
    val reps: Long,
    val weightKg: Double,
    val dateEpochDay: Long
)

/**
 * Day Status - Takvimde günün durumunu gösterir
 */
enum class DayStatus {
    NONE,       // Hiç workout yok
    RED,        // Workout var ama hiçbiri tamamlanmamış
    ORANGE,     // Kısmi tamamlanma
    GREEN       // Tüm workout'lar tamamlanmış
}

/**
 * Current Workout State - Şu anda aktif olan antrenmanın durumu
 */
data class CurrentWorkoutState(
    val workoutId: Long,                   // Scheduled workout ID
    val name: String,                      // Egzersiz adı
    val muscleGroup: String?,              // Kas grubu
    val sets: Long,                        // Set sayısı
    val reps: Long,                        // Tekrar sayısı
    val weight: Double,                    // Ağırlık (kg)
    val rest: Long?,                       // Dinlenme süresi (saniye)
    val notes: String?,                    // Notlar
    val scheduledDateEpochDay: Long,       // Antrenmanın planlandığı tarih (epoch day)
    val elapsedTimeMs: Long = 0L,          // Geçen süre (milisaniye)
    val isRunning: Boolean = false,        // Timer çalışıyor mu?
    val isCompleted: Boolean = false,      // Antrenman tamamlandı mı?
    val completedSets: Long = 0L           // Tamamlanan set sayısı
)

/**
 * WorkoutViewModel - Ana Workout ekranının iş mantığını yöneten ViewModel
 * 
 * Seçili tarih için planlanmış egzersizleri gösterir
 * Kullanıcılar tarih seçebilir, workout'ları silebilir ve yeni workout ekleyebilir
 * 
 * @param appRepository Exercise listesi ve genel işlemler için repository
 * @param schedulingRepository Zamanlama işlemleri için repository
 * @param workoutPlannerRepository Planlama ve silme işlemleri için repository
 * @param coroutineScope ViewModel yaşam döngüsüne bağlı coroutine scope
 */
class WorkoutViewModel(
    private val appRepository: AppRepository,
    private val schedulingRepository: SchedulingRepository,
    private val workoutPlannerRepository: WorkoutPlannerRepository,
    private val progressRepository: ProgressRepository,
    private val coroutineScope: CoroutineScope,
    private val progressViewModel: ProgressViewModel? = null,
    private val goalViewModel: GoalViewModel? = null
) {

    /**
     * Workout ekranının UI state'i
     * 
     * @property selectedDate Seçili tarih
     * @property scheduledWorkouts O tarih için planlanmış workout'lar
     * @property isLoading Veri yüklenme durumu
     * @property availableExercises Seçilebilir egzersiz listesi
     * @property currentStep Wizard akışının mevcut adımı
     * @property categories Kategori listesi
     * @property selectedCategory Seçili kategori
     * @property filteredExercises Kategoriye göre filtrelenmiş egzersizler
     * @property showAddDialog Exercise seçim dialog'u göster/gizle
     * @property showDatePicker Tarih seçici göster/gizle
     * @property selectedExercise Seçili egzersiz
     * @property configSets Set sayısı
     * @property configReps Tekrar sayısı
     * @property configWeight Ağırlık (kg)
     * @property configRest Dinlenme süresi (saniye)
     * @property configNotes Notlar
     * @property configDate Workout için seçili tarih
     * @property monthDayStatuses Takvim için gün durumları (tarih -> durum)
     * @property dailyTotalTimes Günlük toplam antrenman süreleri (tarih -> toplam ms)
     */
    data class WorkoutUiState(
        val selectedDate: LocalDate = getCurrentDate(),
        val scheduledWorkouts: List<GetScheduledForDate> = emptyList(),
        val isLoading: Boolean = false,
        
        // Add Workout Flow - Exercise list
        val availableExercises: List<Exercises> = emptyList(),
        
        // Wizard state
        val currentStep: WizardStep = WizardStep.CATEGORY,
        val categories: List<String> = listOf(
            "Göğüs",
            "Sırt",
            "Omuz",
            "Kol",
            "Bacak",
            "Karın",
            "Kardiyo",
            "Tüm Vücut"
        ),
        val selectedCategory: String? = null,
        val filteredExercises: List<Exercises> = emptyList(),
        
        // Dialog states
        val showAddDialog: Boolean = false,
        val showDatePicker: Boolean = false,
        val showDeleteConfirmDialog: Boolean = false,

        // Edit Workout Flow - "antrenman ekle" akışındaki config dialog'unu
        // mevcut bir antrenmanı düzenlemek için yeniden kullanır
        val showEditDialog: Boolean = false,
        val editingWorkout: GetScheduledForDate? = null,
        
        // Config
        val selectedExercise: Exercises? = null,
        val configSets: String = "",
        val configReps: String = "",
        val configWeight: String = "",
        val configRest: String = "",
        val configNotes: String = "",
        val configDate: LocalDate = getCurrentDate(),
        val selectedDates: List<LocalDate> = emptyList(), // Çoklu tarih seçimi için
        
        // Current Workout - Aktif olarak çalışılan antrenman
        val currentWorkout: CurrentWorkoutState? = null,
        
        // Calendar & Stats - Takvim göstergeleri
        val monthDayStatuses: Map<LocalDate, DayStatus> = emptyMap(),
        val dailyTotalTimes: Map<LocalDate, Long> = emptyMap(), // Günlük toplam süreler (ms)
        
        // Selected Day Progress - Seçili günün ilerleme bilgileri
        val selectedDayTotalWorkouts: Int = 0,
        val selectedDayCompletedWorkouts: Int = 0,
        val selectedDayCompletionPercent: Int = 0,
        // Gerçekten tamamlanmış (tüm setleri işaretlenmiş) workout'ların sw_id'leri
        val completedWorkoutIds: Set<Long> = emptySet(),
        
        // Error/Warning messages
        val errorMessage: String? = null,

        // Silinen antrenman — [PENDING_DELETE_UNDO_WINDOW_MS] boyunca "SİLİNDİ · GERİ AL"
        // çubuğu gösterilir; bu antrenman hâlâ scheduledWorkouts içindedir (UI onu ayrıca
        // filtreler), süre dolmadan geri al'a basılırsa veritabanından hiç silinmemiş olur.
        val pendingDeleteWorkout: GetScheduledForDate? = null,

        // Son Performans - populated when an exercise is selected
        val lastPerformance: LastPerformance? = null,

        // Son Seans - bugünden önceki en son antrenman yapılan günün özeti
        // (Ana sayfadaki "SON SEANS" şeridi için)
        val lastSession: LastSessionSummary? = null,

        // Antrenman Bitti özeti - günün son egzersizi tamamlandığında dolar,
        // showWorkoutCompleteSummary 3sn sonra otomatik false'a döner.
        val workoutCompleteSummary: WorkoutCompleteSummary? = null,
        val showWorkoutCompleteSummary: Boolean = false,

        // Dinlenme Ekranı - bir egzersiz tamamlandığında, günün başka egzersizi
        // varsa sıradaki egzersize geçmeden önce gösterilir.
        val isResting: Boolean = false,
        val restTotalSeconds: Int = 0,
        val restSecondsRemaining: Int = 0,
        val restNextWorkoutId: Long? = null,
        val restNextWorkoutName: String? = null,
        val restNextWorkoutSets: Long? = null
    )

    data class LastSessionSummary(
        val label: String,          // O günün kas grubu (veya tek egzersizse adı)
        val durationMinutes: Long,  // O gün toplam antrenman süresi
        val exerciseCount: Int,     // O gün planlanmış egzersiz sayısı
        val daysAgo: Long           // Bugünden kaç gün önce
    )

    /**
     * Antrenman Bitti özeti - günün tüm egzersizleri tamamlandığında
     * 3 saniyeliğine gösterilen kartın verisi.
     */
    data class WorkoutCompleteSummary(
        val durationMinutes: Long,  // O gün toplam antrenman süresi
        val exerciseCount: Int,     // Tamamlanan egzersiz sayısı
        val setCount: Long,         // Tamamlanan toplam set sayısı
        val totalKg: Double         // Kaldırılan toplam ağırlık (set * tekrar * kg)
    )

    // Private mutable state
    private val _uiState = MutableStateFlow(WorkoutUiState())
    
    /**
     * Public immutable state - UI tarafından gözlemlenir
     */
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    // Flow collection job - eski flow'u iptal etmek için
    private var workoutFlowJob: kotlinx.coroutines.Job? = null
    
    // Current Workout timer job
    private var timerJob: kotlinx.coroutines.Job? = null

    // Dinlenme ekranı geri sayım job'u
    private var restJob: kotlinx.coroutines.Job? = null

    // Bekleyen (geri alınabilir) silme işleminin geri sayım job'u
    private var pendingDeleteJob: kotlinx.coroutines.Job? = null
    
    // Günün diğer workout'larının toplam süresi (cache için)
    private var otherWorkoutsTotalTimeCache: Long = 0L

    init {
        // Başlangıçta bugün için workout'ları yükle
        loadWorkoutsForDate(_uiState.value.selectedDate)
        
        // Egzersiz listesini yükle ve gerekirse seed et
        loadExercisesAndSeedIfNeeded()
        
        // Takvim durumlarını yükle
        loadMonthStatuses(_uiState.value.selectedDate)
        
        
    }
    
    /**
     * Tüm mevcut egzersizleri yükle ve DB boşsa/eksikse seed et
     * Legacy Android uygulamasından port edilmiş 58 egzersiz kullanır
     */
    private fun loadExercisesAndSeedIfNeeded() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val currentExercises = appRepository.getAllExercises().first()
                
                // Eğer DB boş VEYA çok az egzersiz varsa (eski dummy data), temizle ve yeniden seed et
                val shouldReseed = currentExercises.isEmpty() || currentExercises.size < 20
                
                if (shouldReseed) {
                    println("🌱 WorkoutViewModel: DB'de ${currentExercises.size} egzersiz var - Yeniden seeding yapılıyor...")
                    
                    coroutineScope.launch(Dispatchers.Default) {
                        try {
                            // 1. Tüm eski egzersizleri temizle
                            if (currentExercises.isNotEmpty()) {
                                println("🧹 WorkoutViewModel: Eski ${currentExercises.size} egzersiz temizleniyor...")
                                appRepository.deleteAllExercises()
                            }
                            
                            // 2. Legacy egzersizleri ekle
                            println("🌱 WorkoutViewModel: 58 legacy egzersiz ekleniyor...")
                            com.example.aigymapp.utils.ExerciseSeeder.seedExercises(appRepository)
                            
                            println("✅ WorkoutViewModel: Seeding tamamlandı!")
                        } catch (e: Exception) {
                            println("❌ WorkoutViewModel: Seeding hatası - ${e.message}")
                            e.printStackTrace()
                        }
                    }
                } else {
                    println("✅ WorkoutViewModel: DB'de ${currentExercises.size} egzersiz mevcut")
                }
                
                // Now collect exercises
                appRepository.getAllExercises().collect { exercises ->
                    _uiState.update { 
                        it.copy(
                            availableExercises = exercises,
                            filteredExercises = exercises // Initially show all
                        ) 
                    }
                }
            } catch (e: Exception) {
                println("WorkoutViewModel: Egzersizler yüklenemedi - ${e.message}")
            }
        }
    }
    

    /**
     * Tarih seçildiğinde çağrılır
     * 
     * State'i günceller ve seçili tarih için workout'ları yükler
     * Current workout korunur - kullanıcı farklı günlere bakabilir ama aktif antrenman devam eder
     * 
     * @param date Seçilen tarih
     */
    /**
     * Hata banner'ındaki "TAMAM" aksiyonu — mesajı anında temizler, otomatik
     * 3sn temizleme timer'ını beklemeden.
     */
    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onDateSelected(date: LocalDate) {
        val previousDate = _uiState.value.selectedDate

        // Başka bir güne geçilirken bekleyen bir silme varsa, geri al çubuğu artık
        // görünür olmayacağı için silmeyi hemen kalıcı yap.
        pendingDeleteJob?.cancel()
        _uiState.value.pendingDeleteWorkout?.let { pending ->
            coroutineScope.launch(Dispatchers.Default) {
                commitDelete(pending.sw_id, previousDate)
            }
        }

        // State'i güncelle - current workout KORUNUYOR
        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = date,
                isLoading = true,
                pendingDeleteWorkout = null
                // currentWorkout DEĞİŞTİRİLMİYOR - aktif antrenman korunuyor
            )
        }
        
        println("📅 WorkoutViewModel: Tarih değişti - $previousDate -> $date (Current workout korunuyor)")
        
        // Yeni tarih için workout'ları yükle
        loadWorkoutsForDate(date)
        
        // Eğer ay değiştiyse, takvim durumlarını yeniden yükle
        if (date.month != previousDate.month || 
            date.year != previousDate.year) {
            loadMonthStatuses(date)
        }
    }

    /**
     * Belirli bir tarih için workout'ları yükler
     * 
     * LocalDate'i epoch days'e çevirir ve repository'den veri çeker
     * Flow'u collect ederek UI state'ini günceller
     * 
     * @param date Yüklenecek tarih
     */
    private fun loadWorkoutsForDate(date: LocalDate) {
        // Eski flow collection'ı iptal et
        workoutFlowJob?.cancel()
        
        // Yeni flow collection başlat
        workoutFlowJob = coroutineScope.launch(Dispatchers.Main) {
            try {
                // LocalDate'i epoch days'e çevir
                val epochDay = date.toEpochDays().toLong()
                
                println("🔄 WorkoutViewModel: Flow başlatılıyor - Tarih: $date, EpochDay: $epochDay")
                
                // Repository'den flow olarak veri al
                schedulingRepository.getScheduledForDateFlow(epochDay).collect { workouts ->
                    println("📋 WorkoutViewModel: Flow güncellendi - ${workouts.size} workout")
                    
                    // Seçili günün tamamlanma bilgilerini hesapla
                    loadSelectedDayProgress(date, workouts)
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            scheduledWorkouts = workouts,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda log ve state güncelleme
                println("❌ WorkoutViewModel: Workout'lar yüklenemedi - ${e.message}")
                _uiState.update { currentState ->
                    currentState.copy(
                        scheduledWorkouts = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Seçili günün tamamlanma bilgilerini yükler
     * 
     * @param date Seçili tarih
     * @param workouts O tarihteki workout'lar
     */
    private fun loadSelectedDayProgress(
        date: LocalDate,
        workouts: List<GetScheduledForDate>,
        celebrateOnFullCompletion: Boolean = false
    ) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                val dateString = date.toString()

                // Progress repository'den günün kartlarını al
                val dayCards = progressRepository.getDayCards(dateString)

                if (workouts.isEmpty()) {
                    // Workout yoksa tamamlanma bilgilerini sıfırla
                    _uiState.update {
                        it.copy(
                            selectedDayTotalWorkouts = 0,
                            selectedDayCompletedWorkouts = 0,
                            selectedDayCompletionPercent = 0,
                            completedWorkoutIds = emptySet()
                        )
                    }
                    return@launch
                }

                // Her workout için tamamlanma durumunu kontrol et
                var completedCount = 0
                val completedIds = mutableSetOf<Long>()
                workouts.forEach { workout ->
                    val card = dayCards.find { it.plan_item_id == workout.wpi_id }
                    val totalSets = workout.wpi_sets.toInt()
                    val completedSets = card?.completed_sets?.toInt() ?: 0

                    // Eğer tüm setler tamamlandıysa, workout tamamlanmış sayılır
                    if (completedSets >= totalSets && totalSets > 0) {
                        completedCount++
                        completedIds.add(workout.sw_id)
                    }
                }

                val totalCount = workouts.size
                val percent = if (totalCount > 0) {
                    ((completedCount.toDouble() / totalCount.toDouble()) * 100).toInt()
                } else {
                    0
                }

                println("📊 WorkoutViewModel: Seçili gün progress - $completedCount/$totalCount (%$percent)")

                // Günün tüm egzersizleri şimdi mi tamamlandı? (0/N ya da kısmi -> N/N geçişi)
                val justCompletedFullDay = celebrateOnFullCompletion &&
                    totalCount > 0 &&
                    completedCount == totalCount &&
                    _uiState.value.selectedDayCompletedWorkouts != totalCount

                val summary = if (justCompletedFullDay) {
                    buildWorkoutCompleteSummary(date, workouts)
                } else null

                // State'i güncelle
                _uiState.update {
                    // Gün tamamen bittiğinde, o günün current workout'u da temizlenir —
                    // Ana Sayfa'daki aktif antrenman kartı "Bugün henüz başlamadın"
                    // durumuna kapanır (AnimatedContent, currentWorkout == null'a göre).
                    val clearCurrentWorkout = justCompletedFullDay &&
                        it.currentWorkout?.scheduledDateEpochDay == date.toEpochDays().toLong()

                    it.copy(
                        selectedDayTotalWorkouts = totalCount,
                        selectedDayCompletedWorkouts = completedCount,
                        selectedDayCompletionPercent = percent,
                        completedWorkoutIds = completedIds,
                        workoutCompleteSummary = summary ?: it.workoutCompleteSummary,
                        showWorkoutCompleteSummary = if (justCompletedFullDay) true else it.showWorkoutCompleteSummary,
                        currentWorkout = if (clearCurrentWorkout) null else it.currentWorkout
                    )
                }
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Seçili gün progress yüklenemedi - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Günün tüm egzersizleri tamamlandığında "Antrenman Bitti" kartı için
     * özet verisini hesaplar (süre, egzersiz/set sayısı, toplam kg).
     */
    private suspend fun buildWorkoutCompleteSummary(
        date: LocalDate,
        workouts: List<GetScheduledForDate>
    ): WorkoutCompleteSummary {
        val totalSets = workouts.sumOf { it.wpi_sets }
        val totalKg = workouts.sumOf { it.wpi_sets * it.wpi_reps * it.wpi_weight_kg }
        val durationMs = schedulingRepository.getTotalElapsedTimeForDate(date.toEpochDays().toLong())

        return WorkoutCompleteSummary(
            durationMinutes = durationMs / 60000L,
            exerciseCount = workouts.size,
            setCount = totalSets,
            totalKg = totalKg
        )
    }

    /**
     * "Antrenman Bitti" özet kartını kapatır (3sn'lik otomatik kapanma ya da
     * kullanıcının KAPAT'a basması sonucu çağrılır).
     */
    fun dismissWorkoutCompleteSummary() {
        _uiState.update { it.copy(showWorkoutCompleteSummary = false) }
    }
    
    /**
     * Planlanmış bir workout'ı sil
     * 
     * KONTROL: Eğer workout current workout olarak seçiliyse ve timer çalışıyorsa silinemez
     * 
     * Repository üzerinden silme işlemi yapar
     * Flow otomatik olarak güncelleneceği için manuel state güncellemesi gerekmez
     * 
     * @param scheduledId Silinecek scheduled workout'ın ID'si
     */
    /**
     * Drag-to-reorder kartlardan gelen yeni sıralamayı uygular.
     * UI'da anında yansıması için state'i optimistik günceller, sonra
     * kalıcı olarak veritabanına yazar (sort_order).
     */
    fun onWorkoutsReordered(newOrder: List<GetScheduledForDate>) {
        _uiState.update { it.copy(scheduledWorkouts = newOrder) }
        coroutineScope.launch(Dispatchers.Default) {
            try {
                schedulingRepository.reorderScheduledWorkouts(newOrder.map { it.sw_id })
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Sıralama kaydedilemedi - ${e.message}")
            }
        }
    }

    fun deleteWorkout(scheduledId: Long) {
        val currentWorkout = _uiState.value.currentWorkout
        val currentDate = _uiState.value.selectedDate
        
        println("🗑️ WorkoutViewModel: deleteWorkout çağrıldı - ID: $scheduledId")
        println("   Current workout var mı: ${currentWorkout != null}")
        if (currentWorkout != null) {
            println("   Current workout ID: ${currentWorkout.workoutId}")
            println("   Current workout isRunning: ${currentWorkout.isRunning}")
            println("   ID'ler eşleşiyor mu: ${currentWorkout.workoutId == scheduledId}")
        }
        
        // Kontrol: Current workout olarak seçiliyse silinemez (timer çalışsın veya çalışmasın)
        if (currentWorkout != null && 
            currentWorkout.workoutId == scheduledId) {
            
            val errorMsg = "Aktif antrenman silinemez, önce çıkarın."
            println("⚠️ WorkoutViewModel: AKTİF ANTRENMAN - $errorMsg")
            println("   Hata mesajı state'e ekleniyor...")
            
            // Hata mesajını göster
            _uiState.update {
                it.copy(errorMessage = errorMsg)
            }
            
            println("   State güncellendi - errorMessage: ${_uiState.value.errorMessage}")
            
            // 3 saniye sonra mesajı temizle
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
                println("   Error message temizlendi")
            }
            
            return
        }
        
        println("✅ WorkoutViewModel: Kontroller geçildi, silme işlemi devam ediyor...")

        val workout = _uiState.value.scheduledWorkouts.firstOrNull { it.sw_id == scheduledId } ?: return

        // Zaten bekleyen bir silme varsa (kullanıcı geri al çubuğu açıkken başka bir
        // antrenmanı daha sildiyse), öncekini hemen kalıcı yap — aynı anda tek çubuk gösterilir.
        pendingDeleteJob?.cancel()
        _uiState.value.pendingDeleteWorkout?.let { previouslyPending ->
            coroutineScope.launch(Dispatchers.Default) {
                commitDelete(previouslyPending.sw_id, currentDate)
            }
        }

        _uiState.update { it.copy(pendingDeleteWorkout = workout) }

        pendingDeleteJob = coroutineScope.launch {
            kotlinx.coroutines.delay(PENDING_DELETE_UNDO_WINDOW_MS)
            commitDelete(scheduledId, currentDate)
        }
    }

    /**
     * "GERİ AL" — bekleyen silmeyi iptal eder. Antrenman hiçbir zaman veritabanından
     * silinmemişti (sadece UI'da gizlenmişti), bu yüzden geri getirmek state'ten
     * [pendingDeleteWorkout]'u temizlemekten ibarettir.
     */
    fun undoDeleteWorkout() {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        _uiState.update { it.copy(pendingDeleteWorkout = null) }
    }

    /**
     * Geri alma penceresi dolunca (veya üstüne yeni bir silme binince) antrenmanı
     * kalıcı olarak veritabanından siler.
     */
    private suspend fun commitDelete(scheduledId: Long, date: LocalDate) {
        try {
            schedulingRepository.deleteScheduledWorkout(scheduledId)
            println("🗑️ WorkoutViewModel: Workout silindi - ID: $scheduledId")

            // Progress'i yeniden hesapla ve UI'ı güncelle
            progressRepository.recomputeDay(date.toString())
            kotlinx.coroutines.delay(200)
            loadMonthStatuses(date)
            progressViewModel?.refreshCurrentMonth()
        } catch (e: Exception) {
            println("❌ WorkoutViewModel: Workout silinemedi - ${e.message}")
        } finally {
            _uiState.update {
                if (it.pendingDeleteWorkout?.sw_id == scheduledId) it.copy(pendingDeleteWorkout = null) else it
            }
        }
    }

    /**
     * Tüm workout'ları silmek için onay dialog'unu göster
     */
    fun showDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }
    
    /**
     * Silme onay dialog'unu kapat
     */
    fun dismissDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }
    
    /**
     * Seçili tarihteki tüm workout'ları sil
     * 
     * Kullanıcı bir günün tüm workout'larını temizlemek istediğinde kullanılır
     */
    fun deleteAllWorkoutsForSelectedDate() {
        val currentDate = _uiState.value.selectedDate
        val currentWorkout = _uiState.value.currentWorkout
        val scheduledWorkouts = _uiState.value.scheduledWorkouts
        
        // Kontrol: Eğer current workout bu günün workout'larından biriyse, toplu silme yapılamaz
        if (currentWorkout != null) {
            val isCurrentWorkoutInThisDay = scheduledWorkouts.any { it.sw_id == currentWorkout.workoutId }
            
            if (isCurrentWorkoutInThisDay) {
                val errorMsg = "Bu günün antrenmanı aktif menüde, önce çıkarın."
                println("⚠️ WorkoutViewModel: AKTİF ANTRENMAN BU GÜNDE - $errorMsg")
                
                // Dialog'u kapat
                _uiState.update { it.copy(showDeleteConfirmDialog = false, errorMessage = errorMsg) }
                
                // 3 saniye sonra mesajı temizle
                coroutineScope.launch {
                    kotlinx.coroutines.delay(3000L)
                    _uiState.update { it.copy(errorMessage = null) }
                }
                
                return
            }
        }
        
        coroutineScope.launch(Dispatchers.Default) {
            try {
                val epochDay = currentDate.toEpochDays().toLong()
                schedulingRepository.deleteScheduledForDate(epochDay)
                println("🗑️ WorkoutViewModel: Tüm workout'lar silindi - Tarih: $currentDate")
                
                // Dialog'u kapat
                _uiState.update { it.copy(showDeleteConfirmDialog = false) }
                
                // Progress'i yeniden hesapla ve UI'ı güncelle
                progressRepository.recomputeDay(currentDate.toString())
                kotlinx.coroutines.delay(200)
                loadMonthStatuses(currentDate)
                progressViewModel?.refreshCurrentMonth()
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Workout'lar silinemedi - ${e.message}")
            }
        }
    }

    /**
     * Seçili tarih için workout olup olmadığını kontrol eder
     * 
     * @return true eğer workout varsa, false yoksa
     */
    fun hasWorkoutsForSelectedDate(): Boolean {
        return _uiState.value.scheduledWorkouts.isNotEmpty()
    }

    /**
     * Bugünün tarihine dön
     */
    fun selectToday() {
        onDateSelected(getCurrentDate())
    }

    // ==================== ADD WORKOUT FLOW ====================
    
    /**
     * Kullanıcı + FAB'a tıkladı - Start wizard at CATEGORY step
     */
    fun onAddFabClicked() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                currentStep = WizardStep.CATEGORY,
                selectedCategory = null,
                filteredExercises = emptyList()
            )
        }
    }
    
    /**
     * User selected a category - Go to EXERCISE step
     */
    fun onCategorySelected(category: String) {
        coroutineScope.launch(Dispatchers.Main) {
            val filtered = _uiState.value.availableExercises.filter { 
                it.muscle_group == category 
            }
            
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    filteredExercises = filtered,
                    currentStep = WizardStep.EXERCISE
                )
            }
        }
    }
    
    /**
     * User clicked back in wizard
     */
    fun onWizardBack() {
        val currentStep = _uiState.value.currentStep
        
        when (currentStep) {
            WizardStep.EXERCISE -> {
                // Go back to category — keep the prior selection so the
                // step doesn't rebuild blank when the user returns to it.
                _uiState.update { it.copy(currentStep = WizardStep.CATEGORY) }
            }
            WizardStep.CONFIG -> {
                // Go back to exercise — same: selection persists.
                _uiState.update { it.copy(currentStep = WizardStep.EXERCISE) }
            }
            else -> {
                // Close dialog from category step
                onAddDialogDismissed()
            }
        }
    }
    
    /**
     * Kullanıcı listeden bir egzersiz seçti - Go to CONFIG step
     */
    fun onExerciseSelected(exercise: Exercises) {
        val today = getCurrentDate()
        val todayEpochDay = today.toEpochDays().toLong()
        _uiState.update {
            it.copy(
                currentStep = WizardStep.CONFIG,
                selectedExercise = exercise,
                configDate = it.selectedDate,
                configSets = "",
                configReps = "",
                configWeight = "",
                configRest = "",
                configNotes = "",
                lastPerformance = null
            )
        }
        // Fetch last performance in background
        coroutineScope.launch {
            val perf = schedulingRepository.getLastPerformanceForExercise(
                exerciseName = exercise.name,
                beforeEpochDay = todayEpochDay
            )
            _uiState.update { state ->
                state.copy(
                    lastPerformance = perf?.let {
                        LastPerformance(
                            sets = it.sets ?: 0L,
                            reps = it.reps ?: 0L,
                            weightKg = it.weight_kg ?: 0.0,
                            dateEpochDay = it.date_epoch_day
                        )
                    }
                )
            }
        }
    }
    
    /**
     * Config dialog'da tarih alanına tıklandı
     */
    fun onConfigDateClick() {
        _uiState.update { it.copy(showDatePicker = true) }
    }
    
    /**
     * Tarih seçici'den bir tarih seçildi
     */
    fun onConfigDateSelected(date: LocalDate) {
        val today = getCurrentDate()
        
        // Geçmiş tarihe antrenman eklenemez kontrolü
        if (date < today) {
            _uiState.update { 
                it.copy(
                    configDate = today,
                    showDatePicker = false,
                    errorMessage = "Geçmiş tarihe eklenemez!"
                )
            }

            // Hata mesajını 3 saniye sonra temizle
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(errorMessage = null) }
            }
        } else {
            _uiState.update { 
                it.copy(
                    configDate = date,
                    showDatePicker = false,
                    errorMessage = null
                ) 
            }
        }
    }
    
    /**
     * Tarih seçici'den birden fazla tarih seçildi
     * Tarihleri state'e kaydeder, config menüsüne döner
     */
    fun onMultipleDatesSelected(dates: List<LocalDate>) {
        if (dates.isEmpty()) return
        
        val today = getCurrentDate()
        
        // Geçmiş tarihleri filtrele
        val validDates = dates.filter { it >= today }
        val filteredCount = dates.size - validDates.size
        
        if (filteredCount > 0) {
            _uiState.update { 
                it.copy(
                    selectedDates = validDates,
                    configDate = if (validDates.isNotEmpty()) validDates.first() else today,
                    showDatePicker = false,
                    errorMessage = "$filteredCount geçmiş tarih atlandı."
                ) 
            }
            
            // Hata mesajını 3 saniye sonra temizle
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(errorMessage = null) }
            }
        } else {
            // Tarihleri state'e kaydet ve dialog'u kapat
            // İlk tarihi configDate olarak göster
            _uiState.update { 
                it.copy(
                    selectedDates = dates,
                    configDate = dates.first(),
                    showDatePicker = false,
                    errorMessage = null
                ) 
            }
        }
    }
    
    /**
     * Tarih seçici iptal edildi
     */
    fun onDatePickerDismissed() {
        _uiState.update { it.copy(showDatePicker = false) }
    }
    
    /**
     * Config alanları güncelleme fonksiyonları
     */
    fun onConfigSetsChanged(value: String) {
        _uiState.update { it.copy(configSets = value) }
    }
    
    fun onConfigRepsChanged(value: String) {
        _uiState.update { it.copy(configReps = value) }
    }
    
    fun onConfigWeightChanged(value: String) {
        _uiState.update { it.copy(configWeight = value) }
    }
    
    fun onConfigRestChanged(value: String) {
        _uiState.update { it.copy(configRest = value) }
    }
    
    fun onConfigNotesChanged(value: String) {
        _uiState.update { it.copy(configNotes = value) }
    }
    
    /**
     * Config dialog iptal edildi - Close wizard completely
     */
    fun onConfigDialogDismissed() {
        _uiState.update { 
            it.copy(
                showAddDialog = false,
                currentStep = WizardStep.CATEGORY,
                selectedCategory = null,
                selectedExercise = null,
                filteredExercises = emptyList(),
                configSets = "",
                configReps = "",
                configWeight = "",
                configRest = "",
                configNotes = ""
            ) 
        }
    }
    
    /**
     * Wizard iptal edildi - Close completely
     */
    fun onAddDialogDismissed() {
        _uiState.update { 
            it.copy(
                showAddDialog = false,
                currentStep = WizardStep.CATEGORY,
                selectedCategory = null,
                selectedExercise = null,
                filteredExercises = emptyList()
            ) 
        }
    }
    
    /**
     * Workout'ı veritabanına kaydet
     * 
     * 1. Config date için session oluştur/al
     * 2. Workout planner item ekle
     * 3. Scheduled workout olarak ekle
     * 4. Seçili tarihi config date'e güncelle (UI o güne atlar)
     */
    fun onSaveWorkout() {
        val state = _uiState.value
        val exercise = state.selectedExercise ?: return
        
        // Eğer çoklu tarih seçilmişse, onları kullan; yoksa configDate'i kullan
        val datesToSchedule = if (state.selectedDates.isNotEmpty()) {
            state.selectedDates
        } else {
            listOf(state.configDate)
        }
        
        // Geçmiş tarihleri filtrele
        val today = getCurrentDate()
        val validDates = datesToSchedule.filter { it >= today }
        
        if (validDates.isEmpty()) {
            _uiState.update { 
                it.copy(
                    showAddDialog = false,
                    currentStep = WizardStep.CATEGORY,
                    selectedCategory = null,
                    selectedExercise = null,
                    filteredExercises = emptyList(),
                    errorMessage = "Geçmiş tarihe eklenemez!"
                )
            }

            // Hata mesajını 3 saniye sonra temizle
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(errorMessage = null) }
            }
            return
        }
        
        coroutineScope.launch {
            try {
                // Input'ları parse et (varsayılan değerlerle)
                val sets = state.configSets.toLongOrNull() ?: 3L
                val reps = state.configReps.toLongOrNull() ?: 10L
                val weight = state.configWeight.toDoubleOrNull() ?: 0.0
                val rest = state.configRest.toLongOrNull()
                val notes = state.configNotes.ifEmpty { null }
                
                // Her tarih için antrenman oluştur (IO thread'de)
                kotlinx.coroutines.withContext(Dispatchers.Default) {
                    validDates.forEach { date ->
                        val epochDay = date.toEpochDays().toLong()
                        
                        // 1. Bu tarih için session oluştur/al
                        val sessionId = workoutPlannerRepository.getOrCreateSessionForDate(epochDay)
                        
                        // 2. Workout planner item ekle
                        val itemId = workoutPlannerRepository.insertItem(
                            sessionId = sessionId,
                            exerciseId = exercise.id,
                            sets = sets,
                            reps = reps,
                            weightKg = weight,
                            durationMin = 0L,
                            restSec = rest,
                            notes = notes
                        )
                        
                        // 3. Scheduled workout olarak ekle
                        schedulingRepository.insertScheduledWorkouts(
                            planItemId = itemId,
                            epochDays = listOf(epochDay)
                        )
                        
                        println("✅ WorkoutViewModel: Workout kaydedildi - ${exercise.name} için $date")
                    }
                }
                
                // İlk tarihe git ve o tarihin workout'larını yükle
                val firstDate = datesToSchedule.first()
                val epochDay = firstDate.toEpochDays().toLong()
                
                // Küçük bir delay - veritabanının güncellenmesini bekle
                kotlinx.coroutines.delay(100)
                
                // Workout'ları yükle
                val updatedWorkouts = schedulingRepository.getScheduledForDateFlow(epochDay).first()
                
                println("📋 WorkoutViewModel: ${updatedWorkouts.size} workout yüklendi - Tarih: $firstDate")
                
                // State'i güncelle (workout'ları da dahil et) - Main thread'de
                _uiState.update { 
                    it.copy(
                        selectedDate = firstDate,
                        scheduledWorkouts = updatedWorkouts,
                        showAddDialog = false,
                        currentStep = WizardStep.CATEGORY,
                        selectedCategory = null,
                        selectedExercise = null,
                        filteredExercises = emptyList(),
                        configSets = "",
                        configReps = "",
                        configWeight = "",
                        configRest = "",
                        configNotes = "",
                        selectedDates = emptyList(), // Seçili tarihleri temizle
                        isLoading = false
                    ) 
                }
                
                // Flow'u yeniden başlat (değişiklikleri dinlemek için)
                loadWorkoutsForDate(firstDate)
                
                // Progress'i yeniden hesapla ve UI'ı güncelle
                validDates.forEach { date ->
                    progressRepository.recomputeDay(date.toString())
                }
                kotlinx.coroutines.delay(200)
                loadMonthStatuses(firstDate)
                progressViewModel?.refreshCurrentMonth()
                
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Workout kaydedilemedi - ${e.message}")
                e.printStackTrace()
                // Hata durumunda dialog'u kapat
                _uiState.update { 
                    it.copy(
                        showAddDialog = false,
                        currentStep = WizardStep.CATEGORY,
                        selectedCategory = null,
                        selectedExercise = null,
                        filteredExercises = emptyList(),
                        selectedDates = emptyList()
                    )
                }
            }
        }
    }

    // ==================== EDIT WORKOUT FUNCTIONS ====================

    /**
     * Egzersiz satırındaki "Düzenle" seçeneğine tıklandı.
     * "Antrenman ekle" akışındaki config dialog'unu, mevcut değerlerle
     * doldurulmuş şekilde düzenleme modunda açar.
     */
    fun onEditWorkoutClicked(workout: GetScheduledForDate) {
        val currentWorkout = _uiState.value.currentWorkout
        if (currentWorkout != null && currentWorkout.workoutId == workout.sw_id) {
            _uiState.update {
                it.copy(errorMessage = "Aktif antrenman düzenlenemez, önce çıkarın.")
            }
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(errorMessage = null) }
            }
            return
        }

        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingWorkout = workout,
                selectedExercise = null,
                selectedDates = emptyList(),
                configDate = LocalDate.fromEpochDays(workout.sw_date_epoch_day.toInt()),
                configSets = workout.wpi_sets.toString(),
                configReps = workout.wpi_reps.toString(),
                configWeight = if (workout.wpi_weight_kg % 1.0 == 0.0)
                    workout.wpi_weight_kg.toLong().toString()
                else
                    workout.wpi_weight_kg.toString(),
                configRest = workout.wpi_rest_sec?.toString() ?: "",
                configNotes = workout.wpi_notes ?: "",
                lastPerformance = null
            )
        }

        coroutineScope.launch {
            val exercise = appRepository.getExerciseById(workout.wpi_exercise_id)
            val perf = schedulingRepository.getLastPerformanceForExercise(
                exerciseName = exercise?.name ?: workout.exercise_name,
                beforeEpochDay = workout.sw_date_epoch_day
            )
            _uiState.update { state ->
                state.copy(
                    selectedExercise = exercise,
                    lastPerformance = perf?.let {
                        LastPerformance(
                            sets = it.sets ?: 0L,
                            reps = it.reps ?: 0L,
                            weightKg = it.weight_kg ?: 0.0,
                            dateEpochDay = it.date_epoch_day
                        )
                    }
                )
            }
        }
    }

    /**
     * Düzenleme dialog'u iptal edildi
     */
    fun onEditDialogDismissed() {
        _uiState.update {
            it.copy(
                showEditDialog = false,
                editingWorkout = null,
                selectedExercise = null,
                configSets = "",
                configReps = "",
                configWeight = "",
                configRest = "",
                configNotes = "",
                selectedDates = emptyList()
            )
        }
    }

    /**
     * Düzenlenen antrenmanı kaydet.
     * Tarih değişmediyse sadece set/tekrar/ağırlık/dinlenme/not güncellenir;
     * tarih değiştiyse eski scheduled kayıt silinip yeni tarihe taşınır.
     */
    fun onSaveEditedWorkout() {
        val state = _uiState.value
        val workout = state.editingWorkout ?: return
        val today = getCurrentDate()

        if (state.configDate < today) {
            _uiState.update {
                it.copy(errorMessage = "Geçmiş tarihe taşınamaz!")
            }
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(errorMessage = null) }
            }
            return
        }

        coroutineScope.launch {
            try {
                val sets = state.configSets.toLongOrNull() ?: workout.wpi_sets
                val reps = state.configReps.toLongOrNull() ?: workout.wpi_reps
                val weight = state.configWeight.toDoubleOrNull() ?: workout.wpi_weight_kg
                val rest = state.configRest.toLongOrNull()
                val notes = state.configNotes.ifEmpty { null }
                val newEpochDay = state.configDate.toEpochDays().toLong()
                val oldEpochDay = workout.sw_date_epoch_day
                val dateChanged = newEpochDay != oldEpochDay

                kotlinx.coroutines.withContext(Dispatchers.Default) {
                    workoutPlannerRepository.updateItem(
                        id = workout.wpi_id,
                        sessionId = workout.wpi_session_id,
                        exerciseId = workout.wpi_exercise_id,
                        sets = sets,
                        reps = reps,
                        weightKg = weight,
                        durationMin = workout.wpi_duration_min,
                        restSec = rest,
                        notes = notes
                    )

                    if (dateChanged) {
                        schedulingRepository.deleteScheduledWorkout(workout.sw_id)
                        schedulingRepository.insertScheduledWorkouts(
                            planItemId = workout.wpi_id,
                            epochDays = listOf(newEpochDay)
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        showEditDialog = false,
                        editingWorkout = null,
                        selectedExercise = null,
                        configSets = "",
                        configReps = "",
                        configWeight = "",
                        configRest = "",
                        configNotes = "",
                        selectedDates = emptyList()
                    )
                }

                kotlinx.coroutines.delay(100)

                if (dateChanged) {
                    onDateSelected(LocalDate.fromEpochDays(newEpochDay.toInt()))
                }

                progressRepository.recomputeDay(LocalDate.fromEpochDays(oldEpochDay.toInt()).toString())
                if (dateChanged) {
                    progressRepository.recomputeDay(LocalDate.fromEpochDays(newEpochDay.toInt()).toString())
                }
                kotlinx.coroutines.delay(200)
                loadMonthStatuses(_uiState.value.selectedDate)
                progressViewModel?.refreshCurrentMonth()
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Workout düzenlenemedi - ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        showEditDialog = false,
                        editingWorkout = null,
                        selectedExercise = null,
                        configSets = "",
                        configReps = "",
                        configWeight = "",
                        configRest = "",
                        configNotes = "",
                        selectedDates = emptyList()
                    )
                }
            }
        }
    }

    // ==================== CURRENT WORKOUT FUNCTIONS ====================
    
    /**
     * Bir antrenmanı "Current Workout" olarak seç
     * SADECE seçili tarihe ait antrenmanlar current workout olarak ayarlanabilir
     */
    fun onWorkoutSelectedAsCurrent(workout: GetScheduledForDate) {
        // Yeni bir egzersiz current yapılırken olası tutarsız bir dinlenme durumu kalmasın
        restJob?.cancel()
        restJob = null
        if (_uiState.value.isResting) {
            _uiState.update {
                it.copy(
                    isResting = false,
                    restTotalSeconds = 0,
                    restSecondsRemaining = 0,
                    restNextWorkoutId = null,
                    restNextWorkoutName = null,
                    restNextWorkoutSets = null
                )
            }
        }

        val today = getCurrentDate().toEpochDays().toLong()
        val currentWorkout = _uiState.value.currentWorkout
        
        println("🔍 WorkoutViewModel: onWorkoutSelectedAsCurrent çağrıldı")
        println("   Workout ID: ${workout.sw_id}, Adı: ${workout.exercise_name}")
        println("   Workout tarihi (epochDay): ${workout.sw_date_epoch_day}")
        println("   Bugünün tarihi (epochDay): $today")
        println("   Kaydedilmiş süre: ${workout.sw_elapsed_time_ms}ms")
        
        // Kontrol 1: Eğer bir antrenman çalışıyorsa, önce eski süreyi kaydet
        if (currentWorkout != null && currentWorkout.isRunning) {
            println("⚠️ WorkoutViewModel: Timer çalışıyor - önce kaydediliyor")
            
            // Önce süreyi kaydet
            coroutineScope.launch(Dispatchers.Default) {
                try {
                    schedulingRepository.updateElapsedTime(
                        scheduledWorkoutId = currentWorkout.workoutId,
                        elapsedTimeMs = currentWorkout.elapsedTimeMs
                    )
                    println("💾 WorkoutViewModel: Süre kaydedildi - ${currentWorkout.elapsedTimeMs}ms")
                } catch (e: Exception) {
                    println("❌ WorkoutViewModel: Süre kaydedilemedi - ${e.message}")
                }
            }
            
            _uiState.update {
                it.copy(errorMessage = "Önce durdurun!")
            }
            
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
            }
            
            return
        }
        
        // Kontrol 2: SADECE BUGÜNE AIT antrenmanlar current workout olarak seçilebilir
        if (workout.sw_date_epoch_day != today) {
            println("⚠️ WorkoutViewModel: Bugün olmayan antrenman seçilmeye çalışıldı")
            // Sessizce engelle, kullanıcıya mesaj gösterme (UI'da zaten soluk)
            return
        }
        
        // Eğer zaten bir timer çalışıyorsa durdur
        stopTimer()
        
        // Eğer mevcut antrenman varsa ve farklı bir antrenman seçiliyorsa, süreyi kaydet
        if (currentWorkout != null && currentWorkout.workoutId != workout.sw_id) {
            coroutineScope.launch(Dispatchers.Default) {
                try {
                    schedulingRepository.updateElapsedTime(
                        scheduledWorkoutId = currentWorkout.workoutId,
                        elapsedTimeMs = currentWorkout.elapsedTimeMs
                    )
                    println("💾 WorkoutViewModel: Önceki antrenman süresi kaydedildi - ${currentWorkout.elapsedTimeMs}ms")
                } catch (e: Exception) {
                    println("❌ WorkoutViewModel: Önceki süre kaydedilemedi - ${e.message}")
                }
            }
        }
        
        // Database'den tamamlanma durumunu ve süreyi kontrol et
        coroutineScope.launch(Dispatchers.Default) {
            try {
                val currentDate = _uiState.value.selectedDate
                val dateString = currentDate.toString()
                
                // ProgressRepository üzerinden günün kartlarını al
                val dayCards = progressRepository.getDayCards(dateString)
                
                // Bu workout'ın kartını bul
                val workoutCard = dayCards.find { it.plan_item_id == workout.wpi_id }
                
                // Tamamlanma durumunu kontrol et
                val totalSets = workout.wpi_sets.toInt()
                val completedSets = workoutCard?.completed_sets?.toInt() ?: 0
                val isCompleted = completedSets >= totalSets && totalSets > 0
                
                // Veritabanından kaydedilmiş süreyi al
                val savedElapsedTime = workout.sw_elapsed_time_ms
                
                // Günün diğer workout'larının toplam süresini cache'e al
                val allWorkouts = schedulingRepository.getScheduledForDate(workout.sw_date_epoch_day)
                otherWorkoutsTotalTimeCache = allWorkouts
                    .filter { it.sw_id != workout.sw_id }
                    .sumOf { it.sw_elapsed_time_ms }
                
                println("✅ WorkoutViewModel: Workout tamamlanma durumu kontrol edildi")
                println("   Tamamlanan setler: $completedSets/$totalSets")
                println("   isCompleted: $isCompleted")
                println("   Yüklenen süre: ${savedElapsedTime}ms")
                println("   Diğer workout'lar toplam: ${otherWorkoutsTotalTimeCache}ms")
                
                // State'i güncelle (Main thread'de)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            currentWorkout = CurrentWorkoutState(
                                workoutId = workout.sw_id,
                                name = workout.exercise_name,
                                muscleGroup = workout.exercise_muscle_group,
                                sets = workout.wpi_sets,
                                reps = workout.wpi_reps,
                                weight = workout.wpi_weight_kg,
                                rest = workout.wpi_rest_sec,
                                notes = workout.wpi_notes,
                                scheduledDateEpochDay = workout.sw_date_epoch_day,
                                elapsedTimeMs = savedElapsedTime,  // ← Veritabanından yüklenen süre!
                                isRunning = false,
                                isCompleted = isCompleted,
                                completedSets = completedSets.toLong()
                            ),
                            errorMessage = null
                        )
                    }
                    println("✅ WorkoutViewModel: Current workout seçildi - ${workout.exercise_name}")
                }
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Tamamlanma durumu kontrol hatası - ${e.message}")
                e.printStackTrace()
                
                // Hata durumunda varsayılan olarak false
                _uiState.update {
                    it.copy(
                        currentWorkout = CurrentWorkoutState(
                            workoutId = workout.sw_id,
                            name = workout.exercise_name,
                            muscleGroup = workout.exercise_muscle_group,
                            sets = workout.wpi_sets,
                            reps = workout.wpi_reps,
                            weight = workout.wpi_weight_kg,
                            rest = workout.wpi_rest_sec,
                            notes = workout.wpi_notes,
                            scheduledDateEpochDay = workout.sw_date_epoch_day,
                            elapsedTimeMs = 0L,
                            isRunning = false,
                            isCompleted = false
                        ),
                        errorMessage = null
                    )
                }
            }
        }
    }
    
    /**
     * Current workout timer'ı başlat/durdur
     */
    fun onCurrentWorkoutStartPause() {
        val current = _uiState.value.currentWorkout ?: return
        
        // Başlatmadan önce tarih kontrolü yap
        if (!current.isRunning) {
            val today = getCurrentDate().toEpochDays().toLong()
            if (current.scheduledDateEpochDay != today) {
                val errorMsg = if (current.scheduledDateEpochDay < today) {
                    "Geçmiş tarihte başlatılamaz!"
                } else {
                    "Gelecek tarihte başlatılamaz!"
                }
                
                _uiState.update {
                    it.copy(errorMessage = errorMsg)
                }
                
                coroutineScope.launch {
                    kotlinx.coroutines.delay(3000L)
                    _uiState.update { it.copy(errorMessage = null) }
                }
                
                return
            }
        }
        
        if (current.isRunning) {
            // Timer çalışıyorsa durdur ve veritabanına kaydet
            stopTimer()
            _uiState.update {
                it.copy(
                    currentWorkout = current.copy(isRunning = false)
                )
            }
            println("⏸️ WorkoutViewModel: Timer durduruldu - Süre: ${current.elapsedTimeMs}ms")
            
            // Süreyi veritabanına kaydet ve günlük toplam süreleri güncelle
            coroutineScope.launch(Dispatchers.Default) {
                try {
                    schedulingRepository.updateElapsedTime(
                        scheduledWorkoutId = current.workoutId,
                        elapsedTimeMs = current.elapsedTimeMs
                    )
                    println("💾 WorkoutViewModel: Süre veritabanına kaydedildi - ${current.elapsedTimeMs}ms")
                    
                    // Cache'i güncelle
                    val allWorkouts = schedulingRepository.getScheduledForDate(current.scheduledDateEpochDay)
                    otherWorkoutsTotalTimeCache = allWorkouts
                        .filter { it.sw_id != current.workoutId }
                        .sumOf { it.sw_elapsed_time_ms }
                    
                    val totalTimeForDay = otherWorkoutsTotalTimeCache + current.elapsedTimeMs
                    
                    // Günlük toplam süreleri yeniden yükle
                    loadMonthStatuses(_uiState.value.selectedDate)
                    
                    // Progress ekranını da güncelle
                    progressViewModel?.let { progress ->
                        val today = getCurrentDate()
                        val selectedDay = progress.selectedDay.value
                        if (selectedDay != null && selectedDay == today.dayOfMonth &&
                            progress.selectedMonth.value.month == today.month &&
                            progress.selectedMonth.value.year == today.year) {
                            progress.updateSelectedDayDuration(totalTimeForDay)
                        }
                    }
                } catch (e: Exception) {
                    println("❌ WorkoutViewModel: Süre kaydedilemedi - ${e.message}")
                }
            }
        } else {
            // Timer duruyorsa başlat
            startTimer()
            _uiState.update {
                it.copy(
                    currentWorkout = current.copy(isRunning = true)
                )
            }
            println("▶️ WorkoutViewModel: Timer başlatıldı")
        }
    }
    
    /**
     * Timer'ı başlat (her saniye güncelle)
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L) // Her 1 saniye
                
                val current = _uiState.value.currentWorkout
                if (current != null && current.isRunning) {
                    val newElapsedTime = current.elapsedTimeMs + 1000L
                    
                    // Günlük toplam süreyi güncelle (anlık olarak, cache kullanarak)
                    val currentDate = LocalDate.fromEpochDays(current.scheduledDateEpochDay.toInt())
                    val currentTotalTimes = _uiState.value.dailyTotalTimes.toMutableMap()
                    
                    // Bu workout'ın şu anki süresi + diğer workout'ların süresi (cache'den)
                    val totalTimeForDay = otherWorkoutsTotalTimeCache + newElapsedTime
                    currentTotalTimes[currentDate] = totalTimeForDay
                    
                    _uiState.update {
                        it.copy(
                            currentWorkout = current.copy(
                                elapsedTimeMs = newElapsedTime
                            ),
                            dailyTotalTimes = currentTotalTimes
                        )
                    }
                    
                    // Progress ekranını da güncelle (eğer seçili gün bugünse)
                    progressViewModel?.let { progress ->
                        val today = getCurrentDate()
                        val selectedDay = progress.selectedDay.value
                        if (selectedDay != null && selectedDay == today.dayOfMonth &&
                            progress.selectedMonth.value.month == today.month &&
                            progress.selectedMonth.value.year == today.year) {
                            // Seçili gün bugünse, anlık süreyi güncelle
                            progress.updateSelectedDayDuration(totalTimeForDay)
                        }
                    }
                } else {
                    break
                }
            }
        }
    }
    
    /**
     * Timer'ı durdur
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null

    }
    
    /**
     * Current workout timer'ını sıfırla
     */
    fun onCurrentWorkoutReset() {
        val current = _uiState.value.currentWorkout ?: return
        
        // Timer çalışıyorsa sıfırlamaya izin verme
        if (current.isRunning) {
            println("⚠️ WorkoutViewModel: Timer çalışıyor - sıfırlanamaz")
            
            _uiState.update {
                it.copy(errorMessage = "Önce durdurun!")
            }
            
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
            }
            
            return
        }
        
        stopTimer()
        
        _uiState.update {
            it.copy(
                currentWorkout = current.copy(
                    elapsedTimeMs = 0L,
                    isRunning = false
                )
            )
        }
        println("🔄 WorkoutViewModel: Timer sıfırlandı")
        
        // Veritabanına da sıfır yaz ve Progress ekranını güncelle
        coroutineScope.launch(Dispatchers.Default) {
            try {
                schedulingRepository.updateElapsedTime(
                    scheduledWorkoutId = current.workoutId,
                    elapsedTimeMs = 0L
                )
                println("💾 WorkoutViewModel: Süre veritabanında sıfırlandı")
                
                // Cache'i güncelle
                val allWorkouts = schedulingRepository.getScheduledForDate(current.scheduledDateEpochDay)
                otherWorkoutsTotalTimeCache = allWorkouts
                    .filter { it.sw_id != current.workoutId }
                    .sumOf { it.sw_elapsed_time_ms }
                
                val totalTimeForDay = otherWorkoutsTotalTimeCache // Bu workout 0 olduğu için eklenmedi
                
                // Günlük toplam süreleri yeniden yükle
                loadMonthStatuses(_uiState.value.selectedDate)
                
                // Progress ekranını da güncelle
                progressViewModel?.let { progress ->
                    val today = getCurrentDate()
                    val selectedDay = progress.selectedDay.value
                    if (selectedDay != null && selectedDay == today.dayOfMonth &&
                        progress.selectedMonth.value.month == today.month &&
                        progress.selectedMonth.value.year == today.year) {
                        progress.updateSelectedDayDuration(totalTimeForDay)
                    }
                }
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Süre sıfırlanamadı - ${e.message}")
            }
        }
    }
    
    /**
     * Current workout'u temizle
     */
    fun onCurrentWorkoutClear() {
        val current = _uiState.value.currentWorkout
        
        // Timer çalışıyorsa temizlemeye izin verme
        if (current != null && current.isRunning) {
            println("⚠️ WorkoutViewModel: Timer çalışıyor - antrenman temizlenemez")
            
            _uiState.update {
                it.copy(errorMessage = "Önce durdurun!")
            }
            
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
            }
            
            return
        }
        
        // Temizlemeden önce süreyi kaydet
        if (current != null) {
            coroutineScope.launch(Dispatchers.Default) {
                try {
                    schedulingRepository.updateElapsedTime(
                        scheduledWorkoutId = current.workoutId,
                        elapsedTimeMs = current.elapsedTimeMs
                    )
                    println("💾 WorkoutViewModel: Temizlemeden önce süre kaydedildi - ${current.elapsedTimeMs}ms")
                } catch (e: Exception) {
                    println("❌ WorkoutViewModel: Süre kaydedilemedi - ${e.message}")
                }
            }
        }
        
        stopTimer()
        _uiState.update {
            it.copy(currentWorkout = null)
        }
        println("🗑️ WorkoutViewModel: Current workout temizlendi")
    }
    
    /**
     * Current workout'u tamamlandı olarak işaretle
     */
    fun onCurrentWorkoutCompleted(completed: Boolean) {
        val current = _uiState.value.currentWorkout ?: return
        
        // Timer çalışıyorsa tamamlama yapılamaz
        if (current.isRunning) {
            println("⚠️ WorkoutViewModel: Timer çalışıyor - tamamlama yapılamaz")
            
            _uiState.update {
                it.copy(errorMessage = "Önce durdurun!")
            }
            
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
            }
            
            return
        }
        
        // Timer duruyorsa durdur (güvenlik için)
        if (current.isRunning) {
            stopTimer()
        }
        
        _uiState.update {
            it.copy(
                currentWorkout = current.copy(
                    isCompleted = completed,
                    isRunning = false // Tamamlandığında timer'ı durdur
                )
            )
        }
        
        println("${if (completed) "✅" else "❌"} WorkoutViewModel: Current workout tamamlandı durumu: $completed")
        
        // Database'i güncelle ve UI'ı yenile
        onMarkWorkoutCompleted(current.workoutId, completed, current.scheduledDateEpochDay)
    }

    /**
     * Listedeki bir egzersizin tamamlanma kutucuğuna basıldığında çağrılır.
     * O egzersizin "current workout" olması gerekmez — listedeki herhangi bir
     * egzersiz doğrudan buradan tamamlandı/tamamlanmadı olarak işaretlenebilir.
     */
    fun onWorkoutItemCompletedToggle(workout: GetScheduledForDate) {
        val current = _uiState.value.currentWorkout
        val isThisRunning = current?.workoutId == workout.sw_id && current.isRunning

        // Timer çalışıyorsa tamamlama yapılamaz (current workout ile aynı kural)
        if (isThisRunning) {
            _uiState.update {
                it.copy(errorMessage = "Önce durdurun!")
            }
            coroutineScope.launch {
                kotlinx.coroutines.delay(3000L)
                _uiState.update { it.copy(errorMessage = null) }
            }
            return
        }

        val newCompleted = workout.sw_id !in _uiState.value.completedWorkoutIds

        // Eğer bu egzersiz şu an current workout ise mini player'ı da senkronize et
        if (current != null && current.workoutId == workout.sw_id) {
            _uiState.update {
                it.copy(
                    currentWorkout = current.copy(
                        isCompleted = newCompleted,
                        isRunning = false
                    )
                )
            }
        }

        println("${if (newCompleted) "✅" else "❌"} WorkoutViewModel: Egzersiz tamamlandı durumu (kutucuktan): $newCompleted - ${workout.exercise_name}")

        onMarkWorkoutCompleted(workout.sw_id, newCompleted, workout.sw_date_epoch_day)
    }

    /**
     * Platform-agnostic şekilde mevcut tarihi alır
     * 
     * @return Bugünün LocalDate değeri
     */
    private fun getCurrentDate(): LocalDate {
        val now = Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    
    // ==================== CALENDAR & STATS FUNCTIONS ====================
        
    /**
     * Belirli bir ay için tüm günlerin durumlarını yükler
     * Takvimde görsel göstergeler için kullanılır
     * 
     * @param date Ay bilgisi için referans tarih
     */
    private fun loadMonthStatuses(date: LocalDate) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                // Ayın ilk ve son gününü hesapla (farklı aylara sarkan günler için aralığı genişletiyoruz)
                val baseFirstDay = LocalDate(date.year, date.month, 1)
                val firstDayOfMonth = baseFirstDay.minus(14, DateTimeUnit.DAY)
                
                // Ayın son gününü hesapla (bir sonraki ayın ilk günü - 1 gün)
                val baseLastDay = baseFirstDay
                    .plus(1, DateTimeUnit.MONTH)
                    .plus(-1, DateTimeUnit.DAY)
                val lastDayOfMonth = baseLastDay.plus(14, DateTimeUnit.DAY)
                
                // ProgressRepository'den ay için progress verilerini al
                val progressData = progressRepository.getProgressRange(
                    startDate = firstDayOfMonth.toString(),
                    endDate = lastDayOfMonth.toString()
                )
                
                // Her gün için durum hesapla
                val statusMap = progressData.associate { dayProgress ->
                    val localDate = LocalDate.parse(dayProgress.date)
                    val status = when (dayProgress.status) {
                        "NO_PLAN" -> DayStatus.NONE
                        "FULL" -> DayStatus.GREEN
                        "PARTIAL" -> DayStatus.ORANGE
                        "NONE" -> DayStatus.RED
                        else -> DayStatus.NONE
                    }
                    localDate to status
                }
                
                // Her gün için toplam süreyi hesapla
                val timeMap = mutableMapOf<LocalDate, Long>()
                var currentDate = firstDayOfMonth
                while (currentDate <= lastDayOfMonth) {
                    val epochDay = currentDate.toEpochDays().toLong()
                    val totalTime = schedulingRepository.getTotalElapsedTimeForDate(epochDay)
                    if (totalTime > 0) {
                        timeMap[currentDate] = totalTime
                    }
                    currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                }
                
                println("📅 WorkoutViewModel: Ay durumları yüklendi - ${statusMap.size} gün")
                println("⏱️ WorkoutViewModel: Günlük süreler yüklendi - ${timeMap.size} gün")

                // Son seans - bugünden önceki en son antrenman yapılan gün
                val today = getCurrentDate()
                val lastSessionDate = statusMap.entries
                    .filter { (day, status) ->
                        day < today && (status == DayStatus.GREEN || status == DayStatus.ORANGE)
                    }
                    .maxByOrNull { it.key }
                    ?.key

                val lastSessionSummary = lastSessionDate?.let { day ->
                    val epochDay = day.toEpochDays().toLong()
                    val dayWorkouts = schedulingRepository.getScheduledForDate(epochDay)
                    if (dayWorkouts.isEmpty()) {
                        null
                    } else {
                        val totalMs = timeMap[day] ?: schedulingRepository.getTotalElapsedTimeForDate(epochDay)
                        val primary = dayWorkouts.first()
                        LastSessionSummary(
                            label = primary.exercise_muscle_group ?: primary.exercise_name,
                            durationMinutes = totalMs / 60000L,
                            exerciseCount = dayWorkouts.size,
                            daysAgo = today.toEpochDays().toLong() - epochDay
                        )
                    }
                }

                // State'i güncelle
                _uiState.update {
                    it.copy(
                        monthDayStatuses = statusMap,
                        dailyTotalTimes = timeMap,
                        lastSession = lastSessionSummary
                    )
                }
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Ay durumları yüklenemedi - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Bir workout tamamlandı olarak işaretlendiğinde çağrılır
     * Database'i günceller ve UI'ı yeniler
     * NOT: [epochDay] verilmezse current workout'un (varsa) tarihi, o da yoksa seçili tarih kullanılır.
     * Listedeki belirli bir egzersiz için her zaman o egzersizin kendi tarihi verilmelidir.
     */
    fun onMarkWorkoutCompleted(workoutId: Long, completed: Boolean, epochDay: Long? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                val workoutEpochDay = epochDay
                    ?: _uiState.value.currentWorkout?.scheduledDateEpochDay
                    ?: _uiState.value.selectedDate.toEpochDays().toLong()

                val workoutDate = LocalDate.fromEpochDays(workoutEpochDay.toInt())
                val dateString = workoutDate.toString()

                // Tüm setleri tamamlandı olarak işaretle (veya temizle)
                val workout = _uiState.value.scheduledWorkouts.find { it.sw_id == workoutId }
                    ?: schedulingRepository.getScheduledForDate(workoutEpochDay).find { it.sw_id == workoutId }
                    
                if (workout != null) {
                    val sets = workout.wpi_sets
                    
                    for (setIndex in 0 until sets) {
                        if (completed) {
                            // Seti tamamlandı olarak işaretle
                            progressRepository.onSetCheckboxToggled(
                                planItemId = workout.wpi_id,
                                setIndex = setIndex,
                                date = dateString,
                                checked = true,
                                repsCompleted = workout.wpi_reps,
                                weightUsed = workout.wpi_weight_kg
                            )
                        } else {
                            // Seti temizle
                            progressRepository.onSetCheckboxToggled(
                                planItemId = workout.wpi_id,
                                setIndex = setIndex,
                                date = dateString,
                                checked = false
                            )
                        }
                    }
                    
                    // Progress'i yeniden hesapla
                    progressRepository.recomputeDay(dateString)
                    
                    // UI'ı güncelle
                    kotlinx.coroutines.delay(200) // Database güncellemesini bekle
                    
                    // Workout'un tarihini kullanarak month statuses'ı güncelle
                    loadMonthStatuses(workoutDate)
                    
                    // Eğer workout'un tarihi şu an seçili tarihle aynıysa, progress bilgilerini de güncelle
                    if (workoutDate == _uiState.value.selectedDate) {
                        loadSelectedDayProgress(
                            date = workoutDate,
                            workouts = _uiState.value.scheduledWorkouts,
                            celebrateOnFullCompletion = completed
                        )
                    }
                    
                    // ProgressViewModel'i de güncelle (Stats ekranındaki takvim için)
                    progressViewModel?.refreshCurrentMonth()

                    // GoalViewModel'i güncelle - Ana Sayfa'daki hedef çubukları yeni değere aksın
                    goalViewModel?.refreshProgress()

                    // Egzersiz tamamlandıysa ve günün başka egzersizi varsa dinlenme ekranını başlat
                    if (completed) {
                        startRestIfNeeded(workout, _uiState.value.completedWorkoutIds + workoutId)
                    }

                    println("✅ WorkoutViewModel: Workout tamamlama durumu güncellendi - ID: $workoutId, Completed: $completed, Tarih: $dateString")
                }
            } catch (e: Exception) {
                println("❌ WorkoutViewModel: Workout tamamlama güncellenemedi - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Bir egzersiz tamamlandığında çağrılır. Günün listesinde bu egzersizden sonra gelen
     * (listenin sonuna gelinirse başa sarılarak aranan) ilk tamamlanmamış egzersiz varsa,
     * o egzersizin dinlenme süresi kadar geri sayan tam ekran dinlenme ekranını açar.
     * Sırada tamamlanmamış egzersiz yoksa (gün bitti) hiçbir şey yapmaz — bu durumda zaten
     * mevcut "Antrenman Bitti" özeti devreye girer.
     */
    private fun startRestIfNeeded(completedWorkout: GetScheduledForDate, newCompletedIds: Set<Long>) {
        val orderedList = _uiState.value.scheduledWorkouts
        val currentIndex = orderedList.indexOfFirst { it.sw_id == completedWorkout.sw_id }
        val next = if (currentIndex >= 0) {
            (orderedList.drop(currentIndex + 1) + orderedList.take(currentIndex))
                .firstOrNull { it.sw_id !in newCompletedIds }
        } else null

        if (next == null) {
            println("🏁 WorkoutViewModel: Sırada egzersiz yok - dinlenme ekranı açılmadı")
            return
        }

        // Antrenmanı düzenlerken belirlenen dinlenme süresi aynen kullanılır - burada
        // rastgele bir varsayılana (ör. 60sn) düşülmez. Kullanıcı dinlenme süresini hiç
        // ayarlamadıysa (config çarkında varsayılan 0'dır) dinlenme ekranı hiç açılmaz,
        // direkt sıradaki egzersize geçilir.
        val restSeconds = completedWorkout.wpi_rest_sec?.toInt() ?: 0
        if (restSeconds <= 0) {
            println("⏩ WorkoutViewModel: Dinlenme süresi ayarlanmamış (0sn) - direkt sıradaki egzersize geçiliyor")
            onWorkoutSelectedAsCurrent(next)
            return
        }

        restJob?.cancel()
        _uiState.update {
            it.copy(
                isResting = true,
                restTotalSeconds = restSeconds,
                restSecondsRemaining = restSeconds,
                restNextWorkoutId = next.sw_id,
                restNextWorkoutName = next.exercise_name,
                restNextWorkoutSets = next.wpi_sets
            )
        }

        println("💤 WorkoutViewModel: Dinlenme başladı - ${restSeconds}sn, sıradaki: ${next.exercise_name}")

        restJob = coroutineScope.launch {
            while (_uiState.value.restSecondsRemaining > 0) {
                kotlinx.coroutines.delay(1000L)
                _uiState.update { it.copy(restSecondsRemaining = (it.restSecondsRemaining - 1).coerceAtLeast(0)) }
            }
            finishRest(next)
        }
    }

    /**
     * Kullanıcı dinlenme ekranındaki GEÇ butonuna bastığında çağrılır -
     * geri sayımı iptal edip sıradaki egzersize hemen geçer.
     */
    fun skipRest() {
        if (!_uiState.value.isResting) return
        val nextId = _uiState.value.restNextWorkoutId
        val next = _uiState.value.scheduledWorkouts.firstOrNull { it.sw_id == nextId }
        restJob?.cancel()
        restJob = null
        println("⏭️ WorkoutViewModel: Dinlenme atlandı")
        finishRest(next)
    }

    /**
     * Dinlenme ekranını kapatır ve varsa sıradaki egzersizi current workout yapar.
     */
    private fun finishRest(next: GetScheduledForDate?) {
        restJob = null
        _uiState.update {
            it.copy(
                isResting = false,
                restTotalSeconds = 0,
                restSecondsRemaining = 0,
                restNextWorkoutId = null,
                restNextWorkoutName = null,
                restNextWorkoutSets = null
            )
        }
        if (next != null) {
            onWorkoutSelectedAsCurrent(next)
        }
    }

    /**
     * Artık yıl kontrolü
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    companion object {
        /**
         * Platform-agnostic şekilde mevcut tarihi alır (Static/Companion accessor)
         * 
         * @return Bugünün LocalDate değeri
         */
        fun getCurrentDate(): LocalDate {
            val now = Clock.System.now()
            return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }
}
