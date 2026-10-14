package com.example.aigymapp.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.aigymapp.ui.components.*
import com.example.aigymapp.ui.theme.ModernistDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Stagger interval between each section's entrance animation. */
private const val EntranceStaggerMs = 70L

/** Duration of each section's fade-in. */
private const val EntranceAlphaDurationMs = 380

/** Vertical distance (dp) each section settles down from. */
private const val EntranceOffsetDp = 10f

/** Number of sections in the entrance sequence — greeting, week strip, stats strip, workout card, goals. */
private const val EntranceSectionCount = 5

/**
 * Home Screen — "Modernist Dark" redesign
 * Main dashboard showing user profile, weekly calendar, goals, and active workout
 *
 * Layout Structure:
 * - Profile Header (User greeting)
 * - Weekly day strip (date selection)
 * - Daily stats strip (Antrenman / Gün seri / Aktif Gün)
 * - Active Workout section (Current exercise with timer) - SYNCED with Workout screen
 * - Goals section (Monthly training progress)
 *
 * All five sections settle into place sequentially on entry (fade + 10dp
 * rise, staggered by EntranceStaggerMs). The goals section's progress bars
 * only start filling once the last section has finished settling.
 */
@Composable
fun HomeScreen(
    workoutViewModel: com.example.aigymapp.ui.viewmodels.WorkoutViewModel,
    goalViewModel: com.example.aigymapp.ui.viewmodels.GoalViewModel,
    profileViewModel: com.example.aigymapp.ui.viewmodels.ProfileViewModel,
    onNavigateToWorkout: () -> Unit = {},
    onManageGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Refresh goals whenever home screen is displayed
    // This ensures progress updates after completing workouts
    LaunchedEffect(Unit) {
        goalViewModel.refreshProgress()
    }

    var goalBarsReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val lastSectionDelay = (EntranceSectionCount - 1) * EntranceStaggerMs
        delay(lastSectionDelay + EntranceAlphaDurationMs)
        goalBarsReady = true
    }

    val workoutState by workoutViewModel.uiState.collectAsState()
    val profileState = profileViewModel.state

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ModernistDark.Bg)
                .verticalScroll(rememberScrollState())
        ) {
            EntranceAnimated(index = 0) {
                ProfileHeader(
                    userName = profileState.name,
                    avatarBytes = profileState.avatarBytes,
                    onAvatarClick = onProfileClick
                )
            }

            EntranceAnimated(index = 1) {
                WeeklyCalendarCard(
                    workoutViewModel = workoutViewModel
                )
            }

            EntranceAnimated(index = 2) {
                DailyStatsStrip(
                    workoutViewModel = workoutViewModel
                )
            }

            EntranceAnimated(index = 3) {
                ActiveWorkoutCard(
                    workoutViewModel = workoutViewModel,
                    onStartWorkoutClick = onNavigateToWorkout
                )
            }

            EntranceAnimated(index = 4) {
                GoalsCard(
                    goalViewModel = goalViewModel,
                    onAddGoalClick = onManageGoalsClick,
                    startBarAnimation = goalBarsReady
                )
            }
        }

        // Günün tüm egzersizleri tamamlandığında Antrenman ekranındakiyle aynı
        // "Bugün tamamlandı" özeti burada da 3sn gösterilir.
        WorkoutCompleteSummaryOverlay(
            visible = workoutState.showWorkoutCompleteSummary,
            summary = workoutState.workoutCompleteSummary,
            onDismiss = { workoutViewModel.dismissWorkoutCompleteSummary() }
        )
    }
}

/**
 * Wraps a home screen section with the entrance animation: fades in over
 * [EntranceAlphaDurationMs] while settling up from [EntranceOffsetDp] below,
 * starting after `index * `[EntranceStaggerMs] of delay.
 */
@Composable
private fun EntranceAnimated(
    index: Int,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(EntranceOffsetDp) }

    LaunchedEffect(Unit) {
        delay(index * EntranceStaggerMs)
        launch {
            alpha.animateTo(1f, animationSpec = tween(EntranceAlphaDurationMs, easing = FastOutSlowInEasing))
        }
        launch {
            offsetY.animateTo(
                0f,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value.dp.toPx()
        }
    ) {
        content()
    }
}
