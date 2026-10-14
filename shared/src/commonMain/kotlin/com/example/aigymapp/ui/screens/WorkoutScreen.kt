package com.example.aigymapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.with
import com.example.aigymapp.database.GetScheduledForDate
import com.example.aigymapp.ui.components.CustomIcons
import com.example.aigymapp.ui.components.leftRule
import com.example.aigymapp.ui.components.topRule
import com.example.aigymapp.ui.theme.ModernistDark as MD
import com.example.aigymapp.ui.viewmodels.WorkoutViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle

// Custom Colors - Define all colors explicitly
// ... (colors code assumed unchanged, skip logic implies focusing on the import)
// Wait, I must be careful with replace_file_content context.
// I will target the imports block specifically.

// Actually, I can fix the logic block inside CalendarStripSection as well.
// Let's do imports first.

internal val NeonLime = Color(0xFFCCFF00)
internal val DarkBackground = Color(0xFF000000)  // Pure Black - OLED optimized
private val DarkSurface = Color(0xFF0D0D0D)     // Very Dark Grey - Cards
internal val DarkSurfaceVariant = Color(0xFF1A1A1A)  // Slightly lighter for contrast
private val TextPrimary = Color(0xFFFFFFFF)
internal val TextSecondary = Color(0xB3FFFFFF)
private val ErrorRed = Color(0xFFEF5350)
private val PartialYellow = Color(0xFFD1A02A)

// Egzersiz görsel kutusunun boş hâli — sayfa zemininden (MD.Bg) bir ton açık,
// kas grubu rozetiyle görsel yer tutucu ikonu bu zemin üzerinde oturur.
private val ExerciseImagePlaceholderBg = Color(0xFF1D1B1A)

