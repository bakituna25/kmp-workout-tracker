package com.example.aigymapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.models.Goal
import com.example.aigymapp.models.GoalType
import com.example.aigymapp.ui.theme.ModernistDark as MD
import com.example.aigymapp.ui.viewmodels.GoalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

/**
 * Full-screen "Yeni Hedef" flow: a 2-step wizard (pick goal type, then pick
 * target value) followed by a reward/confirmation screen shown once the
 * goal is actually created. Replaces the previous single-screen form.
 */
@Composable
fun AddGoalFlow(
    goalViewModel: GoalViewModel,
    onDismiss: () -> Unit,
    onGoalCreatedNavigateToWorkout: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedGoalType by remember { mutableStateOf<GoalType?>(null) }
    var selectedExerciseId by remember { mutableStateOf<Long?>(null) }
    var selectedMuscleGroup by remember { mutableStateOf<String?>(null) }
    var targetValue by remember { mutableStateOf(8) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var showMuscleGroupPicker by remember { mutableStateOf(false) }
    var createdGoal by remember { mutableStateOf<Goal?>(null) }

    val exercises by goalViewModel.allExercises.collectAsState()
    val muscleGroups = remember(exercises) {
        exercises.mapNotNull { it.muscle_group }.distinct().sorted()
    }
    val selectedExercise = selectedExerciseId?.let { id -> exercises.find { it.id == id } }

    // Aynı hedeften (aynı egzersiz / aynı kas grubu / aylık hedef) ikinci kez
    // eklenmesini engellemek için mevcut aktif hedefleri referans al.
    val goalsWithProgress by goalViewModel.goalsWithProgress.collectAsState()
    val activeGoals = remember(goalsWithProgress) { goalsWithProgress.filter { it.goal.isActive } }
    val takenExerciseIds = remember(activeGoals) {
        activeGoals.filter { it.goal.goalType == GoalType.EXERCISE_COUNT }
            .mapNotNull { it.goal.exerciseId }
            .toSet()
    }
    val takenMuscleGroups = remember(activeGoals) {
        activeGoals.filter { it.goal.goalType == GoalType.MUSCLE_GROUP_COUNT }
            .mapNotNull { it.goal.muscleGroup }
            .toSet()
    }
    val hasMonthlyGoal = remember(activeGoals) {
        activeGoals.any { it.goal.goalType == GoalType.MONTHLY_DAYS }
    }
    val pickableExercises = remember(exercises, takenExerciseIds) {
        exercises.filterNot { it.id in takenExerciseIds }
    }
    val pickableMuscleGroups = remember(muscleGroups, takenMuscleGroups) {
        muscleGroups.filterNot { it in takenMuscleGroups }
    }

    val canContinueStep1 = when (selectedGoalType) {
        null -> false
        GoalType.EXERCISE_COUNT -> selectedExerciseId != null
        GoalType.MUSCLE_GROUP_COUNT -> selectedMuscleGroup != null
        GoalType.MONTHLY_DAYS -> !hasMonthlyGoal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (step <= 2) {
                GoalWizardHeader(
                    step = step,
                    onClose = onDismiss,
                    onBack = { step = (step - 1).coerceAtLeast(1) }
                )
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState == 3) {
                        // The lime field itself doesn't slide or fade in — it's
                        // just there immediately. Only the staggered content
                        // inside (GoalRewardScreen's sections) animates on top
                        // of it, so the reveal reads as "field first, then
                        // the writing" instead of the panel and its content
                        // arriving together.
                        EnterTransition.None togetherWith ExitTransition.None
                    } else if (targetState > initialState) {
                        (slideInHorizontally(tween(440, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(tween(440, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(160)))
                    } else {
                        (slideInHorizontally(tween(440, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(tween(440, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(tween(160)))
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "goalWizardStep"
            ) { animatedStep ->
                when (animatedStep) {
                    1 -> GoalStepOne(
                        selectedGoalType = selectedGoalType,
                        selectedExerciseName = selectedExercise?.name,
                        selectedMuscleGroup = selectedMuscleGroup,
                        monthlyGoalTaken = hasMonthlyGoal,
                        canContinue = canContinueStep1,
                        onSelectType = { type ->
                            if (type != GoalType.MONTHLY_DAYS || !hasMonthlyGoal) {
                                selectedGoalType = type
                                when (type) {
                                    GoalType.EXERCISE_COUNT -> {
                                        selectedMuscleGroup = null
                                        if (selectedExerciseId == null) showExercisePicker = true
                                    }
                                    GoalType.MUSCLE_GROUP_COUNT -> {
                                        selectedExerciseId = null
                                        if (selectedMuscleGroup == null) showMuscleGroupPicker = true
                                    }
                                    GoalType.MONTHLY_DAYS -> {
                                        selectedExerciseId = null
                                        selectedMuscleGroup = null
                                    }
                                }
                            }
                        },
                        onEditExercise = { showExercisePicker = true },
                        onEditMuscleGroup = { showMuscleGroupPicker = true },
                        onContinue = { step = 2 }
                    )

                    2 -> GoalStepTwo(
                        goalType = selectedGoalType ?: GoalType.MONTHLY_DAYS,
                        categoryLabel = categoryLabel(selectedGoalType, selectedExercise?.name, selectedMuscleGroup),
                        targetValue = targetValue,
                        onValueChange = { targetValue = it.coerceIn(1, 999) },
                        onSubmit = {
                            val type = selectedGoalType ?: return@GoalStepTwo
                            val today = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date

                            val finalTitle = when (type) {
                                GoalType.EXERCISE_COUNT -> "${selectedExercise?.name} - $targetValue Kere"
                                GoalType.MUSCLE_GROUP_COUNT -> "$selectedMuscleGroup - $targetValue Seans"
                                GoalType.MONTHLY_DAYS -> "$targetValue Gün Aylık Antrenman"
                            }

                            val newGoal = Goal(
                                title = finalTitle,
                                goalType = type,
                                targetValue = targetValue,
                                exerciseId = selectedExerciseId,
                                muscleGroup = selectedMuscleGroup,
                                startDate = today,
                                endDate = null
                            )

                            goalViewModel.addGoal(newGoal)
                            createdGoal = newGoal
                            step = 3
                        }
                    )

                    else -> createdGoal?.let { goal ->
                        GoalRewardScreen(
                            goal = goal,
                            onStartWorkout = onGoalCreatedNavigateToWorkout
                        )
                    }
                }
            }
        }
    }

    // Gated to step 1: these overlay the Box above as siblings, not children,
    // so if the wizard advances to step 2 while a sheet is still mid-exit
    // (e.g. the user taps "Devam et" right after picking), an un-gated
    // overlay would keep rendering on top of Step 2 until its own animation
    // finished — the "ghost" the picked box left behind. Dropping the whole
    // overlay the instant we leave step 1 removes it immediately instead.
    if (step == 1) {
        AnimatedGoalPicker(visible = showExercisePicker) {
            ExercisePickerDialog(
                exercises = pickableExercises,
                onExerciseSelected = {
                    selectedExerciseId = it.id
                    showExercisePicker = false
                },
                onDismiss = {
                    showExercisePicker = false
                    if (selectedGoalType == GoalType.EXERCISE_COUNT && selectedExerciseId == null) {
                        selectedGoalType = null
                    }
                }
            )
        }

        AnimatedGoalPicker(visible = showMuscleGroupPicker) {
            MuscleGroupPickerDialog(
                muscleGroups = pickableMuscleGroups,
                exercises = exercises,
                onMuscleGroupSelected = {
                    selectedMuscleGroup = it
                    showMuscleGroupPicker = false
                },
                onDismiss = {
                    showMuscleGroupPicker = false
                    if (selectedGoalType == GoalType.MUSCLE_GROUP_COUNT && selectedMuscleGroup == null) {
                        selectedGoalType = null
                    }
                }
            )
        }
    }
}

/**
 * "Hedefleri Düzenle" menu — the goals card's add button opens this instead
 * of jumping straight into goal creation. Lists current goals with a delete
 * action per row, and a bottom bar to launch the "Yeni Hedef" wizard.
 */
@Composable
fun GoalEditMenu(
    goalViewModel: GoalViewModel,
    onClose: () -> Unit,
    onAddGoalClick: () -> Unit
) {
    val goalsWithProgress by goalViewModel.goalsWithProgress.collectAsState()
    var goalPendingDelete by remember { mutableStateOf<Goal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 22.dp, end = 22.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(2.dp, MD.BorderStrong)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = MD.TextPrimary, modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 26.dp)) {
            Text(
                text = "Hedefleri\nDüzenle",
                fontSize = 38.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MD.TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Yeni bir hedef ekle ya da mevcutlarını sil",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MD.TextMuted
            )
        }

        if (goalsWithProgress.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Henüz hedef eklenmedi",
                    fontSize = 14.sp,
                    color = MD.TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .topRule(2.dp, MD.BorderStrong)
            ) {
                itemsIndexed(goalsWithProgress, key = { _, g -> g.goal.id }) { index, goalWithProgress ->
                    GoalEditRow(
                        title = goalWithProgress.goal.title,
                        progressText = goalWithProgress.getProgressText(),
                        showTopDivider = index > 0,
                        onDeleteClick = { goalPendingDelete = goalWithProgress.goal }
                    )
                }
            }
        }

        ContinueBar(
            label = "+ YENİ HEDEF EKLE",
            enabled = true,
            icon = Icons.Default.Add,
            onClick = onAddGoalClick
        )
    }

    goalPendingDelete?.let { goal ->
        DeleteGoalConfirmationDialog(
            goalTitle = goal.title,
            onConfirm = {
                goalViewModel.deleteGoal(goal.id)
                goalPendingDelete = null
            },
            onDismiss = { goalPendingDelete = null }
        )
    }
}

@Composable
private fun GoalEditRow(
    title: String,
    progressText: String,
    showTopDivider: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MD.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = progressText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MD.TextMuted
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(2.dp, MD.BorderStrong)
                .clickable(onClick = onDeleteClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hedefi Sil",
                tint = GoalDeleteRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DeleteGoalConfirmationDialog(
    goalTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MD.Bg,
        titleContentColor = MD.TextPrimary,
        textContentColor = MD.TextMuted,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = GoalDeleteRed,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(text = "Hedefi Sil?", fontWeight = FontWeight.Bold, color = MD.TextPrimary)
        },
        text = {
            Text(text = "\"$goalTitle\" hedefini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GoalDeleteRed,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Sil", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(text = "İptal", color = MD.TextMuted)
            }
        }
    )
}

private val GoalDeleteRed = androidx.compose.ui.graphics.Color(0xFFEF5350)

/**
 * Manually drives the picker sheets' enter/exit — a single [Animatable]
 * mapped to alpha + vertical translation, always mounted while animating
 * (including the exit leg) so there is exactly one source of truth for
 * "where is it right now" and never a framework-transition first-frame
 * snap that reads as a flicker between two positions.
 */
@Composable
private fun AnimatedGoalPicker(visible: Boolean, content: @Composable () -> Unit) {
    var mounted by remember { mutableStateOf(visible) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            mounted = true
            progress.animateTo(1f, animationSpec = tween(280, easing = FastOutSlowInEasing))
        } else {
            progress.animateTo(0f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            mounted = false
        }
    }

    if (mounted) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Fixed once from the container's own constraints — never read back
            // from the animated layer itself, so there is no feedback loop that
            // could snap the offset near the end of the transition.
            val fullHeightPx = with(LocalDensity.current) { maxHeight.roundToPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(progress.value)
                    .offset { IntOffset(0, ((1f - progress.value) * fullHeightPx).roundToInt()) }
            ) {
                content()
            }
        }
    }
}

private fun categoryLabel(type: GoalType?, exerciseName: String?, muscleGroup: String?): String =
    when (type) {
        GoalType.EXERCISE_COUNT -> (exerciseName ?: "Egzersiz").uppercase()
        GoalType.MUSCLE_GROUP_COUNT -> (muscleGroup ?: "Kas Grubu").uppercase()
        GoalType.MONTHLY_DAYS, null -> "AYLIK ANTRENMAN"
    }

private fun questionText(type: GoalType): String = when (type) {
    GoalType.MONTHLY_DAYS -> "Kaç gün\nspor yapacaksın?"
    GoalType.EXERCISE_COUNT -> "Kaç kere\nyapacaksın?"
    GoalType.MUSCLE_GROUP_COUNT -> "Kaç seans\nçalışacaksın?"
}

private fun unitLabel(type: GoalType): String = when (type) {
    GoalType.MONTHLY_DAYS -> "GÜN"
    GoalType.EXERCISE_COUNT -> "KERE"
    GoalType.MUSCLE_GROUP_COUNT -> "SEANS"
}

private fun unitWord(type: GoalType): String = when (type) {
    GoalType.MONTHLY_DAYS -> "gün"
    GoalType.EXERCISE_COUNT -> "kere"
    GoalType.MUSCLE_GROUP_COUNT -> "seans"
}

/**
 * Adım 1 — pick what kind of goal to challenge yourself with.
 */
@Composable
private fun GoalStepOne(
    selectedGoalType: GoalType?,
    selectedExerciseName: String?,
    selectedMuscleGroup: String?,
    monthlyGoalTaken: Boolean,
    canContinue: Boolean,
    onSelectType: (GoalType) -> Unit,
    onEditExercise: () -> Unit,
    onEditMuscleGroup: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        StaggeredSection(index = 0) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 26.dp)) {
                Text(
                    text = "Neyi\nhedefliyorsun?",
                    fontSize = 38.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = MD.TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Kendine bir meydan oku",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MD.TextMuted
                )
            }
        }

        StaggeredSection(index = 1) {
            Column(
                modifier = Modifier
                    .topRule(2.dp, MD.BorderStrong)
            ) {
                GoalTypeRow(
                    index = "01",
                    title = "Aylık Antrenman",
                    description = if (monthlyGoalTaken) "Zaten aktif bir hedefin var" else "Ay boyunca kaç gün spor yapacaksın?",
                    isSelected = selectedGoalType == GoalType.MONTHLY_DAYS,
                    enabled = !monthlyGoalTaken,
                    onClick = { onSelectType(GoalType.MONTHLY_DAYS) }
                )
                GoalTypeRow(
                    index = "02",
                    title = "Egzersiz Sayısı",
                    description = selectedExerciseName?.let { "Seçili: $it" } ?: "Belirli bir egzersizi X kere yap",
                    isSelected = selectedGoalType == GoalType.EXERCISE_COUNT,
                    showTopDivider = true,
                    onClick = {
                        if (selectedGoalType == GoalType.EXERCISE_COUNT) onEditExercise()
                        else onSelectType(GoalType.EXERCISE_COUNT)
                    }
                )
                GoalTypeRow(
                    index = "03",
                    title = "Kas Grubu Odak",
                    description = selectedMuscleGroup?.let { "Seçili: $it" } ?: "Haftalık kas grubu çalışma sıklığı",
                    isSelected = selectedGoalType == GoalType.MUSCLE_GROUP_COUNT,
                    showTopDivider = true,
                    onClick = {
                        if (selectedGoalType == GoalType.MUSCLE_GROUP_COUNT) onEditMuscleGroup()
                        else onSelectType(GoalType.MUSCLE_GROUP_COUNT)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        StaggeredSection(index = 2) {
            ContinueBar(
                label = "DEVAM",
                enabled = canContinue,
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun GoalTypeRow(
    index: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showTopDivider: Boolean = false,
    enabled: Boolean = true
) {
    val lineWidth by animateDpAsState(if (isSelected) 4.dp else 0.dp, tween(240, easing = FastOutSlowInEasing), label = "goalRowLine")
    val startPadding by animateDpAsState(if (isSelected) 18.dp else 22.dp, tween(240, easing = FastOutSlowInEasing), label = "goalRowPadding")
    val bgColor by animateColorAsState(if (isSelected) MD.LimeWeakBg else Color.Transparent, tween(240), label = "goalRowBg")
    val indexColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextDim, tween(240), label = "goalRowIndexColor")
    val titleColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextPrimary, tween(240), label = "goalRowTitleColor")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showTopDivider) Modifier.topRule(2.dp, MD.BorderStrong) else Modifier)
            .background(bgColor)
            .limeLeftBar(width = lineWidth)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                start = startPadding,
                end = 22.dp,
                top = 22.dp,
                bottom = 22.dp
            )
    ) {
        Text(
            text = index,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = indexColor
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MD.TextMuted
        )
    }
}

