package com.example.aigymapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.ui.components.leftRule
import com.example.aigymapp.ui.components.topRule
import com.example.aigymapp.ui.theme.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt
import com.example.aigymapp.ui.viewmodels.TimePeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

// Calendar day-status colors — hiç tamamlanmamış (kırmızı) / kısmen (sarı) / tamamı (ModernistDark.Lime)
private val CalendarDayRed = Color(0xFFEF5350)
private val CalendarDayYellow = Color(0xFFD1A02A)

/**
 * Progress Screen with Tabbed View
 * Dark Mode Theme: #121212 Background, #CCFF00 Accent
 * 
 * Tabs:
 * 0 - Stats (Progress Charts)
 * 1 - Calendar (Monthly View with Stats)
 * 
 * 🎬 Framer Motion tarzı animasyonlar:
 * - Kartlar slide up + fade in ile giriş yapar
 * - Circular progress 0'dan hedefe animate olur (1.5s)
 * - Büyük sayılar spring ile pop-up efekti
 * - Bar chart barları sırayla yükselir
 * - Kas grubu listesi staggered görünür
 */
@Composable
fun ProgressScreen(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Tab değiştiğinde animasyonları resetle
    var tabChangeKey by remember { mutableStateOf(0) }
    
    // ProgressScreen zaten App.kt'nin Scaffold'u içindeki Surface(.padding(paddingValues)) içinde
    // Ekstra Scaffold/padding eklememek gerekiyor — tab bar en üste yalışmalı
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Segmented Control
        TabSelector(
            selectedTab = selectedTab,
            onTabSelected = { 
                selectedTab = it
                tabChangeKey++ // Animasyonları resetle
            }
        )
        
        // Content Switching with animation key
        when (selectedTab) {
            0 -> StatsContent(viewModel = viewModel, animationKey = tabChangeKey)
            1 -> CalendarContent(viewModel = viewModel, animationKey = tabChangeKey)
        }
    }
}

/**
 * 🎬 AnimatedCard - Framer Motion tarzı giriş animasyonu
 * Y ekseninde 30dp aşağıdan yukarı kayar + fade in
 * 
 * @param delay Animasyon gecikmesi (milisaniye)
 * @param animationKey Animasyonu resetlemek için key
 */
@Composable
private fun AnimatedCard(
    delay: Int = 0,
    animationKey: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember(animationKey) { mutableStateOf(false) }
    val alpha = remember(animationKey) { Animatable(0f) }
    val translationY = remember(animationKey) { Animatable(30f) }
    
    LaunchedEffect(animationKey) {
        // Reset
        alpha.snapTo(0f)
        translationY.snapTo(30f)
        isVisible = false
        
        // Delay sonrası animasyon başlat
        delay(delay.toLong())
        isVisible = true
        
        // Paralel animasyonlar
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translationY.value * density
        }
    ) {
        content()
    }
}

/**
 * Segmented Control Tab Selector
 * Two tabs: Stats and Calendar
 */
@Composable
private fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModernistDark.Bg)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("İstatistikler" to 0, "Takvim" to 1).forEach { (label, index) ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label.uppercase(),
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) ModernistDark.TextPrimary else ModernistDark.TextDim
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (selectedTab == index) ModernistDark.Lime else ModernistDark.BorderStrong)
                )
            }
        }
    }
}

/**
 * Stats Content (İstatistikler)
 * GERÇEK VERİ ile modern tasarım - Figma pixel perfect
 * 🎬 Kartlar sıralı animasyonlu giriş yapar
 */