/**
 * Workout Screen
 * Main screen showing calendar and scheduled workouts
 */
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    // Fresh per composition instance — WorkoutScreen is torn down and rebuilt each time the
    // tab is switched away and back (see App.kt's `when (selectedScreenIndex)`), so this
    // naturally gets a new value on every reopen, replaying the band entrance below.
    val screenKey = remember { Any() }

    // Note: BackHandler is Android-specific and removed for iOS compatibility
    // The workout timer protection should be handled at platform level

    // Error banner content survives the exit animation — errorMessage drops to null the
    // instant the ViewModel clears it, but the slide-up-out shouldn't play against blank text.
    var lastErrorMessage by remember { mutableStateOf("") }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { lastErrorMessage = it }
    }

    // Haftalık görünüm için haftanın günlerini hesapla (Pazartesi-Pazar)
    val weekDays = remember(state.selectedDate) {
        // Seçili günün haftasını hesapla (Pazartesi'den başlayarak)
        val dayOfWeek = when (state.selectedDate.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> 0
            kotlinx.datetime.DayOfWeek.TUESDAY -> 1
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> 2
            kotlinx.datetime.DayOfWeek.THURSDAY -> 3
            kotlinx.datetime.DayOfWeek.FRIDAY -> 4
            kotlinx.datetime.DayOfWeek.SATURDAY -> 5
            kotlinx.datetime.DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        
        // Haftanın Pazartesi'sini bul
        val monday = state.selectedDate.minus(dayOfWeek, DateTimeUnit.DAY)
        
        // 7 günü oluştur
        (0..6).map { index ->
            val date = monday.plus(index, DateTimeUnit.DAY)
            DateItem(
                date = date,
                dayNumber = date.dayOfMonth.toString(),
                dayName = getDayNameTurkish(date)
            )
        }
    }
    
    // Client-side muscle-group filter for the day's list — resets whenever the day changes.
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.selectedDate) { selectedCategoryFilter = null }
    // Ordered by the fixed wizard category order (state.categories), not by where each
    // muscle group happens to fall in the day's list — so drag-to-reorder on the exercise
    // list below never reshuffles these filter chips.
    val availableCategories = remember(state.scheduledWorkouts, state.categories) {
        val present = state.scheduledWorkouts.mapNotNull { it.exercise_muscle_group }.toSet()
        state.categories.filter { it in present } +
            present.filter { it !in state.categories }
    }
    val filteredWorkouts = remember(state.scheduledWorkouts, selectedCategoryFilter, state.pendingDeleteWorkout) {
        val filter = selectedCategoryFilter
        val base = if (filter == null) state.scheduledWorkouts
        else state.scheduledWorkouts.filter { it.exercise_muscle_group == filter }
        // Silinip "GERİ AL" penceresinde bekleyen antrenman henüz veritabanından
        // silinmedi ama listede de görünmemeli — geri al çubuğu kapanana kadar gizlenir.
        val pendingId = state.pendingDeleteWorkout?.sw_id
        if (pendingId == null) base else base.filterNot { it.sw_id == pendingId }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MD.Bg)) {
            StaggeredBand(index = 0, screenKey = screenKey) {
                WorkoutDayHeader(
                    selectedDate = state.selectedDate,
                    onAddClick = { viewModel.onAddFabClicked() }
                )
            }

            // Calendar Strip - Apple Fitness style
            StaggeredBand(index = 1, screenKey = screenKey) {
                CalendarStripSection(
                    monthYear = getMonthYearTurkish(state.selectedDate),
                    weekDays = weekDays,
                    selectedDate = state.selectedDate,
                    hasWorkouts = state.scheduledWorkouts.isNotEmpty(),
                    dayStatuses = state.monthDayStatuses,
                    onDateSelected = { selectedDate ->
                        viewModel.onDateSelected(selectedDate)
                    },
                    onWeekChanged = { weekOffset ->
                        // Hafta değiştiğinde seçili günü güncelle
                        val newDate = state.selectedDate.plus(weekOffset * 7, DateTimeUnit.DAY)
                        viewModel.onDateSelected(newDate)
                    },
                    onDeleteAllClick = {
                        viewModel.showDeleteConfirmDialog()
                    }
                )
            }

            if (availableCategories.isNotEmpty()) {
                StaggeredBand(index = 2, screenKey = screenKey) {
                    CategoryFilterRow(
                        categories = availableCategories,
                        selected = selectedCategoryFilter,
                        onSelect = { selectedCategoryFilter = it }
                    )
                }
            }

            // Workout List or Empty State
            StaggeredBand(
                index = 3,
                screenKey = screenKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.scheduledWorkouts.isEmpty()) {
                        EmptyWorkoutState()
                    } else {
                        WorkoutList(
                            workouts = filteredWorkouts,
                            selectedDate = state.selectedDate,
                            selectedCurrentWorkoutId = state.currentWorkout?.workoutId,
                            completedWorkoutIds = state.completedWorkoutIds,
                            isCurrentWorkoutRunning = state.currentWorkout?.isRunning ?: false,
                            isAnyWorkoutRunning = state.currentWorkout?.isRunning ?: false,
                            // Sıralama sadece filtresiz (tam) listede anlamlı — bir kategori
                            // filtrelenmişken alt küme sürüklemesi tüm listedeki sırayı belirsizleştirir.
                            reorderEnabled = selectedCategoryFilter == null,
                            onWorkoutClick = { workout ->
                                // Tıklayınca direkt current workout yap
                                viewModel.onWorkoutSelectedAsCurrent(workout)
                            },
                            onDeleteClick = { workout ->
                                viewModel.deleteWorkout(workout.sw_id)
                            },
                            onEditClick = { workout ->
                                viewModel.onEditWorkoutClicked(workout)
                            },
                            onSetAsCurrentClick = { workout ->
                                viewModel.onWorkoutSelectedAsCurrent(workout)
                            },
                            onCompletedToggleClick = { workout ->
                                viewModel.onWorkoutItemCompletedToggle(workout)
                            },
                            onReorder = { newOrder ->
                                viewModel.onWorkoutsReordered(newOrder)
                            }
                        )
                    }
                }
            }
        }

        // Morphing "current workout" bar — mini player <-> full-screen detail,
        // Spotify style: drag/tap to grow the mini bar into the full sheet and back.
        val currentExerciseIndex = remember(state.scheduledWorkouts, state.currentWorkout?.workoutId) {
            state.scheduledWorkouts.indexOfFirst { it.sw_id == state.currentWorkout?.workoutId } + 1
        }
        StaggeredBand(index = 4, screenKey = screenKey, modifier = Modifier.fillMaxSize()) {
            MorphingWorkoutBar(
                workout = state.currentWorkout,
                exerciseIndex = currentExerciseIndex,
                totalExercises = state.scheduledWorkouts.size,
                onStartPauseClick = { viewModel.onCurrentWorkoutStartPause() },
                onResetClick = { viewModel.onCurrentWorkoutReset() },
                onClearClick = { viewModel.onCurrentWorkoutClear() },
                onCompletedToggle = { completed ->
                    viewModel.onCurrentWorkoutCompleted(completed)
                },
                isResting = state.isResting,
                restTotalSeconds = state.restTotalSeconds,
                restSecondsRemaining = state.restSecondsRemaining,
                restNextWorkoutName = state.restNextWorkoutName,
                onSkipRest = { viewModel.skipRest() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Error banner — slides down from the top edge, same dark surface as
        // every other status banner in the app, red accent for errors.
        com.example.aigymapp.ui.components.StatusBanner(
            visible = state.errorMessage != null,
            tone = com.example.aigymapp.ui.components.SnackTone.Error,
            message = lastErrorMessage,
            onAction = { viewModel.dismissErrorMessage() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // "SİLİNDİ · <EGZERSİZ>" geri al çubuğu — bir antrenman silindiğinde aktif
        // antrenman menüsünün hemen üstünde birkaç saniyeliğine açılır; süre dolmadan
        // "GERİ AL"a basılırsa antrenman aynı yerine geri döner.
        //
        // pendingDeleteWorkout state'ten anında null'a düşer (bkz. undoDeleteWorkout),
        // bu yüzden içerik en son bilinen değeri ayrıca hatırlar — yoksa kapanma
        // animasyonu boş içerikle oynardı.
        var lastDeletedWorkout by remember { mutableStateOf<GetScheduledForDate?>(null) }
        LaunchedEffect(state.pendingDeleteWorkout) {
            state.pendingDeleteWorkout?.let { lastDeletedWorkout = it }
        }
        AnimatedVisibility(
            visible = state.pendingDeleteWorkout != null,
            enter = expandVertically(animationSpec = tween(280)) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(animationSpec = tween(220)) { fullHeight -> fullHeight } +
                fadeOut(animationSpec = tween(160)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 68.dp) // Aktif antrenman çubuğunun üstünde
        ) {
            lastDeletedWorkout?.let { deleted ->
                DeleteUndoBar(
                    exerciseName = deleted.exercise_name,
                    onUndoClick = { viewModel.undoDeleteWorkout() }
                )
            }
        }

        // "Antrenman Bitti" özeti — günün son egzersizi tamamlanınca 3sn
        // gösterilir, sonra aynı yoldan aşağı inerek kapanır.
        WorkoutCompleteSummaryOverlay(
            visible = state.showWorkoutCompleteSummary,
            summary = state.workoutCompleteSummary,
            onDismiss = { viewModel.dismissWorkoutCompleteSummary() }
        )

        // Wizard Dialog - Shows different content based on step. The header
        // (back/close · step label · progress bar) is hoisted above the
        // AnimatedContent so it persists across steps: the label throws the old
        // value up and brings the new one in from below, and the active segment
        // widens to fill, independently of the panel's own horizontal slide.
        //
        // Kept as a child of the screen's outer Box (rather than a sibling
        // top-level composable) so it reliably stacks above the day list —
        // HorizontalPager's page slot doesn't guarantee overlapping z-order
        // for multiple root-level composables emitted from one page.
        if (state.showAddDialog) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MD.Bg)
        ) {
            val currentStep = state.currentStep
            WizardStepHeader(
                stepLabel = when (currentStep) {
                    com.example.aigymapp.ui.viewmodels.WizardStep.CATEGORY -> "ADIM 1 · 3"
                    com.example.aigymapp.ui.viewmodels.WizardStep.EXERCISE -> "ADIM 2 · 3"
                    com.example.aigymapp.ui.viewmodels.WizardStep.CONFIG -> "ADIM 3 · 3"
                },
                filledSegments = when (currentStep) {
                    com.example.aigymapp.ui.viewmodels.WizardStep.CATEGORY -> 1
                    com.example.aigymapp.ui.viewmodels.WizardStep.EXERCISE -> 2
                    com.example.aigymapp.ui.viewmodels.WizardStep.CONFIG -> 3
                },
                onBack = if (currentStep == com.example.aigymapp.ui.viewmodels.WizardStep.CATEGORY) {
                    { viewModel.onAddDialogDismissed() }
                } else {
                    { viewModel.onWizardBack() }
                },
                trailing = if (currentStep == com.example.aigymapp.ui.viewmodels.WizardStep.CONFIG) {
                    {
                        Text(
                            text = "İPTAL",
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextDim,
                            modifier = Modifier.clickable(onClick = { viewModel.onConfigDialogDismissed() })
                        )
                    }
                } else null
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(tween(440, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(tween(440, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(160)))
                    } else {
                        (slideInHorizontally(tween(440, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(tween(440, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(tween(160)))
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "addWorkoutWizardStep"
            ) { animatedStep ->
                when (animatedStep) {
                    com.example.aigymapp.ui.viewmodels.WizardStep.CATEGORY -> {
                        CategorySelectionDialog(
                            categories = state.categories,
                            availableExercises = state.availableExercises,
                            initialSelectedCategory = state.selectedCategory,
                            onCategorySelected = { viewModel.onCategorySelected(it) }
                        )
                    }
                    com.example.aigymapp.ui.viewmodels.WizardStep.EXERCISE -> {
                        ExerciseSelectionDialog(
                            exercises = state.filteredExercises,
                            selectedCategory = state.selectedCategory ?: "",
                            onExerciseSelected = { viewModel.onExerciseSelected(it) }
                        )
                    }
                    com.example.aigymapp.ui.viewmodels.WizardStep.CONFIG -> {
                        state.selectedExercise?.let { exercise ->
                            // Use local state wrapper to prevent ViewModel updates on each keystroke
                            ExerciseConfigDialogWithLocalState(
                                exercise = exercise,
                                configDate = state.configDate,
                                selectedDatesCount = state.selectedDates.size,
                                initialSets = state.configSets,
                                initialReps = state.configReps,
                                initialWeight = state.configWeight,
                                initialRest = state.configRest,
                                initialNotes = state.configNotes,
                                lastPerformance = state.lastPerformance,
                                showHeader = false,
                                onDateClick = { viewModel.onConfigDateClick() },
                                onSave = { sets, reps, weight, rest, notes ->
                                    // Update ViewModel only on save
                                    viewModel.onConfigSetsChanged(sets)
                                    viewModel.onConfigRepsChanged(reps)
                                    viewModel.onConfigWeightChanged(weight)
                                    viewModel.onConfigRestChanged(rest)
                                    viewModel.onConfigNotesChanged(notes)
                                    viewModel.onSaveWorkout()
                                },
                                onBack = { viewModel.onWizardBack() },
                                onDismiss = { viewModel.onConfigDialogDismissed() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Edit Dialog - Egzersizi düzenle, "antrenman ekle" akışındaki detay menüsüyle
    // aynı bileşeni kullanır; sadece metinleri düzenleme bağlamına göre değişir.
    if (state.showEditDialog) {
        state.selectedExercise?.let { exercise ->
            ExerciseConfigDialogWithLocalState(
                exercise = exercise,
                configDate = state.configDate,
                selectedDatesCount = 0,
                initialSets = state.configSets,
                initialReps = state.configReps,
                initialWeight = state.configWeight,
                initialRest = state.configRest,
                initialNotes = state.configNotes,
                lastPerformance = state.lastPerformance,
                isEditMode = true,
                onDateClick = { viewModel.onConfigDateClick() },
                onSave = { sets, reps, weight, rest, notes ->
                    viewModel.onConfigSetsChanged(sets)
                    viewModel.onConfigRepsChanged(reps)
                    viewModel.onConfigWeightChanged(weight)
                    viewModel.onConfigRestChanged(rest)
                    viewModel.onConfigNotesChanged(notes)
                    viewModel.onSaveEditedWorkout()
                },
                onBack = { viewModel.onEditDialogDismissed() },
                onDismiss = { viewModel.onEditDialogDismissed() }
            )
        }
    }

    if (state.showDatePicker) {
        WorkoutDatePickerDialog(
            initialDate = state.configDate,
            onDatesSelected = { dates ->
                viewModel.onMultipleDatesSelected(dates)
            },
            onDismiss = { viewModel.onDatePickerDismissed() }
        )
    }
    
        // Delete Confirmation Dialog
        if (state.showDeleteConfirmDialog) {
            DeleteConfirmationDialog(
                date = state.selectedDate,
                workoutCount = state.scheduledWorkouts.size,
                onConfirm = { viewModel.deleteAllWorkoutsForSelectedDate() },
                onDismiss = { viewModel.dismissDeleteConfirmDialog() }
            )
        }
    }
}

/**
 * Staggered entrance for the workout page's five top-level bands (header,
 * calendar strip, category filter, exercise list, morphing bar) — each
 * settles up from 10px below on its own spring while fading in on a
 * separate tween, offset by `index * 70ms` so they cascade into place in
 * sequence rather than all appearing at once. Keyed on [screenKey], which
 * is fresh every time [WorkoutScreen] re-enters composition, so the whole
 * cascade replays each time the tab or screen is reopened.
 */
@Composable
private fun StaggeredBand(
    index: Int,
    screenKey: Any,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { 10.dp.toPx() }
    val alpha = remember(screenKey) { Animatable(0f) }
    val translationY = remember(screenKey) { Animatable(startOffsetPx) }

    LaunchedEffect(screenKey) {
        alpha.snapTo(0f)
        translationY.snapTo(startOffsetPx)
        delay(index * 70L)
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
        launch {
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translationY.value
        }
    ) {
        content()
    }
}

/**
 * "Antrenman Bitti" özeti — günün tüm egzersizleri tamamlandığında otomatik
 * olarak 3 saniyeliğine tam ekran gelir, sonra aynı yoldan aşağı inerek
 * kapanır. Panel slideInVertically(tween(420, FastOutSlowInEasing)) ile
 * altdan gelir; üstteki ince süre çubuğu 3sn boyunca doğrusal olarak dolar
 * ve dolunca overlay'i kapatır; dört ölçü hücresi (süre/egzersiz/set/toplam
 * kg) 80ms aralıklarla, 8px yükselerek belirir.
 */
@Composable
internal fun WorkoutCompleteSummaryOverlay(
    visible: Boolean,
    summary: WorkoutViewModel.WorkoutCompleteSummary?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Üstteki süre çubuğu — görünür kaldığı 3sn boyunca yavaşça dolar,
    // dolunca overlay'i otomatik kapatır.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        ) { fullHeight -> fullHeight },
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        ) { fullHeight -> fullHeight },
        modifier = modifier.fillMaxSize()
    ) {
        val current = summary
        if (current != null) {
            // Her yeni özet gösterimi için taze bir anahtar — hücre animasyonu
            // her açılışta baştan oynar.
            val revealKey = remember(current) { Any() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp, start = 22.dp, end = 22.dp)
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .clip(RoundedCornerShape(2.dp))
                            .background(NeonLime)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(top = 28.dp, start = 22.dp, end = 22.dp)
                ) {
                    Text(
                        text = "ANTRENMAN BİTTİ",
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonLime
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bugün tamamlandı",
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp,
                        color = MD.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceVariant))

                Row(modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                    SummaryMetricCell(
                        label = "SÜRE",
                        value = "${current.durationMinutes}dk",
                        index = 0,
                        revealKey = revealKey,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(DarkSurfaceVariant))
                    SummaryMetricCell(
                        label = "EGZERSİZ",
                        value = "${current.exerciseCount}",
                        index = 1,
                        revealKey = revealKey,
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceVariant))
                Row(modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                    SummaryMetricCell(
                        label = "SET",
                        value = "${current.setCount}",
                        index = 2,
                        revealKey = revealKey,
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(DarkSurfaceVariant))
                    SummaryMetricCell(
                        label = "TOPLAM KG",
                        value = formatTotalKg(current.totalKg),
                        index = 3,
                        revealKey = revealKey,
                        valueColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceVariant))

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonLime)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KAPAT",
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Kapat",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

/**
 * Dinlenme çubuğu — bir egzersiz tamamlandığında, günün başka egzersizi varsa
 * [MorphingWorkoutBar]'ın mini/"current antrenman" alanında [CurrentWorkoutCompact]'ın
 * yerini alır (aynı konum, aynı yükseklik sınıfı). O egzersizin dinlenme süresi kadar
 * geri sayar; altındaki ince çubuk `animateFloatAsState(tween(720, FastOutSlowInEasing))`
 * ile dolar. Süre dolunca ya da GEÇ'e basılınca ViewModel otomatik olarak sıradaki
 * egzersizi current workout yapar ve bu çubuk kaybolur.
 */
@Composable
private fun RestCompactBar(
    totalSeconds: Int,
    remainingSeconds: Int,
    nextWorkoutName: String?,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
        label = "restProgress"
    )
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            // Cell 1 — DİNLENME + Sıradaki egzersiz
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 22.dp, end = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "DİNLENME",
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonLime
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nextWorkoutName?.let { "Sıradaki · $it" } ?: "Sıradaki",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MD.TextDim,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Cell 2 — geri sayım
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .leftRule(1.dp, MD.BorderWeak)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    style = TabularNumsStyle,
                    color = NeonLime
                )
            }

            // Cell 3 — GEÇ
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .leftRule(1.dp, MD.BorderWeak)
                    .clickable(onClick = onSkip),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GEÇ",
                    fontSize = 13.sp,
                    letterSpacing = 1.1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextPrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(MD.BorderWeak)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(NeonLime)
            )
        }
    }
}

/**
 * Tek bir ölçü hücresi (SÜRE, EGZERSİZ, SET, TOPLAM KG) — [revealKey]
 * değiştiğinde alpha tween(300) + translationY spring(dampingRatio = 0.9f)
 * ile belirir, `index * 80ms` gecikmeyle sıraya girer.
 */
@Composable
internal fun SummaryMetricCell(
    label: String,
    value: String,
    index: Int,
    revealKey: Any,
    modifier: Modifier = Modifier,
    valueColor: Color = MD.TextPrimary
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { 8.dp.toPx() }
    val alpha = remember(revealKey) { Animatable(0f) }
    val translationY = remember(revealKey) { Animatable(startOffsetPx) }

    LaunchedEffect(revealKey) {
        alpha.snapTo(0f)
        translationY.snapTo(startOffsetPx)
        delay(index * 80L)
        launch {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 300))
        }
        launch {
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationY = translationY.value
            }
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

internal fun formatTotalKg(totalKg: Double): String {
    val rounded = totalKg.toLong()
    val digits = rounded.toString()
    val grouped = StringBuilder()
    for ((i, char) in digits.withIndex()) {
        val fromEnd = digits.length - i
        if (fromEnd != digits.length && fromEnd % 3 == 0) grouped.append('.')
        grouped.append(char)
    }
    return grouped.toString()
}

/**
 * Day header — "Bugün · 28 Ağustos" / "Antrenman" title + lime add-square.
 */
@Composable
private fun WorkoutDayHeader(
    selectedDate: LocalDate,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dayLabel = if (selectedDate == today) "Bugün" else getDayNameTurkish(selectedDate)
    val dateLabel = "$dayLabel · ${selectedDate.dayOfMonth} ${getMonthNameTurkish(selectedDate.monthNumber)}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MD.Bg)
            .padding(start = 22.dp, end = 22.dp, top = 11.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = dateLabel.uppercase(),
                fontSize = 10.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Antrenman",
                fontSize = 23.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.46).sp,
                color = MD.TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MD.Lime)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Yeni Antrenman Ekle",
                tint = MD.OnLime,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * Muscle-group filter chip row — client-side filter over the day's list.
 */
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .topRule(2.dp, MD.BorderStrong)
            .horizontalScroll(rememberScrollState())
            .padding(start = 22.dp, end = 22.dp, top = 9.dp, bottom = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(label = "TÜMÜ", isSelected = selected == null, onClick = { onSelect(null) })
        categories.forEach { category ->
            CategoryChip(
                label = category.uppercase(),
                isSelected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .then(
                if (isSelected) Modifier.background(MD.Lime)
                else Modifier.border(2.dp, MD.BorderStrong)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            lineHeight = 9.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) MD.OnLime else MD.TextMuted,
            style = LocalTextStyle.current.copy(
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}

/**
 * Workout List — flush numbered list, flat Modernist Dark style. When
 * [reorderEnabled] is true, each row exposes a drag handle (≡) that lets the
 * user long-press-free drag it to a new position; [onReorder] fires once per
 * drop with the full reordered list so the caller can persist it. Every row
 * can also be swiped left to reveal a delete action.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutList(
    workouts: List<GetScheduledForDate>,
    selectedDate: LocalDate,
    selectedCurrentWorkoutId: Long?,
    completedWorkoutIds: Set<Long>,
    onWorkoutClick: (GetScheduledForDate) -> Unit,
    onDeleteClick: (GetScheduledForDate) -> Unit,
    onEditClick: (GetScheduledForDate) -> Unit,
    onSetAsCurrentClick: (GetScheduledForDate) -> Unit,
    onCompletedToggleClick: (GetScheduledForDate) -> Unit,
    isCurrentWorkoutRunning: Boolean = false,
    isAnyWorkoutRunning: Boolean = false,
    reorderEnabled: Boolean = false,
    onReorder: (List<GetScheduledForDate>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val isToday = selectedDate == today

    // Mirrors `workouts` locally so a drag can reorder rows instantly, without
    // waiting on the DB round-trip. Resets whenever the upstream list changes
    // (new data loaded, item added/removed, or the persisted order comes back).
    var orderedItems by remember(workouts) { mutableStateOf(workouts) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val autoScrollEdgePx = remember(density) { with(density) { 56.dp.toPx() } }
    val haptic = LocalHapticFeedback.current

    var draggingKey by remember { mutableStateOf<Long?>(null) }
    var draggingOffset by remember { mutableStateOf(0f) }

    // Reorder logic is driven off `listState.layoutInfo`'s real on-screen item positions
    // rather than a manually-tracked "row height" — that stays correct no matter how many
    // items are in the list or which one is mid-drag, including once a longer list has
    // scrolled so the dragged row's neighbors are no longer the ones it started next to.
    fun onItemDragStart(key: Long) {
        draggingKey = key
        draggingOffset = 0f
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onItemDrag(delta: Float) {
        val key = draggingKey ?: return

        val layoutInfo = listState.layoutInfo
        val draggingItem = layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return

        // Don't let the dragged row's top pass above the top of the list's own viewport —
        // without this it can be pulled arbitrarily far up, off screen.
        val minOffset = (layoutInfo.viewportStartOffset - draggingItem.offset).toFloat()
        draggingOffset = (draggingOffset + delta).coerceAtLeast(minOffset)

        val draggingCenter = draggingItem.offset + draggingOffset + draggingItem.size / 2f

        val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { candidate ->
            candidate.key != key &&
                draggingCenter >= candidate.offset &&
                draggingCenter <= candidate.offset + candidate.size
        }
        if (targetItem != null) {
            val fromIndex = orderedItems.indexOfFirst { it.sw_id == key }
            val toIndex = orderedItems.indexOfFirst { it.sw_id == targetItem.key }
            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                orderedItems = orderedItems.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                // The dragged row's layout slot just moved by the swapped item's size —
                // compensate so it stays visually anchored under the finger.
                draggingOffset += draggingItem.offset - targetItem.offset
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        // Auto-scroll while the dragged row is held near either edge of the viewport, so
        // reordering across more items than fit on screen isn't stuck at the edge.
        val currentTop = draggingItem.offset + draggingOffset
        val currentBottom = currentTop + draggingItem.size
        val scrollDelta = when {
            currentBottom > layoutInfo.viewportEndOffset - autoScrollEdgePx -> autoScrollEdgePx / 4f
            currentTop < layoutInfo.viewportStartOffset + autoScrollEdgePx -> -autoScrollEdgePx / 4f
            else -> 0f
        }
        if (scrollDelta != 0f) {
            coroutineScope.launch { listState.scrollBy(scrollDelta) }
        }
    }

    fun onItemDragEnd() {
        draggingKey = null
        draggingOffset = 0f
        onReorder(orderedItems)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // room for the fixed current-workout footer
    ) {
        itemsIndexed(items = orderedItems, key = { _, item -> item.sw_id }) { index, workout ->
            val isDragging = workout.sw_id == draggingKey
            val currentWorkout by rememberUpdatedState(workout)
            val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                        onDeleteClick(currentWorkout)
                    }
                    true
                }
            )
            Box(
                modifier = Modifier
                    // The dragged row is driven by raw finger deltas via translationY below —
                    // animating its placement too would fight that and lag behind the finger.
                    // Everything it displaces gets the placement animation, so the swap reads
                    // as items sliding into the new slot rather than snapping there instantly.
                    .then(
                        if (isDragging) Modifier
                        else Modifier.animateItem(placementSpec = spring(dampingRatio = 0.9f))
                    )
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) draggingOffset else 0f
                        alpha = if (isDragging) 0.7f else 1f
                        scaleX = if (isDragging) 1.02f else 1f
                        scaleY = if (isDragging) 1.02f else 1f
                    }
            ) {
                androidx.compose.material3.SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = { SwipeToDeleteBackground(dismissState) }
                ) {
                    WorkoutItemCard(
                        showTopDivider = index > 0,
                        workout = workout,
                        isSelectedAsCurrent = selectedCurrentWorkoutId == workout.sw_id,
                        isCompleted = workout.sw_id in completedWorkoutIds,
                        isTimerRunning = isCurrentWorkoutRunning && selectedCurrentWorkoutId == workout.sw_id,
                        isToday = isToday,
                        isAnyWorkoutRunning = isAnyWorkoutRunning,
                        showDragHandle = reorderEnabled,
                        onClick = { onWorkoutClick(workout) },
                        onDeleteClick = { onDeleteClick(workout) },
                        onEditClick = { onEditClick(workout) },
                        onSetAsCurrentClick = { onSetAsCurrentClick(workout) },
                        onCompletedToggleClick = { onCompletedToggleClick(workout) },
                        onDragStart = { onItemDragStart(workout.sw_id) },
                        onDrag = { delta -> onItemDrag(delta) },
                        onDragEnd = { onItemDragEnd() }
                    )
                }
            }
        }
    }
}

/**
 * Red delete backdrop revealed behind a workout row as it's swiped left.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteBackground(state: androidx.compose.material3.SwipeToDismissBoxState) {
    // The red panel's width tracks the actual drag offset, so it grows in lockstep
    // with the reveal instead of a fixed-size patch popping in at full width — the
    // area behind the row stays the normal page background everywhere else.
    val density = LocalDensity.current
    val revealWidth = with(density) {
        val offsetPx = try {
            kotlin.math.abs(state.requireOffset())
        } catch (e: IllegalStateException) {
            0f
        }
        offsetPx.toDp()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(revealWidth)
                .background(ErrorRed),
            contentAlignment = Alignment.Center
        ) {
            if (revealWidth > 44.dp) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * "SİLİNDİ · <EGZERSİZ ADI>" geri al çubuğu — [WorkoutList]'ten bir antrenman
 * silindiğinde, aktif antrenman çubuğunun hemen üstünde belirir. "GERİ AL"a
 * basılana ya da geri alma penceresi dolana kadar açık kalır.
 */
@Composable
private fun DeleteUndoBar(
    exerciseName: String,
    onUndoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MD.Bg)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MD.BorderWeak))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SİLİNDİ · ${exerciseName.uppercase()}",
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.Bold,
                color = MD.TextMuted,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "GERİ AL",
                fontSize = 13.sp,
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.Lime,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable(onClick = onUndoClick)
            )
        }
    }
}

/**
 * Workout Item Card — flush numbered row: index · name + sets×reps·kg · status square.
 * The active (current) workout gets a lime left bar + lime accent; a genuinely
 * completed workout (all sets checked off) shows a filled checkmark — tappable
 * directly to toggle completion; everything else is an empty outline.
 */
@Composable
private fun WorkoutItemCard(
    showTopDivider: Boolean,
    workout: GetScheduledForDate,
    isSelectedAsCurrent: Boolean,
    isCompleted: Boolean,
    isToday: Boolean,
    isTimerRunning: Boolean = false,
    isAnyWorkoutRunning: Boolean = false,
    showDragHandle: Boolean = false,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSetAsCurrentClick: () -> Unit,
    onCompletedToggleClick: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isClickable = isToday && (!isAnyWorkoutRunning || isSelectedAsCurrent)
    // Sadece gerçekten tamamlanmış (tüm setleri işaretlenmiş) egzersizler işaretli görünür —
    // başka bir egzersize geçmek veya süre biriktirmiş olmak tek başına "tamamlandı" saydırmaz.
    // Seçili (current) olması işareti gizlemez — tamamlanmışsa öyle kalır.
    val isDone = isCompleted

    var sheetVisible by remember { mutableStateOf(false) }

    val nameColor = MD.TextPrimary
    val subColor = when {
        isSelectedAsCurrent -> MD.Lime
        !isClickable -> MD.TextFaint
        isDone -> MD.TextMuted
        else -> MD.TextDim
    }

    // Sürükleme artık ayrı bir tutamaç değil, kartın tamamı üzerinden çalışır — basılı
    // tutup bekleyince (uzun basma) devreye girer, düz bir dokunuş normal tıklamayı
    // (onClick) tetiklemeye devam eder. rememberUpdatedState olmadan pointerInput(Unit)
    // yalnızca ilk kompozisyondaki callback'lere bağlı kalır.
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .then(if (isSelectedAsCurrent) Modifier.leftRule(3.dp, MD.Lime) else Modifier)
            .clickable(enabled = isClickable, onClick = onClick)
            .then(
                if (showDragHandle) Modifier.pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { currentOnDragStart() },
                        onDragEnd = { currentOnDragEnd() },
                        onDragCancel = { currentOnDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentOnDrag(dragAmount.y)
                        }
                    )
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isSelectedAsCurrent) 19.dp else 22.dp,
                    end = 10.dp,
                    top = 14.dp,
                    bottom = 14.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Egzersiz görsel kutusu — şimdilik her zaman boş: halter yer tutucusu +
            // sağ alt köşeye oturan kas grubu etiketi (düz neon yeşil yazı, kutusuz).
            // Gerçek fotoğraf kaydı yok, kutu boşken de satır düzeni bozulmuyor.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(ExerciseImagePlaceholderBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CustomIcons.DumbbellOutlined,
                    contentDescription = null,
                    tint = MD.TextFaint,
                    modifier = Modifier.size(24.dp)
                )
                workout.exercise_muscle_group?.let { muscleGroup ->
                    Text(
                        text = muscleGroup.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        color = MD.Lime,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp, end = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                Text(
                    text = workout.exercise_name,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = nameColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${workout.wpi_sets} set · ${workout.wpi_reps} tekrar · ${workout.wpi_weight_kg.toInt()} kg",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = subColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Tik kutusu tamamlandığında bir kez 1.2x büyüyüp geri dönen sıçrama animasyonu.
            val checkScale = remember { Animatable(1f) }
            LaunchedEffect(isDone) {
                if (isDone) {
                    checkScale.animateTo(
                        targetValue = 1.2f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                    )
                    checkScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                    )
                } else {
                    checkScale.snapTo(1f)
                }
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = checkScale.value
                        scaleY = checkScale.value
                    }
                    .clickable(enabled = isToday, onClick = onCompletedToggleClick)
                    .then(
                        when {
                            isDone -> Modifier.background(MD.Lime)
                            isSelectedAsCurrent -> Modifier.border(2.dp, MD.Lime)
                            else -> Modifier.border(2.dp, MD.BorderStrong)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = MD.OnLime,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(enabled = !isTimerRunning) { sheetVisible = true },
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(MD.TextDim)
                        )
                    }
                }
            }
        }
    }

    if (sheetVisible) {
        ExerciseActionsSheet(
            exerciseName = workout.exercise_name,
            onDismiss = { sheetVisible = false },
            onSetAsCurrentClick = onSetAsCurrentClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

/**
 * Kart üstündeki üç noktaya basınca açılan eylem menüsü — kare köşeli, alttan
 * açılan bir sheet: egzersiz adı başlık, altında üç eylem satırı (aktif yap,
 * set/tekrar düzenle, listeden çıkar — bu sonuncusu tuğla renginde).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseActionsSheet(
    exerciseName: String,
    onDismiss: () -> Unit,
    onSetAsCurrentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MD.Bg,
        scrimColor = Color(0xBF000000),
        shape = RoundedCornerShape(0.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(2.dp)
                    .background(MD.BorderStrong)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = exerciseName,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 16.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MD.BorderStrong))
            ExerciseSheetActionRow(
                label = "Şuradan devam et",
                hint = "AKTİF YAP",
                labelColor = MD.TextPrimary,
                showTopDivider = false,
                onClick = { onDismiss(); onSetAsCurrentClick() }
            )
            ExerciseSheetActionRow(
                label = "Set ve tekrarı düzenle",
                hint = "",
                labelColor = MD.TextPrimary,
                showTopDivider = true,
                onClick = { onDismiss(); onEditClick() }
            )
            ExerciseSheetActionRow(
                label = "Listeden çıkar",
                hint = "",
                labelColor = ErrorRed,
                showTopDivider = true,
                onClick = { onDismiss(); onDeleteClick() }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExerciseSheetActionRow(
    label: String,
    hint: String,
    labelColor: Color,
    showTopDivider: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 17.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = labelColor)
        if (hint.isNotEmpty()) {
            Text(
                text = hint,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = MD.TextFaint
            )
        }
    }
}

/**
 * Calendar Strip Section - Apple Fitness Style
 * Haftalık görünüm, yuvarlak günler, swipe ile hafta değiştirme
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CalendarStripSection(
    monthYear: String,
    weekDays: List<DateItem>,
    selectedDate: LocalDate,
    hasWorkouts: Boolean,
    dayStatuses: Map<LocalDate, com.example.aigymapp.ui.viewmodels.DayStatus>,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (Int) -> Unit,
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Başlangıç sayfası (Sonsuz kaydırma için orta nokta)
    val initialPage = Int.MAX_VALUE / 2
    
    // Pager state
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = initialPage
    ) { Int.MAX_VALUE }
    
    // Referans tarih (Pager'ın 0 noktası - usually the first loaded week's Monday)
    // weekDays değişse bile anchor sabit kalmalı ki pager pozisyonu bozulmasın
    val anchorDate = remember { weekDays.first().date }
    
    // ViewModel ile senkronizasyon için state
    var lastSelectedWeekStart by remember { mutableStateOf(weekDays.first().date) }

    // Eğer dışarıdan hafta değişirse (örn. tarih seçimiyle), Pager'ı güncelle
    LaunchedEffect(weekDays) {
        val currentWeekStart = weekDays.first().date
        if (currentWeekStart != lastSelectedWeekStart) {
            // Anchor ile Current arasındaki hafta farkını hesapla
            // Formül: Page = Initial + (Current - Anchor) / 7
            val daysDiff = anchorDate.daysUntil(currentWeekStart)
            val weekOffset = daysDiff / 7
            
            val targetPage = initialPage + weekOffset
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
            lastSelectedWeekStart = currentWeekStart
        }
    }

    // Pager değişince ViewModel'i güncelle
    LaunchedEffect(pagerState.settledPage) {
        val currentPage = pagerState.settledPage
        // Current page'in temsil ettiği hafta başı
        val pageOffset = currentPage - initialPage
        val pageWeekStart = anchorDate.plus(pageOffset, DateTimeUnit.WEEK)
        
        // Eğer ViewModel'deki haftadan farklıysa güncelle
        if (pageWeekStart != lastSelectedWeekStart) {
            val weekDelta = lastSelectedWeekStart.daysUntil(pageWeekStart) / 7
            if (weekDelta != 0) {
                onWeekChanged(weekDelta)
                lastSelectedWeekStart = pageWeekStart
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MD.Bg)
            .topRule(2.dp, MD.BorderStrong)
    ) {
        // Quiet section label + week workout count; the count itself opens the
        // overflow menu for the destructive "delete all" action (kept reachable,
        // just folded into the count label instead of a separate ⋮ icon).
        var headerMenuExpanded by remember { mutableStateOf(false) }
        val weekWorkoutCount = weekDays.count { day ->
            dayStatuses[day.date]?.let { it != com.example.aigymapp.ui.viewmodels.DayStatus.NONE } == true
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYear.uppercase(),
                fontSize = 9.sp,
                letterSpacing = 1.44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )

            // Tapping the count reveals delete-all only when there are workouts
            Box {
                Text(
                    text = "$weekWorkoutCount ANTRENMAN",
                    fontSize = 9.sp,
                    letterSpacing = 1.26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextDim,
                    modifier = Modifier.clickable { headerMenuExpanded = true }
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = headerMenuExpanded,
                    onDismissRequest = { headerMenuExpanded = false },
                    modifier = Modifier.background(MD.Bg)
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Text(
                                text = "Tümünü Sil",
                                color = if (hasWorkouts) ErrorRed else MD.TextDim,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (hasWorkouts) ErrorRed else MD.TextDim,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        enabled = hasWorkouts,
                        onClick = {
                            headerMenuExpanded = false
                            onDeleteAllClick()
                        }
                    )
                }
            }
        }

        // Apple Fitness Style Horizontal Pager
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .topRule(1.dp, MD.BorderWeak),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            // Bu sayfa için günleri hesapla
            val pageOffset = page - initialPage
            val pageWeekStart = anchorDate.plus(pageOffset, DateTimeUnit.WEEK)

            // 7 günü oluştur
            val days = remember(page) {
                (0..6).map { dayIndex ->
                    val date = pageWeekStart.plus(dayIndex, DateTimeUnit.DAY)
                    DateItem(
                        date = date,
                        dayName = getDayNameTurkish(date),
                        dayNumber = date.dayOfMonth.toString()
                    )
                }
            }

            // Bugünün tarihi (Highlight için)
            val today = remember { kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date }

            val selectedIndexInPage = days.indexOfFirst { it.date == selectedDate }
            val density = LocalDensity.current
            var rowWidthPx by remember(page) { mutableStateOf(0) }
            var rowHeightPx by remember(page) { mutableStateOf(0) }
            val columnWidth = with(density) { (rowWidthPx / 7).toDp() }
            val rowHeight = with(density) { rowHeightPx.toDp() }
            val fillOffsetX by animateDpAsState(
                targetValue = columnWidth * selectedIndexInPage.coerceAtLeast(0),
                animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                if (rowWidthPx > 0 && rowHeightPx > 0 && selectedIndexInPage >= 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = fillOffsetX)
                            .width(columnWidth)
                            .height(rowHeight)
                            .background(MD.Lime)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            rowWidthPx = it.width
                            rowHeightPx = it.height
                        }
                ) {
                    days.forEach { day ->
                        val status = dayStatuses[day.date]
                        CircularDayItem(
                            dayName = day.dayName,
                            dayNumber = day.dayNumber,
                            isSelected = day.date == selectedDate,
                            isToday = day.date == today,
                            status = status,
                            onClick = { onDateSelected(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular Day Item - Apple Fitness Style
 * Yuvarlak gün gösterimi
 */
@Composable
private fun CircularDayItem(
    dayName: String,
    dayNumber: String,
    isSelected: Boolean,
    isToday: Boolean = false,
    status: com.example.aigymapp.ui.viewmodels.DayStatus? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseIndicatorColor = when (status) {
        com.example.aigymapp.ui.viewmodels.DayStatus.GREEN -> MD.Lime
        com.example.aigymapp.ui.viewmodels.DayStatus.ORANGE -> PartialYellow
        com.example.aigymapp.ui.viewmodels.DayStatus.RED -> ErrorRed
        else -> Color.Transparent
    }

    val labelColor by animateColorAsState(
        targetValue = when {
            isSelected -> MD.OnLime
            isToday -> MD.Lime
            else -> MD.TextDim
        },
        animationSpec = tween(220)
    )
    val numberColor by animateColorAsState(
        targetValue = if (isSelected) MD.OnLime else MD.TextPrimary,
        animationSpec = tween(220)
    )
    val indicatorColor by animateColorAsState(
        targetValue = baseIndicatorColor,
        animationSpec = tween(220)
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .padding(top = 8.dp, bottom = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName.uppercase(),
            fontSize = 8.sp,
            letterSpacing = 0.96.sp,
            fontWeight = FontWeight.ExtraBold,
            color = labelColor
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = dayNumber,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = numberColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(3.dp)
                .background(indicatorColor)
        )
    }
}

/**
 * Navigation Arrow Button
 */
@Composable
private fun NavigationArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Date Item Card
 */
@Composable
private fun DateItemCard(
    date: DateItem,
    isSelected: Boolean,
    dayStatus: com.example.aigymapp.ui.viewmodels.DayStatus? = null,
    totalTimeMs: Long = 0L,
    onClick: () -> Unit
) {
    // Arka plan rengi - Seçili ise neon yeşil, değilse koyu
    val backgroundColor = if (isSelected) NeonLime else DarkSurface
    val textColor = if (isSelected) Color.Black else TextPrimary
    
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Ana kart (5 güne sığacak boyut)
        Column(
            modifier = Modifier
                .width(64.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayNumber,
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date.dayName,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
        
        // Toplam süre göstergesi (kart altında)
        if (totalTimeMs > 0) {
            Text(
                text = formatElapsedTimeShort(totalTimeMs),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) NeonLime else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        color = if (isSelected) DarkSurface else DarkSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        } else {
            // Boş alan - diğer kartlarla hizalamak için
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Format elapsed time to short format (e.g., "12:34" or "1h 23m")
 */
private fun formatElapsedTimeShort(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else if (minutes > 0) {
        "${minutes}m"
    } else {
        "< 1m"
    }
}

/**
 * Empty Workout State
 */
@Composable
private fun EmptyWorkoutState(
    modifier: Modifier = Modifier
) {
    // Compact centered empty state — sits snugly below the calendar strip
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = "Egzersiz yok",
            tint = MD.TextFaint,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = "Bugün için egzersiz eklenmedi",
            fontSize = 14.sp,
            color = MD.TextDim,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Morphing "current workout" bar — Spotify mini-player style. A single
 * [expandProgress] (0 = mini bar, 1 = full-screen detail) drives the
 * container's height; the full detail content is always laid out at its
 * natural full size and anchored to the bottom, so the growing/shrinking
 * box is a real "curtain" reveal of that same content rather than an empty
 * box that pops content in later — no fade/alpha blending anywhere (that
 * read as a translucent "shadow" during the transition). The resting mini
 * bar is a plain opaque overlay shown only while essentially collapsed.
 */
@Composable
private fun MorphingWorkoutBar(
    workout: com.example.aigymapp.ui.viewmodels.CurrentWorkoutState?,
    exerciseIndex: Int,
    totalExercises: Int,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onClearClick: () -> Unit,
    onCompletedToggle: (Boolean) -> Unit,
    isResting: Boolean = false,
    restTotalSeconds: Int = 0,
    restSecondsRemaining: Int = 0,
    restNextWorkoutName: String? = null,
    onSkipRest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val expandProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // If the active workout is cleared while expanded, collapse instantly — there's
    // nothing left to show full-screen.
    LaunchedEffect(workout == null) {
        if (workout == null) expandProgress.snapTo(0f)
    }

    // Spring settle — spatially continuous with whatever speed the user was
    // already dragging/flinging at (initialVelocity), so the release never
    // visually "jumps" from the finger's motion into the settle animation.
    fun settle(target: Float, initialVelocity: Float = 0f) {
        scope.launch {
            expandProgress.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialVelocity = initialVelocity
            )
        }
    }

    // Dinlenme başladığında bar açık/genişletilmişse anında toparlanır — kullanıcı
    // dinlenme çubuğunu her zaman görsün (tam ekran detay onu gizlemesin).
    LaunchedEffect(isResting) {
        if (isResting) settle(0f)
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
    ) {
        // Dinlenme çubuğu (RestCompactBar) normal kompakt bardan (CurrentWorkoutCompact,
        // 56dp + 2dp üst çizgi) daha yüksek (72dp satır + 3dp ilerleme çubuğu) — collapsed
        // yükseklik buna göre animasyonlu şekilde büyür/küçülür, yoksa alt kısmı kırpılır
        // ya da dinlenmeye geçiş sert bir sıçrama gibi görünür.
        val miniHeight by animateDpAsState(
            targetValue = if (isResting) 80.dp else 58.dp,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            label = "compactBarHeight"
        )
        val fullHeight = this.maxHeight
        val dragRangePx = with(androidx.compose.ui.platform.LocalDensity.current) {
            (fullHeight - miniHeight).toPx()
        }.coerceAtLeast(1f)
        val barHeight = miniHeight + (fullHeight - miniHeight) * expandProgress.value

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(barHeight)
                .clipToBounds()
                .background(MD.Bg)
                .then(
                    if (workout != null && !isResting) {
                        // Gesture handling lives on this OUTER box (an ancestor of every
                        // button/pager inside the content below) and reads pointer events
                        // in the Initial pass — i.e. before its descendants get a look —
                        // so a vertical drag is reliably claimed no matter where on the
                        // full-screen content it starts, not just near the very top.
                        Modifier
                            .pointerInput(workout.workoutId, dragRangePx) {
                                val touchSlop = viewConfiguration.touchSlop
                                awaitEachGesture {
                                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                    val velocityTracker = VelocityTracker()
                                    velocityTracker.addPointerInputChange(down)
                                    var accumulatedY = 0f
                                    var accumulatedX = 0f
                                    var decided = false
                                    var claimedVertical = false
                                    while (true) {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        velocityTracker.addPointerInputChange(change)
                                        if (!change.pressed) {
                                            if (claimedVertical) {
                                                // Fast flick wins outright regardless of position;
                                                // otherwise fall back to the halfway position.
                                                val flingPxPerSec = velocityTracker.calculateVelocity().y
                                                val target = when {
                                                    flingPxPerSec < -900f -> 1f
                                                    flingPxPerSec > 900f -> 0f
                                                    else -> if (expandProgress.value > 0.4f) 1f else 0f
                                                }
                                                settle(target, initialVelocity = -flingPxPerSec / dragRangePx)
                                            }
                                            break
                                        }
                                        val dy = change.positionChange().y
                                        val dx = change.positionChange().x
                                        if (!decided) {
                                            accumulatedY += dy
                                            accumulatedX += dx
                                            if (kotlin.math.abs(accumulatedY) > touchSlop &&
                                                kotlin.math.abs(accumulatedY) > kotlin.math.abs(accumulatedX)
                                            ) {
                                                decided = true
                                                claimedVertical = true
                                            } else if (kotlin.math.abs(accumulatedX) > touchSlop) {
                                                decided = true
                                                claimedVertical = false
                                            }
                                        }
                                        if (claimedVertical) {
                                            change.consume()
                                            val next = (expandProgress.value - dy / dragRangePx).coerceIn(0f, 1f)
                                            scope.launch { expandProgress.snapTo(next) }
                                        }
                                    }
                                }
                            }
                            .clickable(
                                enabled = expandProgress.value < 0.06f,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { settle(1f) }
                            )
                    } else Modifier
                )
        ) {
            if (workout != null) {
                // Full content is always laid out at its natural full height and
                // anchored to the bottom, so the growing/shrinking box just reveals
                // or hides more of it — a real "curtain" reveal instead of an empty
                // box that suddenly swaps to content at some threshold.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fullHeight)
                        .align(Alignment.BottomCenter)
                ) {
                    FullScreenWorkoutSheetContent(
                        workout = workout,
                        exerciseIndex = exerciseIndex,
                        totalExercises = totalExercises,
                        onDismiss = { settle(0f) },
                        onStartPauseClick = onStartPauseClick,
                        onResetClick = onResetClick,
                        onClearClick = {
                            settle(0f)
                            onClearClick()
                        },
                        onCompletedToggle = onCompletedToggle
                    )
                }

                // Collapsed mini bar — only shown while (nearly) fully collapsed; the
                // instant a drag/tap starts, the reveal above takes over. While resting,
                // this shows the dinlenme (rest) bar instead of the normal compact bar.
                if (expandProgress.value < 0.06f) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MD.Bg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MD.BorderStrong)
                        )
                        // Normal kompakt bar <-> dinlenme çubuğu geçişi animasyonlu:
                        // eski içerik yukarı kayıp solarken yenisi altdan kayıp belirir.
                        AnimatedContent(
                            targetState = isResting,
                            transitionSpec = {
                                (fadeIn(tween(260, delayMillis = 90, easing = FastOutSlowInEasing)) +
                                    slideInVertically(tween(260, delayMillis = 90, easing = FastOutSlowInEasing)) { height -> height / 2 })
                                    .togetherWith(
                                        fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                                            slideOutVertically(tween(160, easing = FastOutSlowInEasing)) { height -> -height / 2 }
                                    )
                            },
                            label = "compactBarContent"
                        ) { resting ->
                            if (resting) {
                                RestCompactBar(
                                    totalSeconds = restTotalSeconds,
                                    remainingSeconds = restSecondsRemaining,
                                    nextWorkoutName = restNextWorkoutName,
                                    onSkip = onSkipRest
                                )
                            } else {
                                CurrentWorkoutCompact(
                                    workout = workout,
                                    onStartPauseClick = onStartPauseClick,
                                    onResetClick = onResetClick,
                                    onCompletedToggle = onCompletedToggle
                                )
                            }
                        }
                    }
                }
            } else {
                CurrentWorkoutPlaceholder()
            }
        }
    }
}

/**
 * Shown when no workout is selected as current.
 */
@Composable
private fun CurrentWorkoutPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aktif antrenman yok",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MD.TextMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Antrenman eklemek için bir antrenmana tıklayınız",
            fontSize = 13.sp,
            color = MD.TextDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

/**
 * Play/pause icon that crossfades between states instead of swapping
 * instantly — the two icons sit on top of each other, one fading out as the
 * other fades in, so the surrounding box never jitters.
 */
@Composable
private fun PlayPauseIcon(
    isRunning: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
) {
    Crossfade(
        targetState = isRunning,
        animationSpec = tween(200),
        modifier = modifier
    ) { running ->
        Icon(
            imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (running) "Duraklat" else "Başlat",
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Timer digit color: lime while running, white once paused — eased over 300ms. */
@Composable
private fun rememberElapsedTimeColor(isRunning: Boolean, activeColor: Color, idleColor: Color): Color {
    val color by animateColorAsState(
        targetValue = if (isRunning) activeColor else idleColor,
        animationSpec = tween(300),
        label = "elapsedTimeColor"
    )
    return color
}

private val TabularNumsStyle = TextStyle(fontFeatureSettings = "tnum")

/**
 * Active workout content — one 56dp-tall row, split into four cells by
 * hairline dividers: name + set count, timer, play/pause, complete. Reset
 * (SIFIRLA) has no button of its own here — it's a long-press on the timer
 * cell, keeping the row to exactly four cells.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CurrentWorkoutCompact(
    workout: com.example.aigymapp.ui.viewmodels.CurrentWorkoutState,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onCompletedToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        // Cell 1 — exercise name
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 22.dp, end = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = workout.name,
                fontSize = 15.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MD.TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )
        }

        // Cell 2 — elapsed timer; long-press resets it
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .leftRule(1.dp, MD.BorderWeak)
                .combinedClickable(
                    enabled = !workout.isCompleted,
                    onClick = {},
                    onLongClick = onResetClick
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatElapsedTime(workout.elapsedTimeMs),
                fontSize = 19.sp,
                letterSpacing = (-0.57).sp,
                fontWeight = FontWeight.ExtraBold,
                style = TabularNumsStyle,
                color = rememberElapsedTimeColor(workout.isRunning, MD.Lime, MD.TextPrimary)
            )
        }

        // Cell 3 — play/pause
        Box(
            modifier = Modifier
                .width(46.dp)
                .fillMaxHeight()
                .leftRule(1.dp, MD.BorderWeak)
                .clickable(enabled = !workout.isCompleted, onClick = onStartPauseClick),
            contentAlignment = Alignment.Center
        ) {
            PlayPauseIcon(
                isRunning = workout.isRunning,
                tint = if (workout.isCompleted) MD.TextFaint else MD.TextPrimary,
                iconSize = 17.dp
            )
        }

        // Cell 4 — complete
        Box(
            modifier = Modifier
                .width(104.dp)
                .fillMaxHeight()
                .background(MD.Lime)
                .clickable { onCompletedToggle(!workout.isCompleted) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TAMAMLA",
                fontSize = 13.sp,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.OnLime,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
            )
        }
    }
}

/**
 * Expanded workout content - Full details like home page design
 */
@Composable
private fun CurrentWorkoutExpanded(
    workout: com.example.aigymapp.ui.viewmodels.CurrentWorkoutState,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onClearClick: () -> Unit,
    onCompletedToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with collapse indicator and clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                IconButton(
                    onClick = onClearClick,
                    enabled = !workout.isRunning,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kaldır",
                        tint = if (workout.isRunning) TextSecondary.copy(alpha = 0.3f) else ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Collapse indicator
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Daralt",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Timer and Control Buttons - Like home page
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer Display - Big like home page
            Text(
                text = formatElapsedTime(workout.elapsedTimeMs),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = rememberElapsedTimeColor(workout.isRunning, NeonLime, TextPrimary)
            )
            
            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play/Pause Button - Disabled when completed
                FloatingActionButton(
                    onClick = { if (!workout.isCompleted) onStartPauseClick() },
                    containerColor = when {
                        workout.isCompleted -> DarkSurfaceVariant // Gray when completed
                        workout.isRunning -> ErrorRed 
                        else -> NeonLime
                    },
                    contentColor = when {
                        workout.isCompleted -> TextSecondary
                        workout.isRunning -> Color.White 
                        else -> Color.Black
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    PlayPauseIcon(
                        isRunning = workout.isRunning,
                        tint = when {
                            workout.isCompleted -> TextSecondary
                            workout.isRunning -> Color.White
                            else -> Color.Black
                        },
                        iconSize = 28.dp
                    )
                }

                // Check Button (Complete)
                FloatingActionButton(
                    onClick = { onCompletedToggle(!workout.isCompleted) },
                    containerColor = if (workout.isCompleted) NeonLime else Color(0xFFFF9800),
                    contentColor = Color.Black,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamla",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Stats Row - Like home page
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sets
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Set Sayısı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = workout.sets.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(DarkBackground)
            )
            
            // Reps
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = workout.reps.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(DarkBackground)
            )
            
            // Weight
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${workout.weight.toInt()} kg",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Muscle group badge if available
        workout.muscleGroup?.let { muscleGroup ->
            Text(
                text = muscleGroup,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonLime,
                modifier = Modifier
                    .background(
                        color = NeonLime.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Detail Chip - Small info chip for sets/reps/weight/rest
 */
@Composable
private fun DetailChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = DarkBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Content for the expanded (progress = 1) state of [MorphingWorkoutBar].
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun FullScreenWorkoutSheetContent(
    workout: com.example.aigymapp.ui.viewmodels.CurrentWorkoutState,
    exerciseIndex: Int,
    totalExercises: Int,
    onDismiss: () -> Unit,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onClearClick: () -> Unit,
    onCompletedToggle: (Boolean) -> Unit
) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MD.Bg)
    ) {
        // Header: collapse (chevron-down) · 2-page indicator · clear (x)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 22.dp, end = 22.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, MD.BorderStrong)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Küçült",
                    tint = MD.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .background(if (pagerState.currentPage == index) MD.Lime else MD.TextFaint)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, MD.BorderStrong)
                    .clickable(enabled = !workout.isRunning, onClick = onClearClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Antrenmanı Kaldır",
                    tint = if (workout.isRunning) MD.TextFaint else MD.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Horizontal Pager for Details and Video
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> WorkoutDetailsPage(
                    workout = workout,
                    exerciseIndex = exerciseIndex,
                    totalExercises = totalExercises,
                    onStartPauseClick = onStartPauseClick,
                    onResetClick = onResetClick,
                    onCompletedToggle = onCompletedToggle
                )
                1 -> VideoPage()
            }
        }
    }
}

/**
 * First page - Workout Details (Figma pixel-perfect alignment)
 */
@Composable
private fun WorkoutDetailsPage(
    workout: com.example.aigymapp.ui.viewmodels.CurrentWorkoutState,
    exerciseIndex: Int,
    totalExercises: Int,
    onStartPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onCompletedToggle: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Identity row — icon square · muscle pill + index · exercise name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).border(2.dp, MD.BorderStrong),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MD.Lime,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    workout.muscleGroup?.let { muscleGroup ->
                        Text(
                            text = muscleGroup.uppercase(),
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.OnLime,
                            modifier = Modifier
                                .background(MD.Lime)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                    }
                    if (totalExercises > 0 && exerciseIndex > 0) {
                        Text(
                            text = "EGZERSİZ ${exerciseIndex.toString().padStart(2, '0')} · ${totalExercises.toString().padStart(2, '0')}",
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextMuted
                        )
                    }
                }
                Text(
                    text = workout.name,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = MD.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 9.dp)
                )
            }
        }

        // Timer — the one dominant number on screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            val stateLabel = when {
                workout.isCompleted -> "TAMAMLANDI"
                workout.isRunning -> "ÇALIŞIYOR"
                else -> "SÜRE"
            }
            Text(
                text = stateLabel,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (workout.isRunning || workout.isCompleted) MD.Lime else MD.TextMuted
            )
            Text(
                text = formatElapsedTime(workout.elapsedTimeMs),
                fontSize = 84.sp,
                lineHeight = 78.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-3).sp,
                style = TabularNumsStyle,
                color = rememberElapsedTimeColor(workout.isRunning, MD.Lime, MD.TextPrimary),
                modifier = Modifier.padding(top = 14.dp)
            )

            val totalSets = workout.sets.toInt().coerceIn(1, 8)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(totalSets) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .background(if (workout.isCompleted) MD.Lime else MD.BorderStrong)
                    )
                }
            }
        }

        // Action bar — sıfırla · başlat/duraklat (primary) · tamamla
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
        ) {
            Box(
                modifier = Modifier
                    .width(78.dp)
                    .clickable(onClick = onResetClick)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sıfırla",
                    tint = MD.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(modifier = Modifier.width(1.dp).height(52.dp).background(MD.BorderWeak))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(MD.Lime)
                    .clickable(enabled = !workout.isCompleted) { onStartPauseClick() }
                    .padding(vertical = 20.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (workout.isRunning) "DURAKLAT" else "BAŞLAT",
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.OnLime
                )
                PlayPauseIcon(
                    isRunning = workout.isRunning,
                    tint = MD.OnLime,
                    iconSize = 20.dp
                )
            }

            Box(modifier = Modifier.width(1.dp).height(52.dp).background(MD.BorderWeak))

            Box(
                modifier = Modifier
                    .width(78.dp)
                    .clickable { onCompletedToggle(!workout.isCompleted) }
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Tamamla",
                    tint = if (workout.isCompleted) MD.Lime else MD.TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Set / Tekrar / Kg / Dinlenme — 4 flush cells
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
                .padding(vertical = 16.dp)
        ) {
            DetailStatCell(label = "SET", value = workout.sets.toString(), modifier = Modifier.weight(1f).padding(start = 22.dp))
            DetailStatCell(label = "TEKRAR", value = workout.reps.toString(), modifier = Modifier.weight(1f).padding(start = 18.dp), hasDivider = true)
            DetailStatCell(label = "KG", value = workout.weight.toInt().toString(), modifier = Modifier.weight(1f).padding(start = 18.dp), hasDivider = true)
            DetailStatCell(
                label = "DİNLENME",
                value = workout.rest?.toString() ?: "-",
                modifier = Modifier.weight(1f).padding(start = 18.dp),
                hasDivider = true,
                dim = true
            )
        }

        // Notes — kept from the previous version, real data (workout.notes)
        workout.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "NOTLAR",
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextMuted
                )
                Text(
                    text = notes,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MD.TextPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Form videosu hint row — swipe to the Video page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
                .padding(horizontal = 22.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FORM VİDEOSU",
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SAĞA KAYDIR",
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = MD.TextDim
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MD.TextDim,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = false,
    dim: Boolean = false
) {
    Row(modifier = modifier) {
        if (hasDivider) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(MD.BorderWeak)
            )
            Spacer(modifier = Modifier.width(18.dp))
        }
        Column {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (dim) MD.TextDim else MD.TextPrimary
            )
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Second page - Video placeholder
 */
@Composable
private fun VideoPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
            .topRule(2.dp, MD.BorderStrong),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp).border(2.dp, MD.BorderStrong),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MD.TextFaint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Video yakında eklenecek",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MD.TextMuted
            )
        }
    }
}