/** Left accent bar — Compose's Modifier.border() has no per-side shorthand. */
private fun Modifier.limeLeftBar(width: androidx.compose.ui.unit.Dp = 4.dp): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(
                color = MD.Lime,
                topLeft = androidx.compose.ui.geometry.Offset.Zero,
                size = androidx.compose.ui.geometry.Size(width.toPx(), size.height)
            )
        }
    )

/**
 * Adım 2 — pick the target value for the chosen goal type.
 */
@Composable
private fun GoalStepTwo(
    goalType: GoalType,
    categoryLabel: String,
    targetValue: Int,
    onValueChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        StaggeredSection(index = 0) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 20.dp)) {
                Text(
                    text = categoryLabel,
                    fontSize = 11.sp,
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = questionText(goalType),
                    fontSize = 38.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = MD.TextPrimary
                )
            }
        }

        StaggeredSection(index = 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .padding(vertical = 26.dp, horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, MD.BorderStrong)
                        .clickable(enabled = targetValue > 1) { onValueChange(targetValue - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt", tint = MD.TextMuted, modifier = Modifier.size(22.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = targetValue,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically(tween(170, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(170))) togetherWith
                                    (slideOutVertically(tween(170, easing = FastOutSlowInEasing)) { -it } + fadeOut(tween(170)))
                            } else {
                                (slideInVertically(tween(170, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(170))) togetherWith
                                    (slideOutVertically(tween(170, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(170)))
                            }
                        },
                        label = "targetValueAnim"
                    ) { animatedValue ->
                        Text(
                            text = animatedValue.toString(),
                            fontSize = 84.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-2).sp,
                            color = MD.Lime
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = unitLabel(goalType),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MD.Lime)
                        .clickable { onValueChange(targetValue + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Artır", tint = MD.OnLime, modifier = Modifier.size(22.dp))
                }
            }
        }

        StaggeredSection(index = 2) {
            Row(modifier = Modifier.fillMaxWidth().topRule(2.dp, MD.BorderStrong)) {
                listOf(8, 12, 16, 20).forEachIndexed { i, chipValue ->
                    val isChipSelected = targetValue == chipValue
                    val chipBg by animateColorAsState(if (isChipSelected) MD.Lime else Color.Transparent, tween(170), label = "chipBg")
                    val chipTextColor by animateColorAsState(if (isChipSelected) MD.OnLime else MD.TextMuted, tween(170), label = "chipTextColor")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (i > 0) Modifier.leftRule(2.dp, MD.BorderStrong) else Modifier)
                            .background(chipBg)
                            .clickable { onValueChange(chipValue) }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chipValue.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = chipTextColor
                        )
                    }
                }
            }
        }

        StaggeredSection(index = 3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.BorderStrong)
                    .limeLeftBar()
                    .padding(start = 18.dp, end = 22.dp, top = 20.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "ÖNERİ",
                    fontSize = 9.sp,
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Geçen ay 14 gün antrenman yaptın. Bu ay 16'yı hedeflemeye ne dersin?",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MD.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        StaggeredSection(index = 4) {
            ContinueBar(
                label = "HEDEF OLUŞTUR",
                icon = Icons.Default.Flag,
                enabled = true,
                onClick = onSubmit
            )
        }
    }
}