@Composable
private fun StatsContent(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    animationKey: Int = 0
) {
    // StateFlow'ları collect et
    val monthlyDailyWorkoutCounts by viewModel.monthlyDailyWorkoutCounts.collectAsState()
    val muscleGroupDistribution by viewModel.muscleGroupDistribution.collectAsState()
    val totalSets by viewModel.totalSets.collectAsState()
    val totalPlannedSets by viewModel.totalPlannedSets.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()
    val monthDateRangeText by viewModel.monthDateRangeText.collectAsState()
    val completedWorkouts by viewModel.completedWorkouts.collectAsState()
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModernistDark.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Date Selector Row with Dropdown (GERÇEK TARİH) - Delay: 0ms
        AnimatedCard(delay = 0, animationKey = animationKey) {
            Box(modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                DateSelectorRowWithDropdown(
                    selectedPeriod = selectedPeriod,
                    dateRangeText = monthDateRangeText,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )
            }
        }

        // 2. Antrenman / Set / Tamamlama summary row - Delay: 80ms
        AnimatedCard(delay = 80, animationKey = animationKey) {
            Box(modifier = Modifier.padding(horizontal = 22.dp)) {
                CompletedSetsProgressCard(
                    totalWorkouts = totalWorkouts,
                    completedSets = totalSets,
                    plannedSets = totalPlannedSets,
                    selectedPeriod = selectedPeriod
                )
            }
        }

        // 3. Activity Bar Chart (Haftalık/Aylık/Yıllık - GERÇEK VERİ) - Delay: 160ms
        AnimatedCard(delay = 160, animationKey = animationKey) {
            MonthlyActivityCard(
                monthlyWorkoutCounts = monthlyDailyWorkoutCounts,
                selectedPeriod = selectedPeriod,
                animationKey = animationKey
            )
        }

        // 4. Muscle Group Distribution (GERÇEK VERİ) - Delay: 240ms
        AnimatedCard(delay = 240, animationKey = animationKey) {
            MuscleGroupDistributionCard(
                muscleGroupDistribution = muscleGroupDistribution,
                animationKey = animationKey
            )
        }

        // Alt boşluk
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Calendar Content
 * 🎬 Kartlar sıralı animasyonlu giriş yapar
 */
@Composable
private fun CalendarContent(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    animationKey: Int = 0
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val monthProgress by viewModel.monthProgress.collectAsState()
    val showExerciseDialog by viewModel.showExerciseDialog.collectAsState()
    val selectedDayExerciseDetails by viewModel.selectedDayExerciseDetails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModernistDark.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Grid + inline day-detail — one continuous table, matching the redesign.
        AnimatedCard(delay = 0, animationKey = animationKey) {
            CalendarCard(
                viewModel = viewModel,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                monthProgress = monthProgress,
                onDetailClick = { viewModel.openExerciseDialog() }
            )
        }

        // Alt boşluk
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Egzersiz Listesi Pop-up (GERÇEK VERİ - NO MOCK!)
    selectedDay?.let { day ->
        if (showExerciseDialog) {
            ExerciseHistoryDialog(
                date = kotlinx.datetime.LocalDate(selectedMonth.year, selectedMonth.month, day).toString(),
                exerciseDetails = selectedDayExerciseDetails,
                onDismiss = { viewModel.closeExerciseDialog() }
            )
        }
    }
}

/**
 * Calendar Card — a real grid table (day number top-left, status bar
 * bottom-left, 1px row/column rules) with the selected day's detail
 * (kas grubu / tamamlanan set / süre / kalori) folded into the same
 * continuous card, matching the redesign exactly.
 */
@Composable
private fun CalendarCard(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    selectedMonth: kotlinx.datetime.LocalDateTime,
    selectedDay: Int?,
    monthProgress: List<com.example.aigymapp.database.Day_progress>,
    onDetailClick: () -> Unit
) {
    val monthNames = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )
    val monthName = monthNames[selectedMonth.monthNumber - 1]
    val year = selectedMonth.year

    val daysInMonth = daysInMonth(selectedMonth.year, selectedMonth.monthNumber)
    val activeDays = remember(monthProgress) {
        monthProgress.count { it.status == "FULL" || it.status == "PARTIAL" }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header: "{aktif gün} AKTİF GÜN · {gün sayısı} GÜN" eyebrow + month title + arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$activeDays AKTİF GÜN · $daysInMonth GÜN",
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernistDark.TextMuted
                )
                Text(
                    text = "$monthName $year",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = ModernistDark.TextPrimary,
                    modifier = Modifier.padding(top = 9.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(2.dp, ModernistDark.BorderStrong)
                        .clickable { viewModel.previousMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Önceki Ay",
                        tint = ModernistDark.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(2.dp, ModernistDark.BorderStrong)
                        .clickable { viewModel.nextMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Sonraki Ay",
                        tint = ModernistDark.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Weekday header row
        Row(modifier = Modifier.fillMaxWidth().topRule(2.dp, ModernistDark.BorderStrong)) {
            listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CMT", "PAZ").forEachIndexed { index, day ->
                Text(
                    text = day,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernistDark.TextDim,
                    modifier = Modifier
                        .weight(1f)
                        .then(if (index > 0) Modifier.leftRule(1.dp, ModernistDark.BorderWeak) else Modifier)
                        .padding(vertical = 9.dp, horizontal = 8.dp)
                )
            }
        }

        // Grid table
        CalendarGrid(
            viewModel = viewModel,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            monthProgress = monthProgress
        )

        // Inline day detail
        CalendarDayDetail(
            viewModel = viewModel,
            selectedMonth = selectedMonth,
            selectedDay = selectedDay,
            onDetailClick = onDetailClick
        )
    }
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}

/**
 * Calendar Grid — real table: 1px row/column rules, day number top-left,
 * a small status bar bottom-left (red = plan yapıldı ama hiç tamamlanmadı,
 * sarı = kısmen, neon yeşil = tamamı tamamlandı). Selected day fills lime;
 * today gets a 2px lime inset border.
 */
@Composable
private fun CalendarGrid(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    selectedMonth: kotlinx.datetime.LocalDateTime,
    selectedDay: Int?,
    monthProgress: List<com.example.aigymapp.database.Day_progress>
) {
    val firstDay = kotlinx.datetime.LocalDate(selectedMonth.year, selectedMonth.monthNumber, 1)
    val startOffset = firstDay.dayOfWeek.ordinal // 0 = Monday
    val daysInCurrentMonth = daysInMonth(selectedMonth.year, selectedMonth.monthNumber)

    val prevMonth = if (selectedMonth.monthNumber == 1) 12 else selectedMonth.monthNumber - 1
    val prevYear = if (selectedMonth.monthNumber == 1) selectedMonth.year - 1 else selectedMonth.year
    val daysInPrevMonth = daysInMonth(prevYear, prevMonth)

    val totalCells = startOffset + daysInCurrentMonth
    val targetCells = if (totalCells <= 35) 35 else 42

    val today = remember {
        kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    }

    Column {
        for (row in 0 until (targetCells / 7)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (row > 0) Modifier.topRule(1.dp, ModernistDark.BorderWeak) else Modifier.topRule(2.dp, ModernistDark.BorderStrong))
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOffset = cellIndex - startOffset + 1

                    val isCurrentMonth = dayOffset in 1..daysInCurrentMonth
                    val displayDay = when {
                        dayOffset < 1 -> daysInPrevMonth + dayOffset
                        dayOffset > daysInCurrentMonth -> dayOffset - daysInCurrentMonth
                        else -> dayOffset
                    }
                    val (actualYear, actualMonth) = when {
                        dayOffset < 1 -> Pair(prevYear, prevMonth)
                        dayOffset > daysInCurrentMonth -> Pair(
                            if (selectedMonth.monthNumber == 12) selectedMonth.year + 1 else selectedMonth.year,
                            if (selectedMonth.monthNumber == 12) 1 else selectedMonth.monthNumber + 1
                        )
                        else -> Pair(selectedMonth.year, selectedMonth.monthNumber)
                    }
                    val cellDate = kotlinx.datetime.LocalDate(actualYear, actualMonth, displayDay)
                    val dateStr = cellDate.toString()
                    val dayStatus = monthProgress.find { it.date == dateStr }?.status

                    CalendarDayItem(
                        day = displayDay,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isCurrentMonth && displayDay == selectedDay,
                        isToday = cellDate == today,
                        status = dayStatus,
                        showLeftRule = col > 0,
                        onClick = { if (isCurrentMonth) viewModel.selectDay(displayDay) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    status: String?,
    showLeftRule: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Gün durumu — hiç tamamlanmamış (kırmızı) / kısmen (sarı) / tamamı (neon yeşil)
    val indicatorColor = when {
        !isCurrentMonth -> Color.Transparent
        status == "FULL" -> ModernistDark.Lime
        status == "PARTIAL" -> CalendarDayYellow
        status == "NONE" -> CalendarDayRed
        else -> ModernistDark.BorderWeak // NO_PLAN — no judgement, neutral
    }
    val dayTextColor = when {
        isSelected -> ModernistDark.OnLime
        !isCurrentMonth -> ModernistDark.TextFaint
        else -> ModernistDark.TextPrimary
    }

    Box(
        modifier = modifier
            .height(50.dp)
            .then(if (showLeftRule) Modifier.leftRule(1.dp, ModernistDark.BorderWeak) else Modifier)
            .then(if (isSelected) Modifier.background(ModernistDark.Lime) else Modifier)
            .then(if (isToday && !isSelected) Modifier.border(2.dp, ModernistDark.Lime) else Modifier)
            .clickable(enabled = isCurrentMonth, onClick = onClick)
            .padding(top = 7.dp, start = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = day.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = dayTextColor
            )
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .width(14.dp)
                    .height(3.dp)
                    .background(indicatorColor)
            )
        }
    }
}

/**
 * Selected day's (or the month's, when nothing is selected) summary —
 * kas grubu / tamamlanan set / süre / kalori — folded right under the grid.
 */
@Composable
private fun CalendarDayDetail(
    viewModel: com.example.aigymapp.ui.viewmodels.ProgressViewModel,
    selectedMonth: kotlinx.datetime.LocalDateTime,
    selectedDay: Int?,
    onDetailClick: () -> Unit
) {
    val muscleGroups by viewModel.selectedDayMuscleGroups.collectAsState()
    val totalSets by if (selectedDay != null) viewModel.selectedDayTotalSets.collectAsState() else viewModel.totalSets.collectAsState()
    val selectedDayDuration by viewModel.selectedDayDuration.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()
    val calories by if (selectedDay != null) viewModel.selectedDayCalories.collectAsState() else viewModel.totalCalories.collectAsState()

    val duration = if (selectedDay != null) selectedDayDuration else totalDuration
    val formattedDuration = run {
        val totalSeconds = duration / 1000
        val minutes = (totalSeconds / 60).toString().padStart(2, '0')
        val seconds = (totalSeconds % 60).toString().padStart(2, '0')
        "$minutes:$seconds"
    }
    val muscleGroupsText = if (muscleGroups.isNotEmpty()) muscleGroups.joinToString(", ") else "—"

    val headerLabel = if (selectedDay != null) {
        val date = kotlinx.datetime.LocalDate(selectedMonth.year, selectedMonth.month, selectedDay)
        "${date.dayOfMonth} ${turkishMonthName(date.monthNumber)} · ${turkishWeekdayName(date.dayOfWeek)}"
    } else {
        "${turkishMonthName(selectedMonth.monthNumber)} ${selectedMonth.year} · Aylık Özet"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, ModernistDark.BorderStrong)
                .padding(horizontal = 22.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerLabel.uppercase(),
                fontSize = 9.sp,
                letterSpacing = 1.3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ModernistDark.Lime,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = "DETAY",
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = ModernistDark.TextDim,
                modifier = Modifier.clickable(enabled = selectedDay != null, onClick = onDetailClick)
            )
        }

        Column(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).topRule(1.dp, ModernistDark.BorderWeak),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalendarDetailCell(
                    value = muscleGroupsText,
                    label = "KAS GRUBU",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    valueFontSize = 19.sp
                )
                CalendarDetailCell(
                    value = totalSets.toString(),
                    label = "TAMAMLANAN SET",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    showLeftRule = true,
                    valueColor = ModernistDark.Lime
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).topRule(1.dp, ModernistDark.BorderWeak),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalendarDetailCell(
                    value = formattedDuration,
                    label = "SÜRE",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                CalendarDetailCell(
                    value = calories.toString(),
                    label = "KALORİ ≈",
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    showLeftRule = true
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).topRule(2.dp, ModernistDark.BorderStrong))
    }
}

@Composable
private fun CalendarDetailCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    showLeftRule: Boolean = false,
    valueColor: Color = ModernistDark.TextPrimary,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 26.sp
) {
    Column(
        modifier = modifier
            .then(if (showLeftRule) Modifier.leftRule(1.dp, ModernistDark.BorderWeak) else Modifier)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.3).sp,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 0.dp)
        )
    }
}