/**
 * Compact stat item for inline display
 */
@Composable
private fun CompactStatItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

/**
 * Workout Stat Item - Individual stat display in the details card
 */
@Composable
private fun WorkoutStatItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Format elapsed time from milliseconds to MM:SS format
 */
private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    // Manual formatting for KMP compatibility (no String.format in common)
    val minutesStr = if (minutes < 10) "0$minutes" else minutes.toString()
    val secondsStr = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$minutesStr:$secondsStr"
}

/**
 * Helper Functions
 */
private fun getDayNameTurkish(date: LocalDate): String {
    return when (date.dayOfWeek.ordinal + 1) {
        1 -> "Pzt"
        2 -> "Sal"
        3 -> "Çar"
        4 -> "Per"
        5 -> "Cum"
        6 -> "Cmt"
        7 -> "Paz"
        else -> ""
    }
}

private fun getMonthYearTurkish(date: LocalDate): String {
    val monthName = when (date.monthNumber) {
        1 -> "Ocak"
        2 -> "Şubat"
        3 -> "Mart"
        4 -> "Nisan"
        5 -> "Mayıs"
        6 -> "Haziran"
        7 -> "Temmuz"
        8 -> "Ağustos"
        9 -> "Eylül"
        10 -> "Ekim"
        11 -> "Kasım"
        12 -> "Aralık"
        else -> ""
    }
    return "$monthName ${date.year}"
}