/**
 * Wizard header shared by [GoalStepOne] and [GoalStepTwo]: back/close box ·
 * step label that throws the old value up and brings the new one in from
 * below · 3-segment progress bar whose active segment widens to fill.
 */
@Composable
private fun GoalWizardHeader(step: Int, onClose: () -> Unit, onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 22.dp, end = 22.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(2.dp, MD.BorderStrong)
                    .clickable(onClick = if (step == 1) onClose else onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (step == 1) Icons.Default.Close else Icons.Default.ArrowBack,
                    contentDescription = if (step == 1) "Kapat" else "Geri",
                    tint = MD.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(tween(220, easing = FastOutSlowInEasing)) { h -> h } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { h -> -h } + fadeOut(tween(160)))
                    } else {
                        (slideInVertically(tween(220, easing = FastOutSlowInEasing)) { h -> -h } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { h -> h } + fadeOut(tween(160)))
                    }
                },
                label = "goalStepLabel"
            ) { animatedStep ->
                Text(
                    text = "0$animatedStep / 03",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextMuted
                )
            }
        }

        val animatedProgress by animateFloatAsState(
            targetValue = step.toFloat(),
            animationSpec = tween(340, easing = FastOutSlowInEasing),
            label = "goalStepProgress"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .height(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MD.BorderStrong)
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

/**
 * Staggered entrance for a step's top-level sections — settles up from 10px
 * below while fading in, offset by `index * 70ms` so a step's sections
 * cascade into place instead of appearing all at once. Each instance is
 * fresh (mounted only when its step becomes the active [AnimatedContent]
 * target), so the entrance replays every time the step is entered.
 */
@Composable
private fun StaggeredSection(
    index: Int,
    modifier: Modifier = Modifier,
    delayMs: Long = 70L,
    alphaDurationMs: Int = 380,
    translationStiffness: Float = Spring.StiffnessMediumLow,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { 10.dp.toPx() }
    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(startOffsetPx) }

    LaunchedEffect(Unit) {
        delay(index * delayMs)
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = alphaDurationMs, easing = FastOutSlowInEasing)
            )
        }
        launch {
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = translationStiffness
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

@Composable
private fun ContinueBar(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ArrowForward
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, MD.BorderStrong)
            .background(if (enabled) MD.Lime else MD.BorderStrong)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) MD.OnLime else MD.TextDim
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MD.OnLime else MD.TextDim,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Reward / confirmation screen shown right after a goal is created.
 * Progress is intentionally shown as freshly-started (0 completed) since
 * the goal has no history yet.
 */
