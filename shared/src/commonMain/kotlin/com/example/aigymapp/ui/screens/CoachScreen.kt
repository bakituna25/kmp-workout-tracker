package com.example.aigymapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.models.Goal
import com.example.aigymapp.models.GoalType
import com.example.aigymapp.models.GoalWithProgress
import com.example.aigymapp.ui.components.CoachChatInputBar
import com.example.aigymapp.ui.components.CoachHeader
import com.example.aigymapp.ui.components.CoachMessageRow
import com.example.aigymapp.ui.components.CoachProposalCard
import com.example.aigymapp.ui.components.CoachSuggestionRow
import com.example.aigymapp.ui.theme.ModernistDark as MD
import com.example.aigymapp.ui.viewmodels.GoalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material3.Text

private const val ChipPlan = "Bu hafta ne çalışayım?"
private const val ChipProgress = "Bench'te ilerliyor muyum?"
private const val ChipInjury = "Omzum ağrıyor"
private const val ChipForm = "Bench formumu düzelt"
private const val ChipNutrition = "Ne yemeliyim?"
private const val ChipNewGoal = "Yeni hedef öner"

private val AllSuggestionChips = listOf(ChipPlan, ChipProgress, ChipInjury, ChipForm, ChipNutrition, ChipNewGoal)

private const val PlanReply =
    "Bu hafta üst gövdeye ağırlık ver: pazartesi göğüs + triceps, çarşamba sırt + biceps, " +
        "cuma omuz + karın. Geçen hafta bacak günü eksikti, istersen cumartesiye ekleyelim."
private const val FormReply =
    "Bench'te iki noktaya dikkat et: skapulanı sıkıştır ve ayaklarını yere sabitle. Barı " +
        "göğsünün alt ucuna indir, dirseklerini gövdene yakın tut."
private const val NutritionReply =
    "Hedefin güç ve kütle; günlük 2400-2600 kalori ve ~150g protein hedefle. Antrenman " +
        "öncesi karbonhidrat, sonrasında protein ağırlıklı bir öğün iyi olur."
private const val InjuryReplyIntro =
    "Omuz ağrısı varken zorlamayalım. Bu hafta için küçük bir uyarlama öneriyorum:"
private const val InjuryFollowUp = "Tamam, programını buna göre uyarladım. Ağrı 3 günden uzun sürerse bir uzmana görün."
private const val NewGoalIntro = "Son verilerine göre önerim şu:"
private const val NewGoalApprovedFollowUp = "Harika, hedefi panoya ekledim."
private const val NoGoalSuggestionReply = "Şu an için önerebileceğim yeni bir hedef yok — tüm hedef türlerinde zaten aktifsin, harika gidiyorsun!"
private const val FallbackReply = "Bunu not aldım, üstünde düşünüp sana daha net bir öneri getireyim."
private const val ChangeFollowUp = "Tamam, ne değiştirmemi istersin? Yazabilirsin."

private val InjuryProposalLines = listOf(
    "Overhead press bu haftalık programdan çıkarılacak",
    "Shoulder press ağırlığı %20 azaltılacak",
    "Bant ile omuz mobilite çalışması eklenecek"
)

private sealed class CoachChatItem {
    data class Coach(val text: String) : CoachChatItem()
    data class User(val text: String) : CoachChatItem()
    data class Proposal(
        val lines: List<String>,
        val kind: ProposalKind,
        val goalPayload: Goal? = null,
        val approved: Boolean = false
    ) : CoachChatItem()
}

private enum class ProposalKind { InjuryAdaptation, NewGoal }

/**
 * Coach tab (5th bottom-nav slot). Goals live only on the Home screen's
 * Hedefler section — goal selection/editing is not reachable from here. The
 * header's "+ YENİ" starts a fresh chat instead, replaying the same entrance
 * animations as the screen's first load. The chat itself is a local,
 * scripted demo (per the design: "gerçek modele bağlanmadı") — only the
 * "yeni hedef öner" proposal has a real effect, via [GoalViewModel.addGoal].
 */