private fun turkishMonthName(month: Int): String = when (month) {
    1 -> "Ocak"; 2 -> "Şubat"; 3 -> "Mart"; 4 -> "Nisan"; 5 -> "Mayıs"; 6 -> "Haziran"
    7 -> "Temmuz"; 8 -> "Ağustos"; 9 -> "Eylül"; 10 -> "Ekim"; 11 -> "Kasım"; 12 -> "Aralık"
    else -> ""
}

private fun turkishWeekdayName(dayOfWeek: kotlinx.datetime.DayOfWeek): String = when (dayOfWeek) {
    kotlinx.datetime.DayOfWeek.MONDAY -> "Pazartesi"
    kotlinx.datetime.DayOfWeek.TUESDAY -> "Salı"
    kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Çarşamba"
    kotlinx.datetime.DayOfWeek.THURSDAY -> "Perşembe"
    kotlinx.datetime.DayOfWeek.FRIDAY -> "Cuma"
    kotlinx.datetime.DayOfWeek.SATURDAY -> "Cumartesi"
    kotlinx.datetime.DayOfWeek.SUNDAY -> "Pazar"
    else -> ""
}


/**
 * 🎬 AnimatedStatCard - Staggered giriş animasyonu
 */
@Composable
private fun AnimatedStatCard(
    delay: Int,
    animationKey: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember(animationKey) { mutableStateOf(false) }
    val alpha = remember(animationKey) { Animatable(0f) }
    val scale = remember(animationKey) { Animatable(0.8f) }
    
    LaunchedEffect(animationKey) {
        alpha.snapTo(0f)
        scale.snapTo(0.8f)
        isVisible = false
        
        delay(delay.toLong())
        isVisible = true
        
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
    
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.scaleX = scale.value
            this.scaleY = scale.value
        }
    ) {
        content()
    }
}

/**
 * Individual Stat Card
 * Hiyerarşi: icon (üst, küçük) -> value (orta, büyük & belirgin) -> label (alt, soluk)
 */
