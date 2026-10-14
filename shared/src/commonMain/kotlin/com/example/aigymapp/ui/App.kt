package com.example.aigymapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aigymapp.database.AppDatabase
import com.example.aigymapp.database.DatabaseDriverFactory
import com.example.aigymapp.repository.AppRepository
import com.example.aigymapp.repository.ProfileRepository
import com.example.aigymapp.repository.ProgressRepository
import com.example.aigymapp.repository.SchedulingRepository
import com.example.aigymapp.repository.WorkoutPlannerRepository
import com.example.aigymapp.repository.WorkoutRepository
import com.example.aigymapp.ui.components.AddGoalFlow
import com.example.aigymapp.ui.components.GoalEditMenu
import com.example.aigymapp.ui.components.GymBottomNavigation
import com.example.aigymapp.ui.screens.CoachScreen
import com.example.aigymapp.ui.screens.HomeScreen
import com.example.aigymapp.ui.screens.ProgressScreen
import com.example.aigymapp.ui.screens.ProfileScreen
import com.example.aigymapp.ui.screens.WorkoutScreen
import com.example.aigymapp.ui.theme.AiGymAppTheme
import com.example.aigymapp.ui.viewmodels.GoalViewModel
import com.example.aigymapp.ui.viewmodels.ProfileViewModel
import com.example.aigymapp.ui.viewmodels.ProgressViewModel
import com.example.aigymapp.ui.viewmodels.WorkoutViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Application Entry Point
 * Manages navigation between screens with bottom navigation bar
 * Supports Dark/Light theme switching with system preference detection
 */
@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory
) {
    // Database ve Repository'leri oluştur
    val database = remember { 
        AppDatabase(databaseDriverFactory.createDriver())
    }
    
    val appRepository = remember {
        AppRepository(database)
    }
    
    val profileRepository = remember { 
        ProfileRepository(database)
    }
    

    // İlk önce schedulingRepository'yi oluştur (progressRepository'ye referans olmadan)
    val schedulingRepositoryTemp = remember {
        SchedulingRepository(database, null)
    }
    
    // Sonra progressRepository'yi oluştur (schedulingRepository ile)
    val progressRepository = remember {
        ProgressRepository(database, schedulingRepositoryTemp)
    }
    
    // schedulingRepository'yi progressRepository ile güncelle
    val schedulingRepository = remember(progressRepository) {
        SchedulingRepository(database, progressRepository)
    }
    
    val workoutPlannerRepository = remember {
        WorkoutPlannerRepository(database)
    }
    
    val workoutRepository = remember {
        WorkoutRepository(workoutPlannerRepository, schedulingRepository)
    }
    
    // ViewModel'leri oluştur (CoroutineScope ile)
    val coroutineScope = rememberCoroutineScope()
    
    val profileViewModel = remember { 
        ProfileViewModel(profileRepository, coroutineScope)
    }
    
    val progressViewModel = remember {
        ProgressViewModel(progressRepository, workoutRepository)
    }
    
    // Goal ViewModel - Hedef takibi için (WorkoutViewModel'den önce oluşturulmalı,
    // egzersiz tamamlanınca hedef ilerlemesini yenilemek için ona enjekte ediliyor)
    val goalViewModel = remember {
        GoalViewModel(appRepository, coroutineScope)
    }

    val workoutViewModel = remember {
        WorkoutViewModel(appRepository, schedulingRepository, workoutPlannerRepository, progressRepository, coroutineScope, progressViewModel, goalViewModel)
    }
    
    AiGymAppTheme {
        var showAddGoalFlow by remember { mutableStateOf(false) }
        var showGoalEditMenu by remember { mutableStateOf(false) }
        var showProfileMenu by remember { mutableStateOf(false) }

        val pagerState = rememberPagerState(pageCount = { 5 })
        val selectedScreenIndex = pagerState.currentPage
        val coroutineScopePager = rememberCoroutineScope()
        fun navigateToScreen(index: Int) {
            coroutineScopePager.launch { pagerState.animateScrollToPage(index) }
        }

        // Note: BackHandler is Android-specific and removed for iOS compatibility
        // The workout timer protection is handled at Android app level

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    GymBottomNavigation(
                        selectedIndex = selectedScreenIndex,
                        onNavigate = { index -> navigateToScreen(index) }
                    )
                }
            ) { paddingValues ->
                Surface(
                    modifier = Modifier.padding(paddingValues),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !showAddGoalFlow && !showGoalEditMenu && !showProfileMenu,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(
                                workoutViewModel = workoutViewModel,
                                goalViewModel = goalViewModel,
                                profileViewModel = profileViewModel,
                                onNavigateToWorkout = { navigateToScreen(1) },
                                onManageGoalsClick = { showGoalEditMenu = true },
                                onProfileClick = { showProfileMenu = true }
                            )
                            1 -> WorkoutScreen(viewModel = workoutViewModel)
                            2 -> CameraPlaceholderScreen()
                            3 -> ProgressScreen(viewModel = progressViewModel)
                            4 -> CoachScreen(
                                goalViewModel = goalViewModel
                            )
                        }
                    }
                }
            }

            // Full-screen "Hedefleri Düzenle" menu — lists current goals with a
            // delete action, plus an entry point into the "Yeni Hedef" wizard below.
            if (showGoalEditMenu) {
                GoalEditMenu(
                    goalViewModel = goalViewModel,
                    onClose = { showGoalEditMenu = false },
                    onAddGoalClick = {
                        showGoalEditMenu = false
                        showAddGoalFlow = true
                    }
                )
            }

            // Full-screen "Yeni Hedef" wizard — covers the bottom nav entirely,
            // matching the design (no nav bar shown during goal creation).
            if (showAddGoalFlow) {
                AddGoalFlow(
                    goalViewModel = goalViewModel,
                    onDismiss = { showAddGoalFlow = false },
                    onGoalCreatedNavigateToWorkout = {
                        showAddGoalFlow = false
                        navigateToScreen(1)
                    }
                )
            }

            // Full-screen profile menu — opened from the avatar on the Ana Sayfa
            // header instead of a dedicated bottom nav tab.
            if (showProfileMenu) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onClose = { showProfileMenu = false }
                )
            }
        }
    }
}

/**
 * Placeholder screens for navigation items not yet implemented
 */
@Composable
private fun CameraPlaceholderScreen() {
    PlaceholderScreen(title = "📷 Camera", subtitle = "Coming soon...")
}

@Composable
private fun ProgressPlaceholderScreen() {
    PlaceholderScreen(title = "📊 Progress", subtitle = "Coming soon...")
}

@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