/**
 * Data class for date items
 */
private data class DateItem(
    val date: LocalDate,
    val dayNumber: String,
    val dayName: String
)

// ==================== DIALOGS ====================

/**
 * Kas Grubu Seç — Adım 1 · 3
 * Flush rule-divided list (index · name · exercise count · checkbox), a
 * 3-segment progress indicator in the header, confirmed via a bottom bar
 * that names the current selection.
 */
@Composable
private fun CategorySelectionDialog(
    categories: List<String>,
    availableExercises: List<com.example.aigymapp.database.Exercises>,
    initialSelectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    // Seeded from the wizard's already-confirmed pick so returning here via
    // "back" shows the prior selection instead of rebuilding blank.
    var selectedCategory by remember { mutableStateOf(initialSelectedCategory) }
    val exerciseCounts = remember(availableExercises) {
        availableExercises.groupingBy { it.muscle_group }.eachCount()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        StaggeredBand(index = 0, screenKey = Unit) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 18.dp)
            ) {
                Text(
                    text = "Kas grubu seç",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = MD.TextPrimary
                )
            }
        }

        StaggeredBand(
            index = 1,
            screenKey = Unit,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .topRule(2.dp, MD.BorderStrong)
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = selectedCategory == category
                    WizardListRow(
                        index = (index + 1).toString().padStart(2, '0'),
                        title = category,
                        trailingLabel = "${exerciseCounts[category] ?: 0} EGZERSİZ",
                        isSelected = isSelected,
                        showTopDivider = index > 0,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }

        val hasSelection = selectedCategory != null
        val barColor by animateColorAsState(if (hasSelection) MD.Lime else MD.BorderStrong, tween(300), label = "continueBarColor")
        val barLabelColor by animateColorAsState(if (hasSelection) MD.OnLime else MD.TextDim, tween(300), label = "continueBarLabelColor")
        val barInteractionSource = remember { MutableInteractionSource() }
        val barPressed by barInteractionSource.collectIsPressedAsState()
        val barScale by animateFloatAsState(if (barPressed) 0.985f else 1f, spring(dampingRatio = 0.75f), label = "continueBarScale")
        val barLabel = "DEVAM ET" + (selectedCategory?.let { " · ${it.uppercase()}" } ?: "")
        StaggeredBand(index = 2, screenKey = Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .graphicsLayer { scaleX = barScale; scaleY = barScale }
                    .background(barColor)
                    .clickable(
                        enabled = hasSelection,
                        indication = null,
                        interactionSource = barInteractionSource
                    ) {
                        selectedCategory?.let { onCategorySelected(it) }
                    }
                    .padding(vertical = 20.dp, horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = barLabel,
                    transitionSpec = {
                        (slideInVertically(tween(160, easing = FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(160))) togetherWith
                            fadeOut(tween(120))
                    },
                    label = "continueBarLabel"
                ) { animatedLabel ->
                    Text(
                        text = animatedLabel,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = barLabelColor
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = barLabelColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Shared wizard header: back box · "ADIM n · 3" (throws the old label up,
 * brings the new one in from below) · 3-segment progress bar whose active
 * segment widens to fill instead of snapping to color.
 */
@Composable
private fun WizardStepHeader(
    stepLabel: String,
    filledSegments: Int,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(2.dp, MD.BorderStrong)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Geri",
                tint = MD.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedContent(
            targetState = filledSegments to stepLabel,
            transitionSpec = {
                if (targetState.first > initialState.first) {
                    (slideInVertically(tween(220, easing = FastOutSlowInEasing)) { h -> h } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { h -> -h } + fadeOut(tween(160)))
                } else {
                    (slideInVertically(tween(220, easing = FastOutSlowInEasing)) { h -> -h } + fadeIn(tween(200))) togetherWith
                        (slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { h -> h } + fadeOut(tween(160)))
                }
            },
            modifier = Modifier.weight(1f),
            label = "wizardStepLabel"
        ) { (_, animatedLabel) ->
            Text(
                text = animatedLabel,
                fontSize = 9.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            val animatedProgress by animateFloatAsState(
                targetValue = filledSegments.toFloat(),
                animationSpec = tween(340, easing = FastOutSlowInEasing),
                label = "wizardStepProgress"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .background(MD.TextFaint)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((animatedProgress - index).coerceIn(0f, 1f))
                                .background(MD.Lime)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shared flush wizard list row: index · title (+ optional subtitle) ·
 * trailing meta label · checkbox square.
 */
@Composable
private fun WizardListRow(
    index: String,
    title: String,
    trailingLabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
    showTopDivider: Boolean = false
) {
    val lineWidth by animateDpAsState(if (isSelected) 4.dp else 0.dp, tween(240, easing = FastOutSlowInEasing), label = "wizardRowLine")
    val startPadding by animateDpAsState(if (isSelected) 18.dp else 22.dp, tween(240, easing = FastOutSlowInEasing), label = "wizardRowPadding")
    val indexColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextDim, tween(240), label = "wizardRowIndexColor")
    val titleColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextPrimary, tween(240), label = "wizardRowTitleColor")
    val trailingColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextDim, tween(240), label = "wizardRowTrailingColor")
    val boxBg by animateColorAsState(if (isSelected) MD.Lime else Color.Transparent, tween(240), label = "wizardRowBoxBg")
    val boxBorder by animateColorAsState(if (isSelected) MD.Lime else MD.BorderStrong, tween(240), label = "wizardRowBoxBorder")
    val checkAlpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(240), label = "wizardRowCheckAlpha")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .leftRule(lineWidth, MD.Lime)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(
                start = startPadding,
                end = 22.dp,
                top = 14.dp,
                bottom = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = index,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = indexColor,
            modifier = Modifier.width(26.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MD.TextMuted,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
        }
        trailingLabel?.let {
            Text(
                text = it,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.ExtraBold,
                color = trailingColor
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(boxBg)
                .border(2.dp, boxBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MD.OnLime,
                modifier = Modifier.size(14.dp).alpha(checkAlpha)
            )
        }
    }
}

/**
 * Muscle Group Card with SVG-drawn line icon
 * Fixed height ~110dp, #1A1A1A bg, 14dp corners, 1px #2A2A2A border.
 * Selected: 2px neon-green border, subtle tint background, neon icon.
 */
@Composable
private fun MuscleGroupCard(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val neonGreen = NeonLime
    val iconColor = if (isSelected) neonGreen else Color.White
    val bgColor = if (isSelected)
        Color(0xFF1A1A1A).copy(alpha = 1f).let { Color(red = it.red + 0.04f, green = it.green + 0.06f, blue = it.blue, alpha = 1f) }
    else
        Color(0xFF1A1A1A)
    val borderColor = if (isSelected) neonGreen else Color(0xFF2A2A2A)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected)
                    Color(0xFF1A1A1A).copy(alpha = 1f)
                        .let { androidx.compose.ui.graphics.lerp(it, neonGreen, 0.07f) }
                else
                    Color(0xFF1A1A1A)
            )
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            // SVG-drawn custom icon
            MuscleGroupIcon(
                category = category,
                tint = iconColor,
                size = 36.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = category,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) neonGreen else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Custom SVG-style line icon for each muscle group.
 * All icons: white stroke, 2dp stroke width, no fill, ~40dp size.
 */
@Composable
private fun MuscleGroupIcon(
    category: String,
    tint: Color,
    size: androidx.compose.ui.unit.Dp
) {
    val strokeWidth = with(androidx.compose.ui.platform.LocalDensity.current) { 2.dp.toPx() }
    val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { size.toPx() }

    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(size)
    ) {
        val w = sizePx
        val h = sizePx
        val paint = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
        when (category) {
            "Göğüs" -> {
                // Chest press icon: barbell bar + two weight plates, symmetrical
                // bar (horizontal line across centre)
                drawLine(
                    tint,
                    androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.50f),
                    androidx.compose.ui.geometry.Offset(w * 0.92f, h * 0.50f),
                    strokeWidth
                )
                // left weight plate
                val lp = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(androidx.compose.ui.geometry.RoundRect(
                        left = w * 0.08f, top = h * 0.28f, right = w * 0.20f, bottom = h * 0.72f,
                        radiusX = w * 0.04f, radiusY = w * 0.04f
                    ))
                }
                drawPath(lp, color = tint, style = paint)
                // right weight plate
                val rp = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(androidx.compose.ui.geometry.RoundRect(
                        left = w * 0.80f, top = h * 0.28f, right = w * 0.92f, bottom = h * 0.72f,
                        radiusX = w * 0.04f, radiusY = w * 0.04f
                    ))
                }
                drawPath(rp, color = tint, style = paint)
                // left collar (short vertical tick inside the bar end)
                drawLine(tint, androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.40f), androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.60f), strokeWidth)
                // right collar
                drawLine(tint, androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.40f), androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.60f), strokeWidth)
            }
            "Sırt" -> {
                // V-taper back: trapezoid with lat spread lines
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.15f, h * 0.20f)
                    lineTo(w * 0.85f, h * 0.20f)
                    lineTo(w * 0.65f, h * 0.80f)
                    lineTo(w * 0.35f, h * 0.80f)
                    close()
                }
                drawPath(path, color = tint, style = paint)
                // shoulder width line
                drawLine(tint, androidx.compose.ui.geometry.Offset(w * 0.05f, h * 0.25f), androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.25f), strokeWidth)
            }
            "Omuz" -> {
                // Shoulder shrug: rounded trapezoid (wide top, narrow bottom) = shoulder/neck profile
                val path = androidx.compose.ui.graphics.Path().apply {
                    // top-left, arc to top-right (wide shoulder line)
                    moveTo(w * 0.05f, h * 0.55f)
                    cubicTo(w * 0.05f, h * 0.25f, w * 0.30f, h * 0.18f, w * 0.50f, h * 0.18f)
                    cubicTo(w * 0.70f, h * 0.18f, w * 0.95f, h * 0.25f, w * 0.95f, h * 0.55f)
                    // bottom: narrow neck column
                    lineTo(w * 0.65f, h * 0.82f)
                    lineTo(w * 0.35f, h * 0.82f)
                    close()
                }
                drawPath(path, color = tint, style = paint)
            }
            "Kol" -> {
                // Arm curl: forearm angled up, bicep peak
                val path = androidx.compose.ui.graphics.Path().apply {
                    // forearm
                    moveTo(w * 0.25f, h * 0.80f)
                    lineTo(w * 0.45f, h * 0.55f)
                    // bicep peak curve
                    cubicTo(w * 0.45f, h * 0.30f, w * 0.60f, h * 0.18f, w * 0.72f, h * 0.28f)
                    cubicTo(w * 0.82f, h * 0.36f, w * 0.75f, h * 0.55f, w * 0.60f, h * 0.58f)
                }
                drawPath(path, color = tint, style = paint)
            }
            "Bacak" -> {
                // Side leg profile: thigh down, knee bend, calf down
                val left = androidx.compose.ui.graphics.Path().apply {
                    // left leg side-profile
                    moveTo(w * 0.28f, h * 0.10f)
                    lineTo(w * 0.28f, h * 0.56f)          // thigh straight down
                    cubicTo(                               // knee bend curve
                        w * 0.28f, h * 0.68f,
                        w * 0.38f, h * 0.70f,
                        w * 0.38f, h * 0.90f
                    )
                }
                val right = androidx.compose.ui.graphics.Path().apply {
                    // right leg side-profile (mirrored)
                    moveTo(w * 0.62f, h * 0.10f)
                    lineTo(w * 0.62f, h * 0.56f)
                    cubicTo(
                        w * 0.62f, h * 0.68f,
                        w * 0.72f, h * 0.70f,
                        w * 0.72f, h * 0.90f
                    )
                }
                drawPath(left, color = tint, style = paint)
                drawPath(right, color = tint, style = paint)
            }
            "Karın" -> {
                // 3×2 grid of small rounded rects (abs)
                val cols = 2
                val rows = 3
                val cellW = w * 0.28f
                val cellH = h * 0.18f
                val gapX = w * 0.08f
                val gapY = h * 0.06f
                val startX = (w - cols * cellW - (cols - 1) * gapX) / 2f
                val startY = (h - rows * cellH - (rows - 1) * gapY) / 2f
                val cr = cellW * 0.20f
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val x = startX + col * (cellW + gapX)
                        val y = startY + row * (cellH + gapY)
                        val path = androidx.compose.ui.graphics.Path().apply {
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = x, top = y, right = x + cellW, bottom = y + cellH,
                                    radiusX = cr, radiusY = cr
                                )
                            )
                        }
                        drawPath(path, color = tint, style = paint)
                    }
                }
            }
            "Kardiyo" -> {
                // Heart with EKG/pulse line through the middle
                // Heart shape
                val heart = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.50f, h * 0.78f)
                    cubicTo(w * 0.15f, h * 0.55f, w * 0.05f, h * 0.30f, w * 0.25f, h * 0.18f)
                    cubicTo(w * 0.38f, h * 0.10f, w * 0.50f, h * 0.22f, w * 0.50f, h * 0.30f)
                    cubicTo(w * 0.50f, h * 0.22f, w * 0.62f, h * 0.10f, w * 0.75f, h * 0.18f)
                    cubicTo(w * 0.95f, h * 0.30f, w * 0.85f, h * 0.55f, w * 0.50f, h * 0.78f)
                    close()
                }
                drawPath(heart, color = tint, style = paint)
                // EKG pulse line across the heart
                val ekg = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.15f, h * 0.50f)
                    lineTo(w * 0.32f, h * 0.50f)
                    lineTo(w * 0.40f, h * 0.32f)
                    lineTo(w * 0.48f, h * 0.65f)
                    lineTo(w * 0.56f, h * 0.38f)
                    lineTo(w * 0.62f, h * 0.50f)
                    lineTo(w * 0.85f, h * 0.50f)
                }
                drawPath(ekg, color = tint, style = paint)
            }
            else -> {
                // Tüm Vücut: stick figure enclosed in a circle
                // outer circle
                drawCircle(tint, radius = w * 0.44f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.50f), style = paint)
                // head (small inner circle)
                drawCircle(tint, radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.22f), style = paint)
                // body
                val figure = androidx.compose.ui.graphics.Path().apply {
                    // torso
                    moveTo(w * 0.50f, h * 0.30f)
                    lineTo(w * 0.50f, h * 0.60f)
                    // arms
                    moveTo(w * 0.50f, h * 0.38f)
                    lineTo(w * 0.30f, h * 0.50f)
                    moveTo(w * 0.50f, h * 0.38f)
                    lineTo(w * 0.70f, h * 0.50f)
                    // legs
                    moveTo(w * 0.50f, h * 0.60f)
                    lineTo(w * 0.36f, h * 0.78f)
                    moveTo(w * 0.50f, h * 0.60f)
                    lineTo(w * 0.64f, h * 0.78f)
                }
                drawPath(figure, color = tint, style = paint)
            }
        }
    }
}