@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(2.dp, ModernistDark.BorderStrong)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = title.uppercase(),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Minimal, tek renkli çizgi ikonlar (stroke-only, dolgusuz)
 * Boyut: ~22dp, Stroke: 1.5dp, Renk: neon yeşil (#CCFF00)
 */
@Composable
private fun LineIconCanvas(
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 1.5.dp,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.(strokePx: Float) -> Unit
) {
    Canvas(modifier = modifier.size(iconSize)) {
        onDraw(strokeWidth.toPx())
    }
}

@Composable
private fun MuscleIcon(modifier: Modifier = Modifier) {
    LineIconCanvas(modifier = modifier) { strokePx ->
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

        // Yumruk
        drawCircle(
            color = Primary,
            radius = w * 0.14f,
            center = Offset(w * 0.22f, h * 0.30f),
            style = stroke
        )

        // Kol + biseps kabarıklığı (dış hat)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.24f, h * 0.42f)
            cubicTo(w * 0.30f, h * 0.55f, w * 0.34f, h * 0.62f, w * 0.40f, h * 0.72f)
            cubicTo(w * 0.48f, h * 0.84f, w * 0.62f, h * 0.84f, w * 0.70f, h * 0.74f)
            cubicTo(w * 0.80f, h * 0.62f, w * 0.80f, h * 0.40f, w * 0.72f, h * 0.26f)
            cubicTo(w * 0.66f, h * 0.16f, w * 0.56f, h * 0.14f, w * 0.50f, h * 0.20f)
            cubicTo(w * 0.46f, h * 0.24f, w * 0.46f, h * 0.30f, w * 0.50f, h * 0.34f)
        }
        drawPath(path = path, color = Primary, style = stroke)
    }
}

@Composable
private fun CheckCircleIcon(modifier: Modifier = Modifier) {
    LineIconCanvas(modifier = modifier) { strokePx ->
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

        drawCircle(
            color = Primary,
            radius = w * 0.42f,
            center = Offset(w / 2f, h / 2f),
            style = stroke
        )

        val checkPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.70f, h * 0.36f)
        }
        drawPath(path = checkPath, color = Primary, style = stroke)
    }
}