@Composable
private fun GoalRewardScreen(
    goal: Goal,
    onStartWorkout: () -> Unit
) {
    val daysRemainingInMonth = remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val firstOfNextMonth = if (today.monthNumber == 12) {
            LocalDate(today.year + 1, 1, 1)
        } else {
            LocalDate(today.year, today.monthNumber + 1, 1)
        }
        val lastDayOfMonth = firstOfNextMonth.minus(1, DateTimeUnit.DAY)
        lastDayOfMonth.dayOfMonth - today.dayOfMonth
    }

    val cellCount = goal.targetValue.coerceAtMost(24)

    // The lime field itself has no entrance animation (the wizard's outer
    // AnimatedContent skips animating in/out of this step) — it's simply
    // there, and this staggered content is what reads as the reveal.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MD.Lime)
    ) {
        StaggeredSection(index = 0, delayMs = 120L, alphaDurationMs = 420, translationStiffness = Spring.StiffnessLow) {
            Column {
                Text(
                    text = "HEDEF OLUŞTURULDU",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.OnLime,
                    modifier = Modifier.padding(top = 36.dp, start = 22.dp, end = 22.dp)
                )

                Text(
                    text = "${goal.targetValue} ${unitWord(goal.goalType)}.\nBu ay\nsenin ayın.",
                    fontSize = 60.sp,
                    lineHeight = 58.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    color = MD.OnLime,
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 30.dp)
                )
            }
        }

        StaggeredSection(index = 1, delayMs = 120L, alphaDurationMs = 420, translationStiffness = Spring.StiffnessLow) {
            Row(modifier = Modifier.fillMaxWidth().topRule(2.dp, MD.OnLime)) {
                Column(modifier = Modifier.weight(1f).padding(20.dp)) {
                    Text(
                        text = goal.targetValue.toString(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = MD.OnLime
                    )
                    Text(
                        text = "HEDEF",
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.OnLime.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .leftRule(2.dp, MD.OnLime)
                        .padding(20.dp)
                ) {
                    Text(
                        text = daysRemainingInMonth.toString(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = MD.OnLime
                    )
                    Text(
                        text = "KALAN GÜN",
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.OnLime.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        StaggeredSection(index = 2, delayMs = 120L, alphaDurationMs = 420, translationStiffness = Spring.StiffnessLow) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .topRule(2.dp, MD.OnLime)
                    .padding(20.dp)
            ) {
                Text(
                    text = "SERİ KAYDI",
                    fontSize = 9.sp,
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.OnLime.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cellCount) { cellIndex ->
                        val cellScale = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay(cellIndex * 34L)
                            cellScale.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .graphicsLayer {
                                    scaleX = cellScale.value
                                    scaleY = cellScale.value
                                }
                                .border(1.5.dp, MD.OnLime)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "0 / ${goal.targetValue} tamamlandı",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MD.OnLime.copy(alpha = 0.65f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topRule(2.dp, MD.OnLime)
                .background(MD.OnLime)
                .clickable(onClick = onStartWorkout)
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ANTRENMANA BAŞLA",
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.Lime
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MD.Lime,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Egzersiz Seç — alt sayfa. Slides up over the (dimmed) Adım 1 screen
 * behind it; search + flush list; a name row selects, the bottom band
 * confirms and closes. Rendered directly in the caller's Box (no separate
 * Android Dialog window) so it measures against the real screen height
 * instead of a wrap-content dialog window.
 */
@Composable
fun ExercisePickerDialog(
    exercises: List<com.example.aigymapp.database.Exercises>,
    onExerciseSelected: (com.example.aigymapp.database.Exercises) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<com.example.aigymapp.database.Exercises?>(null) }
    val filtered = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .background(MD.Bg)
                .topRule(2.dp, MD.TextDim)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {}
                )
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(androidx.compose.foundation.BorderStroke(2.dp, MD.BorderStrong))
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${exercises.size} EGZERSİZ · A–Z",
                            fontSize = 9.sp,
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextMuted
                        )
                        Text(
                            text = "Egzersiz seç",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.2).sp,
                            color = MD.TextPrimary,
                            modifier = Modifier.padding(top = 9.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, MD.BorderStrong)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = MD.TextMuted, modifier = Modifier.size(20.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MD.TextDim, modifier = Modifier.size(18.dp))
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .topRule(2.dp, MD.BorderStrong)
                ) {
                    itemsIndexed(filtered, key = { _, ex -> ex.id }) { index, exercise ->
                        val isSelected = selected?.id == exercise.id
                        val lineWidth by animateDpAsState(if (isSelected) 4.dp else 0.dp, tween(240, easing = FastOutSlowInEasing), label = "pickerRowLine")
                        val startPadding by animateDpAsState(if (isSelected) 18.dp else 22.dp, tween(240, easing = FastOutSlowInEasing), label = "pickerRowPadding")
                        val titleColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextPrimary, tween(240), label = "pickerRowTitleColor")
                        val boxBg by animateColorAsState(if (isSelected) MD.Lime else Color.Transparent, tween(240), label = "pickerRowBoxBg")
                        val boxBorder by animateColorAsState(if (isSelected) MD.Lime else MD.BorderStrong, tween(240), label = "pickerRowBoxBorder")
                        val checkAlpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(240), label = "pickerRowCheckAlpha")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (index > 0) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
                                .limeLeftBar(width = lineWidth)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { selected = exercise }
                                .padding(
                                    start = startPadding,
                                    end = 22.dp,
                                    top = 13.dp,
                                    bottom = 13.dp
                                ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor
                                )
                                exercise.muscle_group?.let { mg ->
                                    Text(
                                        text = mg.uppercase(),
                                        fontSize = 9.sp,
                                        letterSpacing = 1.3.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MD.TextDim,
                                        modifier = Modifier.padding(top = 7.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(boxBg)
                                    .border(2.dp, boxBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MD.OnLime,
                                    modifier = Modifier.size(13.dp).alpha(checkAlpha)
                                )
                            }
                        }
                    }
                }

                val hasSelection = selected != null
                val barColor by animateColorAsState(if (hasSelection) MD.Lime else MD.BorderStrong, tween(300), label = "continueBarColor")
                val barLabelColor by animateColorAsState(if (hasSelection) MD.OnLime else MD.TextDim, tween(300), label = "continueBarLabelColor")
                val barInteractionSource = remember { MutableInteractionSource() }
                val barPressed by barInteractionSource.collectIsPressedAsState()
                val barScale by animateFloatAsState(if (barPressed) 0.985f else 1f, spring(dampingRatio = 0.75f), label = "continueBarScale")
                val barLabel = selected?.let { "${it.name.uppercase()} İLE DEVAM" } ?: "SEÇİLİ EGZERSİZ İLE DEVAM"
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
                        ) { selected?.let(onExerciseSelected) }
                        .padding(vertical = 18.dp, horizontal = 22.dp),
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
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = barLabelColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = barLabelColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
}

/**
 * Kas Grubu Seç — alt sayfa, same pattern as [ExercisePickerDialog].
 */
@Composable
fun MuscleGroupPickerDialog(
    muscleGroups: List<String>,
    exercises: List<com.example.aigymapp.database.Exercises>,
    onMuscleGroupSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    val exerciseCounts = remember(exercises) {
        exercises.groupingBy { it.muscle_group }.eachCount()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            )
    ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MD.Bg)
                    .topRule(2.dp, MD.TextDim)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(androidx.compose.foundation.BorderStroke(2.dp, MD.BorderStrong))
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${muscleGroups.size} GRUP",
                            fontSize = 9.sp,
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MD.TextMuted
                        )
                        Text(
                            text = "Kas grubu seç",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.2).sp,
                            color = MD.TextPrimary,
                            modifier = Modifier.padding(top = 9.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(2.dp, MD.BorderStrong)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = MD.TextMuted, modifier = Modifier.size(20.dp))
                    }
                }

                Column {
                    muscleGroups.forEachIndexed { index, muscleGroup ->
                        val isSelected = selected == muscleGroup
                        val lineWidth by animateDpAsState(if (isSelected) 4.dp else 0.dp, tween(240, easing = FastOutSlowInEasing), label = "pickerRowLine")
                        val startPadding by animateDpAsState(if (isSelected) 18.dp else 22.dp, tween(240, easing = FastOutSlowInEasing), label = "pickerRowPadding")
                        val titleColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextPrimary, tween(240), label = "pickerRowTitleColor")
                        val trailingColor by animateColorAsState(if (isSelected) MD.Lime else MD.TextDim, tween(240), label = "pickerRowTrailingColor")
                        val boxBg by animateColorAsState(if (isSelected) MD.Lime else Color.Transparent, tween(240), label = "pickerRowBoxBg")
                        val boxBorder by animateColorAsState(if (isSelected) MD.Lime else MD.BorderStrong, tween(240), label = "pickerRowBoxBorder")
                        val checkAlpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(240), label = "pickerRowCheckAlpha")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (index > 0) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
                                .limeLeftBar(width = lineWidth)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { selected = muscleGroup }
                                .padding(
                                    start = startPadding,
                                    end = 22.dp,
                                    top = 14.dp,
                                    bottom = 14.dp
                                ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = muscleGroup,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${exerciseCounts[muscleGroup] ?: 0} EGZERSİZ",
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = trailingColor
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(boxBg)
                                    .border(2.dp, boxBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MD.OnLime,
                                    modifier = Modifier.size(13.dp).alpha(checkAlpha)
                                )
                            }
                        }
                    }
                }

                val hasSelection = selected != null
                val barColor by animateColorAsState(if (hasSelection) MD.Lime else MD.BorderStrong, tween(300), label = "continueBarColor")
                val barLabelColor by animateColorAsState(if (hasSelection) MD.OnLime else MD.TextDim, tween(300), label = "continueBarLabelColor")
                val barInteractionSource = remember { MutableInteractionSource() }
                val barPressed by barInteractionSource.collectIsPressedAsState()
                val barScale by animateFloatAsState(if (barPressed) 0.985f else 1f, spring(dampingRatio = 0.75f), label = "continueBarScale")
                val barLabel = selected?.let { "${it.uppercase()} İLE DEVAM" } ?: "DEVAM"
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
                        ) { selected?.let(onMuscleGroupSelected) }
                        .padding(vertical = 18.dp, horizontal = 22.dp),
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
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = barLabelColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = barLabelColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
}