@Composable
fun CoachScreen(
    goalViewModel: GoalViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        goalViewModel.refreshProgress()
    }

    val goalsWithProgress by goalViewModel.goalsWithProgress.collectAsState()
    val allExercises by goalViewModel.allExercises.collectAsState()

    val chatItems = remember { mutableStateListOf<CoachChatItem>() }
    var usedChips by remember { mutableStateOf(setOf<String>()) }
    var inputText by remember { mutableStateOf("") }
    // Bumped on "+ YENİ" so the key() below remounts the whole screen body,
    // replaying its entrance animations exactly like the first load.
    var chatSessionId by remember { mutableStateOf(0) }

    fun startNewChat() {
        chatItems.clear()
        usedChips = emptySet()
        inputText = ""
        chatSessionId++
    }

    fun sendMessage(text: String, chipTapped: String? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        chatItems.add(CoachChatItem.User(trimmed))

        val matchedChip = chipTapped ?: AllSuggestionChips.firstOrNull { it.equals(trimmed, ignoreCase = true) }
        matchedChip?.let { usedChips = usedChips + it }

        when (matchedChip) {
            ChipPlan -> chatItems.add(CoachChatItem.Coach(PlanReply))
            ChipProgress -> chatItems.add(CoachChatItem.Coach(progressReplyText(goalsWithProgress)))
            ChipInjury -> {
                chatItems.add(CoachChatItem.Coach(InjuryReplyIntro))
                chatItems.add(CoachChatItem.Proposal(lines = InjuryProposalLines, kind = ProposalKind.InjuryAdaptation))
            }
            ChipForm -> chatItems.add(CoachChatItem.Coach(FormReply))
            ChipNutrition -> chatItems.add(CoachChatItem.Coach(NutritionReply))
            ChipNewGoal -> {
                val suggestion = computeGoalSuggestion(goalsWithProgress, allExercises)
                if (suggestion != null) {
                    chatItems.add(CoachChatItem.Coach(NewGoalIntro))
                    chatItems.add(
                        CoachChatItem.Proposal(
                            lines = goalSuggestionLines(suggestion),
                            kind = ProposalKind.NewGoal,
                            goalPayload = suggestion
                        )
                    )
                } else {
                    chatItems.add(CoachChatItem.Coach(NoGoalSuggestionReply))
                }
            }
            else -> chatItems.add(CoachChatItem.Coach(FallbackReply))
        }
    }

    fun approveProposal(index: Int) {
        val item = chatItems.getOrNull(index) as? CoachChatItem.Proposal ?: return
        if (item.approved) return
        chatItems[index] = item.copy(approved = true)
        when (item.kind) {
            ProposalKind.NewGoal -> {
                item.goalPayload?.let { goal -> goalViewModel.addGoal(goal) }
                chatItems.add(CoachChatItem.Coach(NewGoalApprovedFollowUp))
            }
            ProposalKind.InjuryAdaptation -> {
                chatItems.add(CoachChatItem.Coach(InjuryFollowUp))
            }
        }
    }

    fun changeProposal(index: Int) {
        val item = chatItems.getOrNull(index) as? CoachChatItem.Proposal ?: return
        if (item.approved) return
        chatItems.add(CoachChatItem.Coach(ChangeFollowUp))
    }

    val remainingChips = remember(usedChips) { AllSuggestionChips.filterNot { it in usedChips } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MD.Bg)
    ) {
        key(chatSessionId) {
            EntranceAnimated(index = 0) {
                CoachHeader(onNewChatClick = ::startNewChat)
            }

            if (chatItems.isEmpty()) {
                EntranceAnimated(index = 1, modifier = Modifier.weight(1f)) {
                    CoachEmptyPrompt(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, top = 24.dp)
                    )
                }
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(chatItems.size) {
                    if (chatItems.isNotEmpty()) listState.animateScrollToItem(chatItems.size - 1)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(chatItems) { index, item ->
                        when (item) {
                            is CoachChatItem.Coach -> CoachMessageRow(text = item.text, fromCoach = true)
                            is CoachChatItem.User -> CoachMessageRow(text = item.text, fromCoach = false)
                            is CoachChatItem.Proposal -> CoachProposalCard(
                                lines = item.lines,
                                approved = item.approved,
                                onChangeClick = { changeProposal(index) },
                                onApproveClick = { approveProposal(index) }
                            )
                        }
                    }
                }
            }

            EntranceAnimated(index = 2) {
                CoachSuggestionRow(
                    chips = remainingChips,
                    onChipClick = { chip -> sendMessage(chip, chipTapped = chip) }
                )
            }

            EntranceAnimated(index = 3) {
                CoachChatInputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        val text = inputText
                        inputText = ""
                        sendMessage(text)
                    }
                )
            }
        }
    }
}