@Composable
private fun ClockIcon(modifier: Modifier = Modifier) {
    LineIconCanvas(modifier = modifier) { strokePx ->
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * 0.55f)

        drawCircle(
            color = Primary,
            radius = w * 0.38f,
            center = center,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
        // Akrep
        drawLine(
            color = Primary,
            start = center,
            end = Offset(center.x, center.y - w * 0.20f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        // Yelkovan
        drawLine(
            color = Primary,
            start = center,
            end = Offset(center.x + w * 0.16f, center.y),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        // Üst düğme (kronometre görünümü)
        drawLine(
            color = Primary,
            start = Offset(w * 0.42f, h * 0.08f),
            end = Offset(w * 0.58f, h * 0.08f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun FlameIcon(modifier: Modifier = Modifier) {
    LineIconCanvas(modifier = modifier) { strokePx ->
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.50f, h * 0.06f)
            cubicTo(w * 0.30f, h * 0.30f, w * 0.66f, h * 0.34f, w * 0.54f, h * 0.50f)
            cubicTo(w * 0.50f, h * 0.44f, w * 0.44f, h * 0.44f, w * 0.42f, h * 0.50f)
            cubicTo(w * 0.30f, h * 0.62f, w * 0.34f, h * 0.82f, w * 0.50f, h * 0.94f)
            cubicTo(w * 0.68f, h * 0.86f, w * 0.82f, h * 0.70f, w * 0.76f, h * 0.52f)
            cubicTo(w * 0.72f, h * 0.62f, w * 0.62f, h * 0.64f, w * 0.60f, h * 0.56f)
            cubicTo(w * 0.66f, h * 0.40f, w * 0.62f, h * 0.20f, w * 0.50f, h * 0.06f)
            close()
        }
        drawPath(path = path, color = Primary, style = stroke)
    }
}

// ========================================
// Stats Content Components (Original)
// ========================================

/**
 * Date Selector Row with Dropdown (GERÇEK TARİH + Period Seçimi)
 * Figma pixel-perfect design
 */
@Composable
private fun DateSelectorRowWithDropdown(
    selectedPeriod: TimePeriod,
    dateRangeText: String,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // GERÇEK tarih aralığı
        Text(
            text = dateRangeText.ifEmpty { "Yükleniyor..." },
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextPrimary
        )

        // Dropdown Button — flat bordered rect, no rounding
        Box {
            Row(
                modifier = Modifier
                    .border(2.dp, ModernistDark.BorderStrong)
                    .clickable { expanded = true }
                    .padding(start = 14.dp, end = 14.dp, top = 7.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedPeriod) {
                        TimePeriod.WEEKLY -> "HAFTA"
                        TimePeriod.MONTHLY -> "AY"
                        TimePeriod.YEARLY -> "YIL"
                    },
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernistDark.TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = ModernistDark.Lime,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ModernistDark.Bg)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Hafta",
                            color = if (selectedPeriod == TimePeriod.WEEKLY) ModernistDark.Lime else ModernistDark.TextMuted,
                            fontWeight = if (selectedPeriod == TimePeriod.WEEKLY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onPeriodSelected(TimePeriod.WEEKLY)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Ay",
                            color = if (selectedPeriod == TimePeriod.MONTHLY) ModernistDark.Lime else ModernistDark.TextMuted,
                            fontWeight = if (selectedPeriod == TimePeriod.MONTHLY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onPeriodSelected(TimePeriod.MONTHLY)
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Yıl",
                            color = if (selectedPeriod == TimePeriod.YEARLY) ModernistDark.Lime else ModernistDark.TextMuted,
                            fontWeight = if (selectedPeriod == TimePeriod.YEARLY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onPeriodSelected(TimePeriod.YEARLY)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Monthly Stats Summary Card - Modern Glassmorphism Tasarım
 * 2x2 Grid ile özet istatistikler
 */
@Composable
private fun MonthlyStatsSummaryCard(
    completedWorkouts: Int,
    totalWorkouts: Int,
    totalSets: Long,
    totalCalories: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Başlık
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈",
                    fontSize = 24.sp
                )
                Text(
                    text = "Aylık Özet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                )
            }
            
            // 2x2 Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        icon = Icons.Default.FitnessCenter,
                        label = "Antrenman",
                        value = "$completedWorkouts/$totalWorkouts",
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        icon = Icons.Default.Bolt,
                        label = "Toplam Set",
                        value = totalSets.toString(),
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Kalori",
                        value = "$totalCalories kcal",
                        color = Color(0xFFFF4D6D),
                        modifier = Modifier.weight(1f)
                    )
                    val completionPercent = if (totalWorkouts > 0) {
                        ((completedWorkouts.toFloat() / totalWorkouts) * 100).toInt()
                    } else 0
                    MiniStatCard(
                        icon = Icons.Default.TrendingUp,
                        label = "Tamamlama",
                        value = "$completionPercent%",
                        color = Color(0xFF4DFF88),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Mini Stat Card - Glassmorphism style
 */
@Composable
private fun MiniStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 20.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnTertiary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Tamamlanan Setler Kartı - Dairesel Progress Göstergeli
 * Referans görseldeki tasarım: Haftalık/Aylık/Yıllık seçimli, dairesel progress
 * Siyah arka plan + Neon yeşil vurgu rengi
 * 
 * 🎬 Animasyonlar:
 * - Circular progress: 0'dan hedefe 1.5 saniyede animate
 * - Büyük sayı: Spring ile 0'dan 1'e scale pop efekti
 */
@Composable
private fun CompletedSetsProgressCard(
    totalWorkouts: Int,
    completedSets: Long,
    plannedSets: Long,
    selectedPeriod: TimePeriod
) {
    val completionPercent = if (plannedSets > 0) {
        ((completedSets.toFloat() / plannedSets.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else if (completedSets > 0) 100 else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, ModernistDark.BorderStrong)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatsSummaryColumn(
            modifier = Modifier.weight(1f),
            value = totalWorkouts.toString(),
            valueColor = ModernistDark.Lime,
            label = "ANTRENMAN"
        )
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(ModernistDark.BorderWeak))
        StatsSummaryColumn(
            modifier = Modifier.weight(1f),
            value = completedSets.toString(),
            valueColor = ModernistDark.TextPrimary,
            label = "SET"
        )
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(ModernistDark.BorderWeak))
        StatsSummaryColumn(
            modifier = Modifier.weight(1f),
            value = "%$completionPercent",
            valueColor = ModernistDark.TextPrimary,
            label = "TAMAMLAMA"
        )
    }
}

@Composable
private fun StatsSummaryColumn(
    value: String,
    valueColor: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color = valueColor,
            modifier = Modifier.offset(y = 3.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextMuted,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

/**
 * Monthly Activity Card with Bar Chart (GERÇEK VERİ)
 */
@Composable
private fun MonthlyActivityCard(
    monthlyWorkoutCounts: Map<String, Long>,
    selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    animationKey: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, ModernistDark.BorderStrong)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(
            text = when (selectedPeriod) {
                TimePeriod.WEEKLY -> "HAFTALIK AKTİVİTE"
                TimePeriod.MONTHLY -> "AYLIK AKTİVİTE"
                TimePeriod.YEARLY -> "YILLIK AKTİVİTE"
            },
            fontSize = 9.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextMuted
        )

        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            MonthlyActivityBarChart(
                monthlyWorkoutCounts = monthlyWorkoutCounts,
                selectedPeriod = selectedPeriod,
                animationKey = animationKey
            )
        }
    }
}

/**
 * Custom Bar Chart using Canvas (GERÇEK VERİ)
 * Haftalık: 7 gün (Pzt-Paz), Aylık: Tüm günler, Yıllık: 12 ay
 * BOŞ GÜNLER DE GÖSTERİLİR (0 olarak)
 * 
 * 🎬 Bar Animasyonu: Her bar 0'dan hedef yüksekliğe animate olur
 * 📊 Tıklama ile sayı gösterme: Bar üstüne tıklayınca antrenman sayısı görünür
 */
@Composable
private fun MonthlyActivityBarChart(
    monthlyWorkoutCounts: Map<String, Long>,
    selectedPeriod: TimePeriod = TimePeriod.WEEKLY,
    animationKey: Int = 0
) {
    // Seçili bar index'i - tıklanan bar'ı takip eder
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    
    // Haftalık için 7 gün (Pzt-Paz), Aylık için 30-31 gün oluştur
    val displayData: List<Pair<String, Float>> = when (selectedPeriod) {
        TimePeriod.WEEKLY -> {
            // Hafta günleri: Pzt, Sal, Çar, Per, Cum, Cmt, Paz
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date
            val dayOfWeek = today.dayOfWeek.ordinal.toLong()
            val weekStart = today.minus(dayOfWeek, DateTimeUnit.DAY)
            
            (0..6).map { dayOffset ->
                val date = weekStart.plus(dayOffset.toLong(), DateTimeUnit.DAY)
                val dateStr = date.toString()
                val count = monthlyWorkoutCounts[dateStr] ?: 0L
                val dayName = when (dayOffset) {
                    0 -> "Pzt"
                    1 -> "Sal"
                    2 -> "Çar"
                    3 -> "Per"
                    4 -> "Cum"
                    5 -> "Cmt"
                    6 -> "Paz"
                    else -> ""
                }
                dayName to count.toFloat()
            }
        }
        TimePeriod.MONTHLY -> {
            // Ayın tüm günleri (1-30/31)
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date
            val year = today.year
            val month = today.monthNumber
            
            // Ayın kaç günü olduğunu hesapla
            val firstDayOfMonth = LocalDate(year, month, 1)
            val firstDayOfNextMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
            val lastDayOfMonth = firstDayOfNextMonth.minus(1, DateTimeUnit.DAY)
            val daysInMonth = lastDayOfMonth.dayOfMonth
            
            (1..daysInMonth).map { day ->
                val date = LocalDate(year, month, day)
                val dateStr = date.toString()
                val count = monthlyWorkoutCounts[dateStr] ?: 0L
                day.toString() to count.toFloat()
            }
        }
        TimePeriod.YEARLY -> {
            // 12 ay
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date
            val year = today.year
            val monthNames = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
            
            (1..12).map { monthNum ->
                // Her ay için toplam workout sayısını hesapla
                val monthStart = LocalDate(year, monthNum, 1)
                val nextMonthStart = monthStart.plus(1, DateTimeUnit.MONTH)
                val monthEnd = nextMonthStart.minus(1, DateTimeUnit.DAY)
                
                var totalCount = 0L
                var currentDate = monthStart
                while (currentDate <= monthEnd) {
                    totalCount += monthlyWorkoutCounts[currentDate.toString()] ?: 0L
                    currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                }
                
                monthNames[monthNum - 1] to totalCount.toFloat()
            }
        }
    }
    
    // Boş veri kontrolü
    val hasData = displayData.any { it.second > 0 }
    if (!hasData) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Veri Yok",
                    tint = ModernistDark.TextFaint,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = when (selectedPeriod) {
                        TimePeriod.WEEKLY -> "Bu hafta için aktivite verisi yok"
                        TimePeriod.MONTHLY -> "Bu ay için aktivite verisi yok"
                        TimePeriod.YEARLY -> "Bu yıl için aktivite verisi yok"
                    },
                    fontSize = 13.sp,
                    color = ModernistDark.TextDim
                )
            }
        }
        return
    }
    
    val maxValue = (displayData.maxOfOrNull { it.second } ?: 4f).coerceAtLeast(4f)
    val yAxisMax = ((maxValue + 3) / 4).toInt() * 4 // Yuvarla 4'ün katına
    val yAxisLabels = (0..yAxisMax step (yAxisMax / 4).coerceAtLeast(1)).toList()

    // Y-ekseni etiketleri başlıkla ("HAFTALIK AKTİVİTE") aynı sol kenardan başlasın diye
    // gerekli genişliği en geniş etiketin ölçüsüne göre hesaplıyoruz (başlık yerinden oynamaz)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val yAxisLabelStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal
    )
    val widestYAxisLabel = yAxisLabels.maxByOrNull { it.toString().length }?.toString() ?: "0"
    val yAxisLabelWidthPx = remember(widestYAxisLabel) {
        textMeasurer.measure(widestYAxisLabel, style = yAxisLabelStyle).size.width.toFloat()
    }
    val yAxisGapDp = 6.dp
    val yAxisWidthDp = with(density) { yAxisLabelWidthPx.toDp() } + yAxisGapDp

    // 🎬 Bar animasyonları - Her bar için ayrı Animatable
    val barAnimations = remember(animationKey, displayData.size) {
        List(displayData.size) { Animatable(0f) }
    }
    
    // 🎬 Sıralı bar animasyonları başlat
    LaunchedEffect(animationKey, displayData) {
        // Reset
        barAnimations.forEach { it.snapTo(0f) }
        
        // Kısa gecikme
        delay(100)
        
        // Her bar sırayla animate olsun (staggered)
        displayData.forEachIndexed { index, (_, value) ->
            launch {
                delay(index * 30L) // 30ms stagger
                barAnimations[index].animateTo(
                    targetValue = if (yAxisMax > 0) value / yAxisMax else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 🎬 Bar Chart: y-ekseni overlay ile tam genişlik — barlar başlık "H" ile hizalı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            var barPositions by remember { mutableStateOf<List<Triple<Float, Float, Float>>>(emptyList()) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayData) {
                        detectTapGestures { tapOffset ->
                            val clickedIndex = barPositions.indexOfFirst { triple ->
                                val barX = triple.first
                                val bw = triple.second
                                tapOffset.x >= barX && tapOffset.x <= barX + bw
                            }
                            selectedBarIndex = if (clickedIndex >= 0 && clickedIndex == selectedBarIndex) null
                                              else if (clickedIndex >= 0) clickedIndex
                                              else null
                        }
                    }
            ) {
                val chartLeft = yAxisWidthDp.toPx()
                val chartWidth = size.width - chartLeft
                val chartHeight = size.height
                val totalBars = displayData.size

                // Y-ekseni etiketleri + grid çizgileri — drawText ile piksel-perfect hizalama
                // reversed: en büyük değer üste (y=0), 0 en alta (y=chartHeight)
                val gridLineColor = ModernistDark.BorderWeak
                val labelStyle = androidx.compose.ui.text.TextStyle(
                    color = ModernistDark.TextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
                yAxisLabels.reversed().forEachIndexed { index, label ->
                    val yPos = (index.toFloat() / (yAxisLabels.size - 1)) * chartHeight
                    // Grid çizgisi (sadece bar alanında)
                    drawLine(
                        color = gridLineColor,
                        start = Offset(chartLeft, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1.dp.toPx()
                    )
                    // Y-ekseni etiketi: sol kenarı başlıkla ("HAFTALIK AKTİVİTE") hizalı
                    val textLayout = textMeasurer.measure(label.toString(), style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            x = 0f,
                            y = yPos - textLayout.size.height / 2f
                        )
                    )
                }

                // Bar hesaplamaları
                val slotWidth = chartWidth / totalBars
                val barWidth = when {
                    totalBars <= 12 -> 20.dp.toPx()
                    else -> (20.dp.toPx()).coerceAtMost(slotWidth * 0.75f)
                }

                val newBarPositions = mutableListOf<Triple<Float, Float, Float>>()

                displayData.forEachIndexed { index, (_, value) ->
                    val animatedHeight = barAnimations.getOrNull(index)?.value ?: 0f
                    val barHeight = animatedHeight * chartHeight
                    val isZero = value == 0f

                    val slotCenterX = chartLeft + slotWidth * index + slotWidth / 2
                    val x = slotCenterX - barWidth / 2

                    newBarPositions.add(Triple(x, barWidth, chartHeight - barHeight))

                    if (isZero) {
                        val phHeight = 3.dp.toPx()
                        drawRect(
                            color = ModernistDark.BorderWeak,
                            topLeft = Offset(x, chartHeight - phHeight),
                            size = Size(barWidth, phHeight)
                        )
                    } else {
                        val isSelected = selectedBarIndex == index
                        val barColor = if (isSelected) ModernistDark.TextPrimary else ModernistDark.Lime
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, chartHeight - barHeight),
                            size = Size(barWidth, barHeight.coerceAtLeast(3f))
                        )
                    }
                }

                barPositions = newBarPositions
            }

            // 📊 Seçili bar tooltip — bar'ın hemen üstünde
            selectedBarIndex?.let { index ->
                if (index in displayData.indices && barPositions.isNotEmpty() && index in barPositions.indices) {
                    val (barX, barWidth, barTopY) = barPositions[index]
                    val workoutCount = displayData[index].second.toInt()
                    if (workoutCount > 0) {
                        val tooltipDensity = androidx.compose.ui.platform.LocalDensity.current
                        val barCenterX = with(tooltipDensity) { (barX + barWidth / 2).toDp() }
                        val tooltipTopY = with(tooltipDensity) { barTopY.toDp() }
                        val tooltipWidth = if (workoutCount >= 10) 26.dp else 20.dp
                        Box(
                            modifier = Modifier
                                .offset(x = barCenterX - tooltipWidth / 2, y = tooltipTopY - 38.dp)
                                .background(Color.Black)
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = workoutCount.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day labels — bar alanıyla hizalı (y-ekseni ile aynı offset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = yAxisWidthDp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            when {
                selectedPeriod == TimePeriod.MONTHLY && displayData.size > 15 -> {
                    displayData.forEach { (day, _) ->
                        val shouldShow = day.toIntOrNull()?.let { it % 5 == 0 || it == 1 } ?: false
                        Text(
                            text = if (shouldShow) day else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnTertiary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    displayData.forEach { (day, _) ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnTertiary,
                                fontSize = if (displayData.size <= 12) 10.sp else 9.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Muscle Group Distribution Card with Donut Chart
 * Profesyonel spor uygulamalarındaki gibi modern tasarım
 * Figma pixel-perfect design
 */
@Composable
private fun MuscleGroupDistributionCard(
    muscleGroupDistribution: Map<String, Long>,
    animationKey: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, ModernistDark.BorderStrong)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(
            text = "KAS GRUBU DAĞILIMI",
            fontSize = 9.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ModernistDark.TextMuted
        )

        if (muscleGroupDistribution.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz veri yok",
                    fontSize = 13.sp,
                    color = ModernistDark.TextDim
                )
            }
        } else {
            MuscleGroupBarList(muscleGroupDistribution = muscleGroupDistribution)
        }
    }
}

// Her kas grubu için ayrı renk — donut chart'ta kullanılan orijinal palet.
private val MuscleGroupColors = mapOf(
    "Göğüs" to Color(0xFFFF6B35),
    "Sırt" to Color(0xFFFF4757),
    "Bacak" to Color(0xFFFFD93D),
    "Omuz" to Color(0xFFA55EEA),
    "Kol" to Color(0xFF2ED573),
    "Karın" to Color(0xFF1E90FF),
    "Kardiyo" to Color(0xFFFF6B81),
    "Tüm Vücut" to Color(0xFFC5FF00)
)
private val MuscleGroupFallbackColor = Color(0xFF888888)

/**
 * Flat horizontal bar list — matches the design's "Kas grubu dağılımı" pattern
 * (a proportional bar per group instead of a donut segment), each group kept
 * in its own distinct color.
 */
@Composable
private fun MuscleGroupBarList(muscleGroupDistribution: Map<String, Long>) {
    val sorted = remember(muscleGroupDistribution) {
        muscleGroupDistribution.entries.sortedByDescending { it.value }
    }
    val maxCount = sorted.maxOfOrNull { it.value } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        sorted.forEach { (name, count) ->
            val fillColor = MuscleGroupColors[name] ?: MuscleGroupFallbackColor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernistDark.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(64.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(ModernistDark.BorderWeak)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f))
                            .background(fillColor)
                    )
                }
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernistDark.TextPrimary,
                    modifier = Modifier.width(28.dp).padding(start = 8.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Professional Donut Chart - Canvas ile çizim
 * Figma tasarımına uygun kalın çember ve canlı renkler
 * 
 * 🎬 Donut Animasyonu: 0'dan tam dolu hale 1.2 saniyede animate
 * 📊 Tıklama ile sayı gösterme: Segment üstüne tıklayınca set sayısı görünür
 */
@Composable
private fun MuscleGroupDonutChart(
    muscleGroupDistribution: Map<String, Long>,
    muscleColors: Map<String, Color>,
    animationKey: Int = 0
) {
    val total = muscleGroupDistribution.values.sum().toFloat()
    
    // Seçili segment index'i - tıklanan segment'i takip eder
    var selectedSegmentIndex by remember { mutableStateOf<Int?>(null) }
    
    // Map'i sıralı listeye çevir (büyükten küçüğe)
    val sortedData = muscleGroupDistribution.entries
        .sortedByDescending { it.value }
        .map { (group, count) ->
            Triple(group, count.toFloat(), muscleColors[group] ?: Color(0xFF888888))
        }
    
    // Segment açı aralıklarını hesapla
    val segmentAngles = remember(sortedData) {
        var startAngle = -90f
        val gapAngle = 3f
        sortedData.map { (_, value, _) ->
            val sweepAngle = ((value / total) * 360f) - gapAngle
            val range = startAngle to (startAngle + sweepAngle)
            startAngle += sweepAngle + gapAngle
            range
        }
    }
    
    // 🎬 Donut progress animasyonu
    val donutProgress = remember(animationKey) { Animatable(0f) }
    
    LaunchedEffect(animationKey) {
        donutProgress.snapTo(0f)
        delay(150)
        donutProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .pointerInput(sortedData) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        
                        // Tıklanan noktanın merkeze uzaklığı
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)
                        
                        // Donut'un iç ve dış yarıçapı
                        val diameter = size.width.toFloat()
                        val strokeWidth = diameter * 0.22f
                        val outerRadius = diameter / 2
                        val innerRadius = outerRadius - strokeWidth
                        
                        // Tıklama donut alanında mı kontrol et
                        if (distance >= innerRadius && distance <= outerRadius) {
                            // Açıyı hesapla (-180 ile 180 arası, sonra -90 offset ile düzelt)
                            var angle = atan2(dy, dx) * (180f / PI.toFloat())
                            // Açıyı -90 başlangıç noktasına göre düzelt ve 0-360 aralığına çevir
                            angle = (angle + 90f + 360f) % 360f - 90f
                            if (angle < -90f) angle += 360f
                            
                            // Hangi segmente tıklandığını bul
                            val clickedIndex = segmentAngles.indexOfFirst { pair ->
                                val start = pair.first
                                val end = pair.second
                                angle >= start && angle <= end
                            }
                            
                            selectedSegmentIndex = if (clickedIndex >= 0 && clickedIndex == selectedSegmentIndex) {
                                null // Aynı segmente tekrar tıklanırsa kapat
                            } else if (clickedIndex >= 0) {
                                clickedIndex
                            } else {
                                null
                            }
                        } else {
                            // Donut dışına tıklandıysa seçimi kaldır
                            selectedSegmentIndex = null
                        }
                    }
                }
        ) {
            val diameter = size.minDimension
            val strokeWidth = diameter * 0.22f // Kalın çember (Figma'daki gibi)
            val radius = (diameter - strokeWidth) / 2
            val gapAngle = 3f // Segmentler arası boşluk
            
            var startAngle = -90f
            
            // 🎬 Animasyonlu sweep angle
            val animatedTotalSweep = donutProgress.value * 360f
            var accumulatedSweep = 0f
            
            sortedData.forEachIndexed { index, (_, value, color) ->
                val targetSweepAngle = ((value / total) * 360f) - gapAngle
                
                // Animasyona göre ne kadar çizileceğini hesapla
                val remainingSweep = animatedTotalSweep - accumulatedSweep
                val actualSweepAngle = targetSweepAngle.coerceAtMost(remainingSweep).coerceAtLeast(0f)
                
                // Seçili segment için daha kalın çizim
                val isSelected = selectedSegmentIndex == index
                val currentStrokeWidth = if (isSelected) strokeWidth * 1.15f else strokeWidth
                
                if (actualSweepAngle > 0) {
                    drawArc(
                        color = if (isSelected) color else color.copy(alpha = if (selectedSegmentIndex != null && !isSelected) 0.5f else 1f),
                        startAngle = startAngle,
                        sweepAngle = actualSweepAngle,
                        useCenter = false,
                        topLeft = Offset(
                            (diameter - (diameter - currentStrokeWidth)) / 2,
                            (diameter - (diameter - currentStrokeWidth)) / 2
                        ),
                        size = Size(diameter - currentStrokeWidth, diameter - currentStrokeWidth),
                        style = Stroke(
                            width = currentStrokeWidth,
                            cap = StrokeCap.Butt // Keskin köşeler (Figma'daki gibi)
                        )
                    )
                }
                
                accumulatedSweep += targetSweepAngle + gapAngle
                startAngle += targetSweepAngle + gapAngle
            }
        }
        
        // 📊 Seçili segment için ortada sayı gösterimi
        selectedSegmentIndex?.let { index ->
            if (index in sortedData.indices) {
                val (name, count, color) = sortedData[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = count.toInt().toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                    Text(
                        text = "set",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnTertiary
                        )
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    )
                }
            }
        }
    }
}

/**
 * Professional Legend - Her kas grubu için renkli dot ve set sayısı
 * Figma tasarımına uygun: Sol tarafta dot+isim, sağ tarafta set sayısı
 * 
 * 🎬 Staggered List Animasyonu: Her item sırayla görünür
 */
@Composable
private fun MuscleGroupLegend(
    muscleGroupDistribution: Map<String, Long>,
    muscleColors: Map<String, Color>,
    animationKey: Int = 0
) {
    // Büyükten küçüğe sırala
    val sortedData = muscleGroupDistribution.entries
        .sortedByDescending { it.value }
        .map { (group, count) ->
            Triple(group, count, muscleColors[group] ?: Color(0xFF888888))
        }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        sortedData.forEachIndexed { index, (name, sets, color) ->
            // 🎬 Her item için ayrı animasyon state
            var isVisible by remember(animationKey) { mutableStateOf(false) }
            val alpha = remember(animationKey) { Animatable(0f) }
            val translationX = remember(animationKey) { Animatable(-20f) }
            
            LaunchedEffect(animationKey) {
                alpha.snapTo(0f)
                translationX.snapTo(-20f)
                isVisible = false
                
                // Staggered delay - her item 80ms sonra
                delay(400L + (index * 80L))
                isVisible = true
                
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    translationX.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        this.alpha = alpha.value
                        this.translationX = translationX.value * density
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sol: Renk daire + İsim
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Renk dairesi
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    
                    // Kas grubu ismi
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = OnSurface,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                // Sağ: Set sayısı
                Text(
                    text = "$sets set",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Türkçe tarih formatı: "2026-08-28" -> "28 Ağustos 2026"
 */
private fun formatTurkishDate(dateStr: String): String {
    return try {
        val date = kotlinx.datetime.LocalDate.parse(dateStr)
        val turkishMonths = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        "${date.dayOfMonth} ${turkishMonths[date.monthNumber - 1]} ${date.year}"
    } catch (e: Exception) {
        dateStr
    }
}

/**
 * Egzersiz Geçmişi Alt Sayfa (Bottom Sheet)
 * GERÇEK VERİTABANI VERİSİ - NO MOCK/RANDOM!
 * Detaylı bilgiler: Set sayısı, ağırlık, tekrar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseHistoryDialog(
    date: String,
    exerciseDetails: List<com.example.aigymapp.ui.viewmodels.ProgressViewModel.ExerciseDetail>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ModernistDark.Bg,
        scrimColor = Color(0xBF000000),
        shape = RoundedCornerShape(0.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(2.dp)
                    .background(ModernistDark.BorderStrong)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            // Başlık ve tarih - sola hizalı
            Text(
                text = "Antrenman Geçmişi",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ModernistDark.TextPrimary
            )
            Text(
                text = formatTurkishDate(date).uppercase(),
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ModernistDark.TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (exerciseDetails.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .topRule(2.dp, ModernistDark.BorderStrong)
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Antrenman kaydı yok",
                            fontSize = 13.sp,
                            color = ModernistDark.TextDim
                        )
                    }
                } else {
                    exerciseDetails.forEachIndexed { index, exercise ->
                        val isCompleted = exercise.completedSets >= exercise.plannedSets && exercise.completedSets > 0
                        val weightText = if (exercise.weightKg > 0) "${exercise.weightKg.toInt()} kg" else "Ağırlıksız"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .topRule(if (index == 0) 2.dp else 1.dp, if (index == 0) ModernistDark.BorderStrong else ModernistDark.BorderWeak)
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (index + 1).toString().padStart(2, '0'),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCompleted) ModernistDark.Lime else ModernistDark.TextDim,
                                modifier = Modifier.width(26.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernistDark.TextPrimary
                                )
                                Text(
                                    text = "${exercise.completedSets}/${exercise.plannedSets} set · $weightText · ${exercise.reps} tekrar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ModernistDark.TextMuted,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Tamamlandı",
                                    tint = ModernistDark.Lime,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, ModernistDark.BorderStrong)
                    .clickable { onDismiss() }
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KAPAT",
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernistDark.Lime
                )
            }
        }
    }
}
