package com.example.aigymapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.ui.theme.ModernistDark as MD
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private val ErrorRed = Color(0xFFEF5350)
private val PartialYellow = Color(0xFFD1A02A)

/**
 * A 2px horizontal rule used throughout the Modernist Dark home screen to
 * separate sections instead of card elevation/rounding.
 */
@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(MD.BorderStrong)
    )
}

/**
 * Profile Header Component
 * Shows user greeting + name on the left, an avatar square on the right.
 */
@Composable
fun ProfileHeader(
    userName: String,
    avatarBytes: ByteArray? = null,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MD.Bg)
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "MERHABA",
                fontSize = 10.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userName,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                color = MD.TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, MD.BorderStrong)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAvatarClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarBytes != null) {
                androidx.compose.foundation.Image(
                    bitmap = remember(avatarBytes) { avatarBytes.toImageBitmap() },
                    contentDescription = "Profil menüsünü aç",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profil menüsünü aç",
                    tint = MD.TextDim,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Weekly day strip — flush 7-column row, no card, no rounding.
 * Selected day fills its whole column with the lime accent.
 */
@Composable
fun WeeklyCalendarCard(
    workoutViewModel: com.example.aigymapp.ui.viewmodels.WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by workoutViewModel.uiState.collectAsState()

    val weekDays = remember(uiState.selectedDate) {
        val dayOfWeek = when (uiState.selectedDate.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> 0
            kotlinx.datetime.DayOfWeek.TUESDAY -> 1
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> 2
            kotlinx.datetime.DayOfWeek.THURSDAY -> 3
            kotlinx.datetime.DayOfWeek.FRIDAY -> 4
            kotlinx.datetime.DayOfWeek.SATURDAY -> 5
            kotlinx.datetime.DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        val monday = uiState.selectedDate.minus(dayOfWeek, DateTimeUnit.DAY)
        (0..6).map { index -> monday.plus(index, DateTimeUnit.DAY) }
    }

    val selectedIndex = weekDays.indexOfFirst { it == uiState.selectedDate }.coerceAtLeast(0)
    val density = LocalDensity.current
    var rowWidthPx by remember { mutableStateOf(0) }
    var rowHeightPx by remember { mutableStateOf(0) }
    val columnWidth = with(density) { (rowWidthPx / 7).toDp() }
    val rowHeight = with(density) { rowHeightPx.toDp() }
    val fillOffsetX by animateDpAsState(
        targetValue = columnWidth * selectedIndex,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        SectionDivider()
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (rowWidthPx > 0 && rowHeightPx > 0) {
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
                weekDays.forEach { dayDate ->
                    val isSelected = dayDate == uiState.selectedDate
                    val status = uiState.monthDayStatuses[dayDate]
                    val baseIndicatorColor = when (status) {
                        com.example.aigymapp.ui.viewmodels.DayStatus.GREEN -> MD.Lime
                        com.example.aigymapp.ui.viewmodels.DayStatus.ORANGE -> PartialYellow
                        com.example.aigymapp.ui.viewmodels.DayStatus.RED -> ErrorRed
                        else -> MD.BorderWeak
                    }

                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) MD.OnLime else MD.TextMuted,
                        animationSpec = tween(220)
                    )
                    val numberColor by animateColorAsState(
                        targetValue = if (isSelected) MD.OnLime else MD.TextPrimary,
                        animationSpec = tween(220)
                    )
                    val dotColor by animateColorAsState(
                        targetValue = baseIndicatorColor,
                        animationSpec = tween(220)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { workoutViewModel.onDateSelected(dayDate) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getDayNameTurkish(dayDate).uppercase(),
                            fontSize = 9.sp,
                            letterSpacing = 0.9.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = dayDate.dayOfMonth.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = numberColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Daily Stats Strip — 3 flush columns separated by hairlines.
 * Antrenman / Gün seri shown as bold typographic numbers, Aktif Gün shown
 * as a number plus a 7-dot week row (mirrors the design's typography-first
 * approach — no rings, no rounded cards).
 */
@Composable
fun DailyStatsStrip(
    workoutViewModel: com.example.aigymapp.ui.viewmodels.WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by workoutViewModel.uiState.collectAsState()

    // Seçili günün planlanmış antrenman sayısı — sadece "bugün" için değil, seçilen
    // her gün için doğru değeri göstermesi gerekiyor (tamamlanma sayısı değil, toplam).
    val selectedDayWorkoutCount = uiState.selectedDayTotalWorkouts

    val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    val streak = remember(uiState.monthDayStatuses, today) {
        var count = 0
        var checkDate = today
        while (true) {
            val status = uiState.monthDayStatuses[checkDate]
            if (status == com.example.aigymapp.ui.viewmodels.DayStatus.GREEN) {
                count++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else if (checkDate == today) {
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }
        }
        count
    }

    val weekActiveDays = remember(uiState.monthDayStatuses, uiState.selectedDate) {
        val dayOfWeek = when (uiState.selectedDate.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> 0
            kotlinx.datetime.DayOfWeek.TUESDAY -> 1
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> 2
            kotlinx.datetime.DayOfWeek.THURSDAY -> 3
            kotlinx.datetime.DayOfWeek.FRIDAY -> 4
            kotlinx.datetime.DayOfWeek.SATURDAY -> 5
            kotlinx.datetime.DayOfWeek.SUNDAY -> 6
            else -> 0
        }
        val monday = uiState.selectedDate.minus(dayOfWeek, DateTimeUnit.DAY)
        (0..6).map { i ->
            val d = monday.plus(i, DateTimeUnit.DAY)
            val s = uiState.monthDayStatuses[d]
            s == com.example.aigymapp.ui.viewmodels.DayStatus.GREEN ||
                s == com.example.aigymapp.ui.viewmodels.DayStatus.ORANGE
        }
    }
    val activeDayCount = weekActiveDays.count { it }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(
                modifier = Modifier.weight(1f),
                value = selectedDayWorkoutCount.toString(),
                valueColor = MD.Lime,
                label = "ANTRENMAN"
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MD.BorderWeak)
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                value = streak.toString(),
                valueColor = MD.TextPrimary,
                label = "GÜN SERİ"
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MD.BorderWeak)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeDayCount.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    color = MD.TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    weekActiveDays.forEach { isActive ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(if (isActive) MD.Lime else MD.BorderWeak)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    valueColor: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically(tween(150)) { height -> height / 2 } + fadeIn(tween(150))) togetherWith
                    (slideOutVertically(tween(150)) { height -> -height / 2 } + fadeOut(tween(150)))
            },
            modifier = Modifier.offset(y = 3.dp)
        ) { animatedValue ->
            Text(
                text = animatedValue,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                color = valueColor
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextMuted,
            modifier = Modifier.offset(y = (-3).dp)
        )
    }
}

/**
 * Active Workout section — flush, divider-framed, big typographic timer.
 * SYNCED with WorkoutScreen's Current Workout state.
 */
@Composable
fun ActiveWorkoutCard(
    workoutViewModel: com.example.aigymapp.ui.viewmodels.WorkoutViewModel,
    onStartWorkoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by workoutViewModel.uiState.collectAsState()
    val currentWorkout = uiState.currentWorkout

    Column(modifier = modifier.fillMaxWidth()) {
        SectionDivider()

        // Aktif antrenman değiştiğinde (sıradaki egzersize geçildiğinde) eski içerik
        // "kapanır" (fade + shrink), yeni içerik "açılır" (fade + expand). workoutId'ye
        // göre keylenir — yoksa saniyede bir değişen elapsedTimeMs geçişi sürekli tetikler.
        AnimatedContent(
            targetState = currentWorkout?.workoutId,
            transitionSpec = {
                (fadeIn(tween(280, delayMillis = 120)) + expandVertically(
                    animationSpec = tween(280, delayMillis = 120),
                    expandFrom = Alignment.Top
                )).togetherWith(
                    fadeOut(tween(160)) + shrinkVertically(
                        animationSpec = tween(160),
                        shrinkTowards = Alignment.Top
                    )
                )
            },
            label = "activeWorkoutContent"
        ) { targetId ->
        if (targetId == null) {
            Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 20.dp)
            ) {
                val lastSession = uiState.lastSession

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "ANTRENMAN",
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextMuted,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (lastSession != null) {
                        Text(
                            text = "SON · ${relativeDayLabel(lastSession.daysAgo)}",
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = MD.TextDim,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                Text(
                    text = "Bugün henüz\nbaşlamadın",
                    fontSize = 25.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = MD.TextMuted,
                    modifier = Modifier.padding(top = 13.dp)
                )

                if (lastSession != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .topRule(1.dp, MD.BorderWeak)
                            .height(46.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SON SEANS · ${relativeDayLabel(lastSession.daysAgo)}",
                            fontSize = 9.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextMuted
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MD.TextPrimary
                                    )
                                ) { append(lastSession.label) }
                                withStyle(SpanStyle(fontSize = 12.sp, color = MD.TextDim)) { append("  /  ") }
                                withStyle(
                                    SpanStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MD.TextPrimary
                                    )
                                ) { append(lastSession.durationMinutes.toString()) }
                                withStyle(SpanStyle(fontSize = 11.sp, color = MD.TextPrimary)) { append("dk") }
                                withStyle(SpanStyle(fontSize = 12.sp, color = MD.TextDim)) { append("  /  ") }
                                withStyle(
                                    SpanStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MD.TextPrimary
                                    )
                                ) { append(lastSession.exerciseCount.toString()) }
                                withStyle(SpanStyle(fontSize = 11.sp, color = MD.TextPrimary)) { append(" egz") }
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .background(MD.Lime)
                    .clickable(onClick = onStartWorkoutClick)
                    .padding(vertical = 18.dp, horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ANTRENMAN BAŞLAT",
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.OnLime,
                    style = LocalTextStyle.current.copy(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MD.OnLime,
                    modifier = Modifier.size(20.dp)
                )
            }
            }
        } else {
            val currentWorkout = currentWorkout ?: return@AnimatedContent
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "AKTİF ANTRENMAN",
                        fontSize = 9.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.Lime
                    )
                    Text(
                        text = "SET ${(currentWorkout.completedSets + 1).coerceAtMost(currentWorkout.sets)} · ${currentWorkout.sets}",
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MD.TextDim
                    )
                }

                Text(
                    text = currentWorkout.name,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.2).sp,
                    color = MD.TextPrimary,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(54.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatElapsedTime(currentWorkout.elapsedTimeMs),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        color = if (currentWorkout.isRunning) MD.Lime else MD.TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .border(2.dp, MD.BorderStrong)
                                .clickable(enabled = !currentWorkout.isCompleted) {
                                    workoutViewModel.onCurrentWorkoutStartPause()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentWorkout.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (currentWorkout.isRunning) "Duraklat" else "Başlat",
                                tint = MD.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        val checkScale = remember { Animatable(1f) }
                        LaunchedEffect(currentWorkout.isCompleted) {
                            if (currentWorkout.isCompleted) {
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
                                .size(54.dp)
                                .graphicsLayer {
                                    scaleX = checkScale.value
                                    scaleY = checkScale.value
                                }
                                .background(MD.Lime)
                                .clickable {
                                    workoutViewModel.onCurrentWorkoutCompleted(!currentWorkout.isCompleted)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tamamla",
                                tint = MD.OnLime,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .topRule(1.dp, MD.BorderWeak)
                ) {
                    MeasurementCell(
                        modifier = Modifier.weight(1f),
                        value = currentWorkout.reps.toString(),
                        label = "TEKRAR"
                    )
                    MeasurementCell(
                        modifier = Modifier.weight(1f),
                        value = formatWeight(currentWorkout.weight),
                        label = "KG",
                        hasDivider = true
                    )
                    MeasurementCell(
                        modifier = Modifier.weight(1f),
                        value = formatWeight(currentWorkout.reps * currentWorkout.weight),
                        label = "HACİM",
                        hasDivider = true
                    )
                }
            }
        }
        }
    }
}

/**
 * Shared measurement strip cell — edge-to-edge equal thirds, full-height
 * divider, label above value. Used for the active workout card's
 * TEKRAR/KG/HACİM row.
 */
@Composable
private fun MeasurementCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = false
) {
    Column(
        modifier = modifier
            .height(60.dp)
            .then(if (hasDivider) Modifier.leftRule(1.dp, MD.BorderWeak) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextPrimary
        )
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) weight.toLong().toString() else weight.toString()
}

/**
 * "DÜN" for 1 day ago, otherwise "N GÜN ÖNCE" — used for the home screen's
 * "SON SEANS" strip so it reflects the real last-workout date.
 */
private fun relativeDayLabel(daysAgo: Long): String {
    return if (daysAgo == 1L) "DÜN" else "$daysAgo GÜN ÖNCE"
}

/**
 * Goals section — flush list, primary goal accented lime, others muted.
 * Shows user's fitness goals with real progress tracking
 * (goals tracked from COMPLETED workouts only — workout_set_ticks table).
 */
@Composable
fun GoalsCard(
    goalViewModel: com.example.aigymapp.ui.viewmodels.GoalViewModel,
    onAddGoalClick: () -> Unit,
    modifier: Modifier = Modifier,
    startBarAnimation: Boolean = true
) {
    val goalsWithProgress by goalViewModel.goalsWithProgress.collectAsState()
    val isLoading by goalViewModel.isLoading.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        SectionDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 22.dp, end = 22.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HEDEFLER",
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextMuted
                )
                Text(
                    text = "DÜZENLE",
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime,
                    modifier = Modifier.clickable(onClick = onAddGoalClick)
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MD.Lime)
                    }
                }

                goalsWithProgress.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Henüz hedef eklenmedi",
                            fontSize = 13.sp,
                            color = MD.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ HEDEF EKLE",
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.Lime,
                            modifier = Modifier.clickable(onClick = onAddGoalClick)
                        )
                    }
                }

                else -> {
                    goalsWithProgress.forEachIndexed { index, goalWithProgress ->
                        val isPrimary = index == 0
                        val accent = if (isPrimary) MD.Lime else MD.TextMuted
                        val barFill = if (isPrimary) MD.Lime else MD.TextDim

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .then(
                                    if (index > 0) Modifier
                                        .topRule(1.dp, MD.BorderWeak)
                                        .padding(top = 16.dp)
                                    else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goalWithProgress.goal.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MD.TextPrimary,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Text(
                                    text = goalWithProgress.getProgressText(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accent
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .height(6.dp)
                                    .background(MD.BorderWeak)
                            ) {
                                val targetProgress = goalWithProgress.getClampedProgressPercent() / 100f
                                val animatedProgress by animateFloatAsState(
                                    targetValue = if (startBarAnimation) targetProgress else 0f,
                                    animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
                                    label = "goalProgress"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .background(barFill)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format elapsed time from milliseconds to MM:SS format
 */
private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val minutesStr = if (minutes < 10) "0$minutes" else minutes.toString()
    val secondsStr = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$minutesStr:$secondsStr"
}

/**
 * Get Turkish day name
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

/**
 * Get Turkish month name (short)
 */
private fun getMonthNameTurkish(monthNumber: Int): String {
    return when (monthNumber) {
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
 * Get week range in Turkish format (e.g., "Kasım 10-16, 2025")
 */
private fun getWeekRangeTurkish(startDate: LocalDate, endDate: LocalDate): String {
    val monthName = getMonthNameTurkish(startDate.monthNumber)
    return "$monthName ${startDate.dayOfMonth}-${endDate.dayOfMonth}, ${startDate.year}"
}

/**
 * Bottom Navigation Bar Component
 * Original icon-only bar (Twitter/YouTube style fill animation, icons
 * transition from outlined to filled when selected) with a thin accent
 * bar added above the active tab to show which page is current.
 */
@Composable
fun GymBottomNavigation(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavigationItemData(0, Icons.Outlined.Home, Icons.Filled.Home, "Ana Sayfa", 26.dp),
        NavigationItemData(1, CustomIcons.DumbbellOutlined, CustomIcons.DumbbellFilled, "Antrenman", 26.dp),
        NavigationItemData(2, Icons.Outlined.CameraAlt, Icons.Filled.CameraAlt, "Kamera", 28.dp),
        NavigationItemData(3, CustomIcons.CalendarOutlined, CustomIcons.CalendarFilled, "İlerleme", 24.dp),
        NavigationItemData(4, Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome, "Koç", 26.dp)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
    ) {
        // Background surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {}

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = (-6).dp)
        ) {
            // Active-tab indicator bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedIndex == item.index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp)
                            .height(3.dp)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Navigation items row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedIndex == item.index
                    val isCamera = item.index == 2

                    // Animation for scale only (no elevation - stays in place like Twitter/YouTube)
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated color for icon
                        val iconColor by animateColorAsState(
                            targetValue = when {
                                // All items: WHITE when selected
                                isSelected -> Color.White
                                // Camera: neon green when NOT selected
                                isCamera -> MaterialTheme.colorScheme.primary
                                // Others: gray when not selected
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "iconColor"
                        )

                        // Icon with Twitter/YouTube style fill animation (no circular container)
                        Box(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onNavigate(item.index) }
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Switch between outlined (unselected) and filled (selected) icons
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(item.iconSize),
                                tint = iconColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for navigation item configuration with outlined/filled icon pairs and custom size
 */
private data class NavigationItemData(
    val index: Int,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val iconSize: androidx.compose.ui.unit.Dp
)