/**
 * Egzersiz Seç — Adım 2 · 3
 * Title + selected-category badge, a real search row, then a flush
 * numbered list (no per-row checkbox — tapping a row advances immediately).
 */
@Composable
private fun ExerciseSelectionDialog(
    exercises: List<com.example.aigymapp.database.Exercises>,
    selectedCategory: String,
    onExerciseSelected: (com.example.aigymapp.database.Exercises) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        StaggeredBand(index = 0, screenKey = Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Egzersiz seç",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = MD.TextPrimary
                )
                Text(
                    text = "${selectedCategory.uppercase()} · ${exercises.size}",
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.OnLime,
                    modifier = Modifier
                        .background(MD.Lime)
                        .padding(start = 9.dp, end = 9.dp, top = 4.dp, bottom = 8.dp)
                )
            }
        }

        StaggeredBand(index = 1, screenKey = Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MD.TextDim,
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(text = "Egzersiz ara", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MD.TextDim)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = MD.TextPrimary, fontSize = 15.sp),
                        cursorBrush = SolidColor(MD.Lime),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        StaggeredBand(
            index = 2,
            screenKey = Unit,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .topRule(2.dp, MD.BorderStrong),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Egzersiz bulunamadı", fontSize = 14.sp, color = MD.TextDim)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .topRule(2.dp, MD.BorderStrong)
                ) {
                    itemsIndexed(
                        items = filteredExercises,
                        key = { _, ex -> ex.id }
                    ) { index, exercise ->
                        ExerciseItem(
                            index = index + 1,
                            exercise = exercise,
                            showTopDivider = index > 0,
                            onClick = { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exercise row — index (dim), name + equipment meta, chevron. Tapping
 * advances straight to the config step, so there's no checkbox here.
 */
@Composable
private fun ExerciseItem(
    index: Int,
    exercise: com.example.aigymapp.database.Exercises,
    showTopDivider: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextFaint,
            modifier = Modifier.width(26.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                fontSize = 15.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MD.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val meta = exercise.equipment_needed?.takeIf { it.isNotBlank() } ?: exercise.muscle_group
            meta?.let {
                Text(
                    text = it.uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextDim,
                    modifier = Modifier.padding(top = 7.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MD.TextDim,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Formats an epoch day (days since 1970-01-01) to a short Turkish date string, e.g. "26 Ağu"
 */
private fun epochDayToTurkishShortDate(epochDay: Long): String {
    val date = LocalDate.fromEpochDays(epochDay.toInt())
    val turkishMonths = listOf(
        "Oca", "Şub", "Mar", "Nis", "May", "Haz",
        "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"
    )
    val month = turkishMonths.getOrElse(date.monthNumber - 1) { "" }
    return "${date.dayOfMonth} $month"
}

/**
 * Son Performans card — shows last recorded sets/reps/weight for the exercise.
 * #1A1A1A background, 12dp radius, 1px #2A2A2A border.
 */
@Composable
private fun LastPerformanceCard(
    lastPerformance: com.example.aigymapp.ui.viewmodels.LastPerformance?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp))
    ) {
        if (lastPerformance == null) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SON PERFORMANS",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Henüz kayıt yok",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Header row ────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SON PERFORMANS",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = epochDayToTurkishShortDate(lastPerformance.dateEpochDay),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // ── Thin separator ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF2A2A2A))
                )

                // ── 3 stats columns ───────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Set
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = lastPerformance.sets.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "set",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    // Vertical divider
                    Box(modifier = Modifier.width(1.dp).height(44.dp).background(Color(0xFF2A2A2A)))
                    // Tekrar
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = lastPerformance.reps.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "tekrar",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    // Vertical divider
                    Box(modifier = Modifier.width(1.dp).height(44.dp).background(Color(0xFF2A2A2A)))
                    // Ağırlık
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val weightDisplay = if (lastPerformance.weightKg % 1.0 == 0.0)
                            lastPerformance.weightKg.toLong().toString()
                        else
                            lastPerformance.weightKg.toString()
                        Text(
                            text = weightDisplay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                        Text(
                            text = "kg",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exercise Configuration Dialog WITH LOCAL STATE - Performance Optimized
 * Uses local state to prevent ViewModel updates on each keystroke
 * Only syncs to ViewModel on save button press
 */
@Composable
private fun ExerciseConfigDialogWithLocalState(
    exercise: com.example.aigymapp.database.Exercises,
    configDate: LocalDate,
    selectedDatesCount: Int = 0,
    initialSets: String,
    initialReps: String,
    initialWeight: String,
    initialRest: String,
    initialNotes: String,
    lastPerformance: com.example.aigymapp.ui.viewmodels.LastPerformance? = null,
    isEditMode: Boolean = false,
    showHeader: Boolean = true,
    onDateClick: () -> Unit,
    onSave: (sets: String, reps: String, weight: String, rest: String, notes: String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    // Local state - prevents ViewModel recomposition on each keystroke
    var localSets by remember { mutableStateOf(initialSets) }
    var localReps by remember { mutableStateOf(initialReps) }
    var localWeight by remember { mutableStateOf(initialWeight) }
    var localRest by remember { mutableStateOf(initialRest) }
    var localNotes by remember { mutableStateOf(initialNotes) }

    ExerciseConfigDialog(
        exercise = exercise,
        configDate = configDate,
        selectedDatesCount = selectedDatesCount,
        sets = localSets,
        reps = localReps,
        weight = localWeight,
        rest = localRest,
        notes = localNotes,
        lastPerformance = lastPerformance,
        isEditMode = isEditMode,
        showHeader = showHeader,
        onSetsChanged = { localSets = it },
        onRepsChanged = { localReps = it },
        onWeightChanged = { localWeight = it },
        onRestChanged = { localRest = it },
        onNotesChanged = { localNotes = it },
        onDateClick = onDateClick,
        onSave = { onSave(localSets, localReps, localWeight, localRest, localNotes) },
        onBack = onBack,
        onDismiss = onDismiss
    )
}

/**
 * Egzersiz Kaydı — Adım 3 · 3
 * Flush "kural bölmeli" fields instead of rounded input boxes: date row,
 * a 2×2 set/tekrar/ağırlık/dinlenme grid, notes, last-performance recap.
 */
@Composable
private fun ExerciseConfigDialog(
    exercise: com.example.aigymapp.database.Exercises,
    configDate: LocalDate,
    selectedDatesCount: Int = 0,
    sets: String,
    reps: String,
    weight: String,
    rest: String,
    notes: String,
    lastPerformance: com.example.aigymapp.ui.viewmodels.LastPerformance? = null,
    isEditMode: Boolean = false,
    showHeader: Boolean = true,
    onSetsChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRestChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onDateClick: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        if (showHeader) {
            WizardStepHeader(
                stepLabel = if (isEditMode) "ANTRENMANI DÜZENLE" else "ADIM 3 · 3",
                filledSegments = 3,
                onBack = onBack,
                trailing = {
                    Text(
                        text = "İPTAL",
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextDim,
                        modifier = Modifier.clickable(onClick = onDismiss)
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Identity
            StaggeredBand(index = 0, screenKey = Unit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .topRule(2.dp, MD.BorderStrong)
                        .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 18.dp)
                ) {
                    exercise.muscle_group?.let { muscleGroup ->
                        Text(
                            text = muscleGroup.uppercase(),
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.OnLime,
                            modifier = Modifier
                                .background(MD.Lime)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Text(
                        text = exercise.name,
                        fontSize = 24.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp,
                        color = MD.TextPrimary
                    )
                }
            }

            // Date row
            StaggeredBand(index = 1, screenKey = Unit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .topRule(2.dp, MD.BorderStrong)
                        .clickable(onClick = onDateClick)
                        .padding(horizontal = 22.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TARİH",
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextMuted
                        )
                        Text(
                            text = if (selectedDatesCount > 1) {
                                "${formatDateTurkish(configDate)} (+${selectedDatesCount - 1} gün)"
                            } else {
                                formatDateTurkish(configDate)
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextPrimary,
                            modifier = Modifier.padding(top = 9.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Tarih Seç",
                        tint = MD.Lime,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Set / Tekrar / Ağırlık / Dinlenme grid
            StaggeredBand(index = 2, screenKey = Unit) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().topRule(2.dp, MD.BorderStrong)) {
                        WheelValueCell(
                            label = "SET",
                            value = sets,
                            values = SetCountWheelValues,
                            onValueChange = onSetsChanged,
                            modifier = Modifier.weight(1f).padding(start = 22.dp),
                            default = 3
                        )
                        WheelValueCell(
                            label = "TEKRAR",
                            value = reps,
                            values = RepsWheelValues,
                            onValueChange = onRepsChanged,
                            modifier = Modifier.weight(1f).padding(start = 18.dp),
                            showLeftRule = true,
                            default = 10
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().topRule(1.dp, MD.BorderWeak)) {
                        WheelValueCell(
                            label = "AĞIRLIK · KG",
                            value = weight,
                            values = WeightWheelValues,
                            onValueChange = onWeightChanged,
                            modifier = Modifier.weight(1f).padding(start = 22.dp),
                            default = 0
                        )
                        WheelValueCell(
                            label = "DİNLENME · SN",
                            value = rest,
                            values = RestWheelValues,
                            onValueChange = onRestChanged,
                            modifier = Modifier.weight(1f).padding(start = 18.dp),
                            showLeftRule = true,
                            default = 0
                        )
                    }
                }
            }

            // Notlar
            StaggeredBand(index = 3, screenKey = Unit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .topRule(2.dp, MD.BorderStrong)
                        .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 18.dp)
                ) {
                    Text(
                        text = "NOTLAR",
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextMuted
                    )
                    Box(modifier = Modifier.padding(top = 11.dp)) {
                        if (notes.isEmpty()) {
                            Text(text = "Not ekle", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MD.TextFaint)
                        }
                        BasicTextField(
                            value = notes,
                            onValueChange = onNotesChanged,
                            textStyle = TextStyle(color = MD.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                            cursorBrush = SolidColor(MD.Lime),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            StaggeredBand(index = 4, screenKey = Unit) {
                LastPerformanceSection(lastPerformance = lastPerformance)
            }
        }

        // Kaydet — pinned bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.BorderStrong)
                .background(MD.Lime)
                .clickable(onClick = onSave)
                .padding(vertical = 20.dp, horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditMode) "GÜNCELLE" else "KAYDET",
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.OnLime
            )
            Icon(
                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MD.OnLime,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private val SetCountWheelValues = (1..12).toList()
private val RepsWheelValues = (1..50).toList()
private val WeightWheelValues = (0..300).toList()
private val RestWheelValues = (0..300 step 5).toList()

/**
 * A single flush number cell backed by an iOS-style spinning wheel picker
 * instead of a keyboard-entry text field — label above, wheel below.
 */
@Composable
private fun WheelValueCell(
    label: String,
    value: String,
    values: List<Int>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLeftRule: Boolean = false,
    default: Int = values.first()
) {
    Row(modifier = modifier.padding(vertical = 10.dp)) {
        if (showLeftRule) {
            Box(modifier = Modifier.width(1.dp).height(102.dp).background(MD.BorderWeak))
            Spacer(modifier = Modifier.width(18.dp))
        }
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            WheelNumberPicker(
                value = value.toIntOrNull() ?: default,
                values = values,
                onValueChange = { onValueChange(it.toString()) },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * A spinning wheel picker matching the iPhone Clock app's timer wheels:
 * a vertically snapping reel of numbers, the centered value shown large
 * and bright, its neighbors dimmed — scroll/fling to select instead of
 * typing on a keyboard.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun WheelNumberPicker(
    value: Int,
    values: List<Int>,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 3,
    itemHeight: Dp = 34.dp
) {
    val initialIndex = remember(values) { values.indexOf(value).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialIndex)
    val flingBehavior = remember(listState) {
        snapFlingBehavior(
            snapLayoutInfoProvider = SnapLayoutInfoProvider(listState, SnapPosition.Center),
            decayAnimationSpec = exponentialDecay(frictionMultiplier = 3f),
            snapAnimationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val centeredIndex by remember {
        derivedStateOf {
            val offsetItems = (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()
            (listState.firstVisibleItemIndex + offsetItems).coerceIn(0, values.lastIndex)
        }
    }

    // Follow external resets (e.g. dialog reopened with a different value)
    // but never fight the user mid-scroll.
    LaunchedEffect(value, values) {
        val idx = values.indexOf(value)
        if (idx >= 0 && !listState.isScrollInProgress && idx != centeredIndex) {
            listState.scrollToItem(idx)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val settled = values.getOrNull(centeredIndex)
            if (settled != null && settled != value) onValueChange(settled)
        }
    }

    Box(
        modifier = modifier
            .width(64.dp)
            .height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleCount / 2)),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(values) { index, item ->
                val selected = index == centeredIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        fontSize = if (selected) 24.sp else 16.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        letterSpacing = (-0.3).sp,
                        color = if (selected) MD.TextPrimary else MD.TextFaint
                    )
                }
            }
        }
    }
}

/**
 * Son performans — flush recap of the last time this exercise was logged.
 */
@Composable
private fun LastPerformanceSection(
    lastPerformance: com.example.aigymapp.ui.viewmodels.LastPerformance?
) {
    Column(modifier = Modifier.fillMaxWidth().topRule(2.dp, MD.BorderStrong)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SON PERFORMANS",
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            if (lastPerformance != null) {
                Text(
                    text = epochDayToTurkishShortDate(lastPerformance.dateEpochDay).uppercase(),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = MD.TextDim
                )
            }
        }

        if (lastPerformance == null) {
            Column(modifier = Modifier.fillMaxWidth().topRule(1.dp, MD.BorderWeak)) {
                Text(
                    text = "Henüz kayıt yok",
                    fontSize = 13.sp,
                    color = MD.TextDim,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(1.dp, MD.BorderWeak)
                    .padding(vertical = 15.dp)
            ) {
                val weightDisplay = if (lastPerformance.weightKg % 1.0 == 0.0)
                    lastPerformance.weightKg.toLong().toString()
                else
                    lastPerformance.weightKg.toString()
                LastPerformanceCell(value = lastPerformance.sets.toString(), label = "SET", modifier = Modifier.weight(1f).padding(start = 22.dp))
                LastPerformanceCell(value = lastPerformance.reps.toString(), label = "TEKRAR", modifier = Modifier.weight(1f).padding(start = 20.dp), showLeftRule = true)
                LastPerformanceCell(value = weightDisplay, label = "KG", modifier = Modifier.weight(1f).padding(start = 20.dp), showLeftRule = true)
            }
        }
    }
}

@Composable
private fun LastPerformanceCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    showLeftRule: Boolean = false
) {
    Row(modifier = modifier) {
        if (showLeftRule) {
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(MD.BorderWeak))
            Spacer(modifier = Modifier.width(20.dp))
        }
        Column {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MD.TextPrimary)
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

@Composable
private fun WorkoutDatePickerDialog(
    initialDate: LocalDate,
    onDatesSelected: (List<LocalDate>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentYear by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialDate.year) }
    var currentMonth by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialDate.monthNumber) }
    var selectedDates by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(setOf<LocalDate>()) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111),
        tonalElevation = 0.dp,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tarihleri Planla",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Shortcut Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bugün
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            val today = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            if (selectedDates.contains(today)) {
                                selectedDates = selectedDates - today
                            } else {
                                selectedDates = selectedDates + today
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Bugün",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Bu Hafta
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            val today = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            
                            // Get day of week (1=Monday, 7=Sunday)
                            val dayOfWeek = when (today.dayOfWeek) {
                                kotlinx.datetime.DayOfWeek.MONDAY -> 1
                                kotlinx.datetime.DayOfWeek.TUESDAY -> 2
                                kotlinx.datetime.DayOfWeek.WEDNESDAY -> 3
                                kotlinx.datetime.DayOfWeek.THURSDAY -> 4
                                kotlinx.datetime.DayOfWeek.FRIDAY -> 5
                                kotlinx.datetime.DayOfWeek.SATURDAY -> 6
                                kotlinx.datetime.DayOfWeek.SUNDAY -> 7
                                else -> 1
                            }
                            
                            // Get Monday of this week
                            val monday = today.minus(dayOfWeek - 1, DateTimeUnit.DAY)
                            
                            // Add all 7 days of the week
                            val newDates = mutableSetOf<LocalDate>()
                            for (i in 0..6) {
                                val date = monday.plus(i, DateTimeUnit.DAY)
                                newDates.add(date)
                            }
                            selectedDates = selectedDates + newDates
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Bu Hafta",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Seçimi Temizle
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            selectedDates = setOf()
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Temizle",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Month/Year Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = NeonLime
                        )
                    }
                    
                    Text(
                        text = getMonthNameTurkish(currentMonth) + " " + currentYear,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    IconButton(onClick = {
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = NeonLime
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Day Headers (Pzt, Sal, Çar, etc.)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Calendar Grid
                CalendarMonthGrid(
                    year = currentYear,
                    month = currentMonth,
                    selectedDates = selectedDates,
                    onDateClick = { date ->
                        if (selectedDates.contains(date)) {
                            selectedDates = selectedDates - date
                        } else {
                            selectedDates = selectedDates + date
                        }
                    }
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    if (selectedDates.isNotEmpty()) {
                        onDatesSelected(selectedDates.toList())
                    }
                    onDismiss()
                },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF2A2A2A),
                    contentColor = NeonLime
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonLime),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (selectedDates.isEmpty()) "Kapat" else "Ekle (${selectedDates.size})",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        dismissButton = {
            if (selectedDates.isNotEmpty()) {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Text("İptal")
                }
            }
        }
    )
}

/**
 * Calendar Month Grid - Shows all days in a month with multi-select support
 */
@Composable
private fun CalendarMonthGrid(
    year: Int,
    month: Int,
    selectedDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    // Get the first day of the month
    val firstDayOfMonth = LocalDate(year, month, 1)
    
    // Get day of week as ISO number (Monday=1, Sunday=7)
    val firstDayOfWeek = when (firstDayOfMonth.dayOfWeek) {
        kotlinx.datetime.DayOfWeek.MONDAY -> 1
        kotlinx.datetime.DayOfWeek.TUESDAY -> 2
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> 3
        kotlinx.datetime.DayOfWeek.THURSDAY -> 4
        kotlinx.datetime.DayOfWeek.FRIDAY -> 5
        kotlinx.datetime.DayOfWeek.SATURDAY -> 6
        kotlinx.datetime.DayOfWeek.SUNDAY -> 7
        else -> 1
    }
    
    // Get number of days in the month
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
    
    // Calculate empty cells before first day (Monday=0, Sunday=6)
    val emptyCellsBefore = (firstDayOfWeek - 1) % 7
    
    // We need days in previous month:
    val prevMonth = if (month == 1) 12 else month - 1
    val prevYear = if (month == 1) year - 1 else year
    val daysInPrevMonth = when (prevMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(prevYear)) 29 else 28
        else -> 30
    }
    
    // Create the calendar grid
    val totalCells = emptyCellsBefore + daysInMonth
    val rows = (totalCells + 6) / 7 // Round up to get number of rows needed
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - emptyCellsBefore + 1
                    
                    val isCurrentMonth = dayNumber in 1..daysInMonth
                    val displayDay = when {
                        dayNumber < 1 -> daysInPrevMonth + dayNumber
                        dayNumber > daysInMonth -> dayNumber - daysInMonth
                        else -> dayNumber
                    }
                    
                    val (actualYear, actualMonth) = when {
                        dayNumber < 1 -> Pair(prevYear, prevMonth)
                        dayNumber > daysInMonth -> Pair(if (month == 12) year + 1 else year, if (month == 12) 1 else month + 1)
                        else -> Pair(year, month)
                    }
                    
                    val currentDate = LocalDate(actualYear, actualMonth, displayDay)
                    val isSelected = selectedDates.contains(currentDate)
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    val isToday = currentDate == today
                    
                    CalendarDayCell(
                        day = displayDay,
                        isSelected = isSelected,
                        isToday = isToday,
                        isCurrentMonth = isCurrentMonth,
                        onClick = { if (isCurrentMonth) onDateClick(currentDate) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Calendar Day Cell - Individual day in the calendar
 */
@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> NeonLime
                    isToday -> DarkSurfaceVariant
                    else -> Color(0xFF1A1A1A)
                }
            )
            .clickable(enabled = isCurrentMonth, onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.Black
                isToday -> NeonLime
                !isCurrentMonth -> TextSecondary.copy(alpha = 0.3f)
                else -> TextPrimary
            },
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Check if year is a leap year
 */
private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

/**
 * Get English month name
 */
private fun getMonthNameTurkish(month: Int): String {
    return when (month) {
        1 -> "Ocak"
        2 -> "Şubat"
        3 -> "Mart"
        4 -> "Nisan"
        5 -> "Mayıs"
        6 -> "Haziran"
        7 -> "Temmuz"
        8 -> "Ağustos"
        9 -> "Eylül"
        10 -> "Ekim"
        11 -> "Kasım"
        12 -> "Aralık"
        else -> ""
    }
}

/**
 * Format date to English format (e.g., "Fri, Dec 20")
 */
private fun formatDateTurkish(date: LocalDate): String {
    val dayName = getDayNameTurkish(date)
    val monthName = when (date.monthNumber) {
        1 -> "Oca"
        2 -> "Şub"
        3 -> "Mar"
        4 -> "Nis"
        5 -> "May"
        6 -> "Haz"
        7 -> "Tem"
        8 -> "Ağu"
        9 -> "Eyl"
        10 -> "Eki"
        11 -> "Kas"
        12 -> "Ara"
        else -> ""
    }
    return "$dayName, ${date.dayOfMonth} $monthName"
}

/**
 * Delete Confirmation Dialog
 * Warns user before deleting all workouts for the selected date
 */
@Composable
private fun DeleteConfirmationDialog(
    date: LocalDate,
    workoutCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Warning",
                tint = ErrorRed,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Tüm Antrenmanları Sil?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${formatDateTurkish(date)} tarihindeki $workoutCount antrenmanı silmek istediğinizden emin misiniz?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Sil",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text("İptal")
            }
        }
    )
}

