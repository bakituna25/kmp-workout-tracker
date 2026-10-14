package com.example.aigymapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.ui.theme.ModernistDark as MD

/**
 * Coach tab header — "KOÇ" eyebrow + "Bugün" title, with a bordered "+ YENİ"
 * button that starts a fresh chat (goal selection lives only on Home now).
 */
@Composable
fun CoachHeader(onNewChatClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = "KOÇ",
                fontSize = 10.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bugün",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                color = MD.TextPrimary
            )
        }

        Row(
            modifier = Modifier
                .border(2.dp, MD.BorderStrong)
                .clickable(onClick = onNewChatClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MD.TextPrimary, modifier = Modifier.size(14.dp))
            Text(
                text = "YENİ",
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextPrimary
            )
        }
    }
}

/**
 * A single chat row — no bubbles. Coach messages sit flush left with a 3dp
 * lime rule; the user's own messages sit flush right on a flat dark block.
 */
@Composable
fun CoachMessageRow(text: String, fromCoach: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (fromCoach) Arrangement.Start else Arrangement.End
    ) {
        if (fromCoach) {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MD.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .leftRule(3.dp, MD.Lime)
                    .padding(start = 14.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MD.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .background(MD.BorderStrong)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

/**
 * Proposal card — shown before the coach changes anything. Lists what it
 * will do line by line with DEĞİŞTİR/ONAYLA while pending; once approved the
 * buttons are replaced by a one-line confirmation strip and the border
 * turns lime, staying that way in the transcript.
 */
@Composable
fun CoachProposalCard(
    lines: List<String>,
    approved: Boolean,
    onChangeClick: () -> Unit,
    onApproveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(if (approved) MD.Lime else MD.BorderStrong, tween(400), label = "proposalBorder")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor)
            .padding(18.dp)
    ) {
        Text(
            text = "ÖNERİ",
            fontSize = 9.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (approved) MD.Lime else MD.TextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))
        lines.forEachIndexed { index, line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index > 0) Modifier.padding(top = 8.dp) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(5.dp).background(MD.Lime))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = line, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MD.TextPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!approved) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, MD.BorderStrong)
                        .clickable(onClick = onChangeClick)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DEĞİŞTİR",
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MD.Lime)
                        .clickable(onClick = onApproveClick)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ONAYLA",
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.OnLime
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MD.Lime, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ONAYLANDI",
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime
                )
            }
        }
    }
}

/**
 * Horizontally scrolling row of suggestion chips — the coach's six jobs.
 * A tapped chip is expected to be dropped from [chips] by the caller so the
 * same question is never offered twice in a session.
 */
@Composable
fun CoachSuggestionRow(chips: List<String>, onChipClick: (String) -> Unit, modifier: Modifier = Modifier) {
    if (chips.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            Box(
                modifier = Modifier
                    .border(1.5.dp, MD.BorderStrong)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onChipClick(chip) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = chip,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MD.TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * "Koça sor" text field + lime send button, dimmed while empty — same
 * enabled/disabled color language as [ContinueBar] in GoalComponents.kt.
 */
@Composable
fun CoachChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canSend = value.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .topRule(2.dp, MD.BorderStrong)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(text = "Koça sor", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MD.TextDim)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = MD.TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(MD.Lime),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (canSend) MD.Lime else MD.BorderStrong)
                .clickable(enabled = canSend, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Gönder",
                tint = if (canSend) MD.OnLime else MD.TextDim,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