@Composable
private fun CoachEmptyPrompt(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Neyi\nçözelim?",
            fontSize = 32.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color = MD.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Plan, form, beslenme ya da geçmiş verin. Bir şey değiştireceksem önce sana sorarım.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium,
            color = MD.TextMuted
        )
    }
}

private fun progressReplyText(goalsWithProgress: List<GoalWithProgress>): String {
    val benchGoal = goalsWithProgress.firstOrNull { it.goal.title.contains("bench", ignoreCase = true) }
    if (benchGoal != null) {
        return "Bench hedefinde ${benchGoal.getProgressText()} durumundasın (${benchGoal.getPercentageText()})." +
            " Bu tempoyla hedefe yakında ulaşırsın."
    }
    val anyGoal = goalsWithProgress.firstOrNull()
    if (anyGoal != null) {
        return "${anyGoal.goal.title} hedefinde ${anyGoal.getProgressText()} durumundasın (${anyGoal.getPercentageText()})." +
            " Gayet iyi gidiyorsun."
    }
    return "Henüz karşılaştıracak yeterli veri yok — birkaç antrenman daha kaydedince daha net konuşabiliriz."
}

/**
 * Suggests a real, addable goal from the live goal state — mirrors the
 * taken-exercise gating [com.example.aigymapp.ui.components.AddGoalFlow] uses,
 * so the suggestion never collides with an existing active goal.
 */
private fun computeGoalSuggestion(
    goalsWithProgress: List<GoalWithProgress>,
    allExercises: List<com.example.aigymapp.database.Exercises>
): Goal? {
    val activeGoals = goalsWithProgress.filter { it.goal.isActive }
    val hasMonthlyGoal = activeGoals.any { it.goal.goalType == GoalType.MONTHLY_DAYS }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    if (!hasMonthlyGoal) {
        return Goal(
            title = "18 Gün Aylık Antrenman",
            goalType = GoalType.MONTHLY_DAYS,
            targetValue = 18,
            startDate = today
        )
    }

    val takenExerciseIds = activeGoals
        .filter { it.goal.goalType == GoalType.EXERCISE_COUNT }
        .mapNotNull { it.goal.exerciseId }
        .toSet()
    val pickableExercise = allExercises.firstOrNull { it.id !in takenExerciseIds } ?: return null

    return Goal(
        title = "${pickableExercise.name} - 10 Kere",
        goalType = GoalType.EXERCISE_COUNT,
        targetValue = 10,
        exerciseId = pickableExercise.id,
        startDate = today
    )
}

private fun goalSuggestionLines(goal: Goal): List<String> {
    val unit = when (goal.goalType) {
        GoalType.MONTHLY_DAYS -> "gün"
        GoalType.EXERCISE_COUNT -> "kere"
        GoalType.MUSCLE_GROUP_COUNT -> "seans"
    }
    return listOf(
        "Yeni hedef eklenecek: ${goal.title}",
        "Hedef değeri: ${goal.targetValue} $unit"
    )
}

/**
 * Same entrance motion as Home/Profile's staggered sections (10dp settle,
 * spring damping 0.9, ~340ms alpha tween, 70ms stagger) — kept as its own
 * private copy per this codebase's existing convention rather than shared.
 */
@Composable
private fun EntranceAnimated(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(10f) }

    LaunchedEffect(Unit) {
        delay(index * 70L)
        launch {
            alpha.animateTo(1f, animationSpec = tween(340, easing = FastOutSlowInEasing))
        }
        launch {
            offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    Column(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value.dp.toPx()
        }
    ) {
        content()
    }
}
