package com.example.readingtutor.ui.activities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingtutor.audio.AudioService
import com.example.readingtutor.models.ActivityItem
import com.example.readingtutor.models.AttemptResult
import com.example.readingtutor.ui.components.GentleRetryBanner
import com.example.readingtutor.ui.components.KidButton
import com.example.readingtutor.ui.components.SpeakerIconButton
import com.example.readingtutor.ui.components.SpokenInstructionBanner
import com.example.readingtutor.ui.components.SuccessOverlay
import com.example.ui.theme.TutorCardBorder
import com.example.ui.theme.TutorCoral
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SentenceAndAssessmentView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (activity.type) {
        "SENTENCE_READING" -> {
            SentenceReadingScreen(
                activity = activity,
                audioService = audioService,
                onAttemptResolved = onAttemptResolved,
                onNext = onNext,
                modifier = modifier
            )
        }
        "ASSESSMENT" -> {
            AssessmentScreen(
                activity = activity,
                audioService = audioService,
                onAttemptResolved = onAttemptResolved,
                onNext = onNext,
                modifier = modifier
            )
        }
        else -> {
            Text("Unsupported screen type: ${activity.type}")
        }
    }
}

@Composable
private fun SentenceReadingScreen(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var activeWordIndex by remember(activity.id) { mutableIntStateOf(-1) }
    var isReadComplete by remember(activity.id) { mutableStateOf(false) }

    val words = if (activity.words.isNotEmpty()) activity.words else activity.sentence.split(" ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpokenInstructionBanner(
            instruction = activity.instruction.ifBlank { "Tap each word to read, or hear the full sentence!" },
            onReplayAudio = {
                audioService.playInstruction(activity.instruction) {
                    audioService.playSentence(activity.sentence)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sentence Presentation Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(2.dp, TutorPrimary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activity.emoji.isNotBlank()) {
                    Text(text = activity.emoji, fontSize = 76.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Interactive Words Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    words.forEachIndexed { index, word ->
                        val isCurrent = activeWordIndex == index
                        val cleanWord = word.replace(Regex("[^a-zA-Z]"), "")
                        val isHelper = activity.helperWords.any { it.equals(cleanWord, ignoreCase = true) }

                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    activeWordIndex = index
                                    audioService.playWord(cleanWord)
                                }
                                .testTag("sentence_word_$index"),
                            shape = RoundedCornerShape(16.dp),
                            color = when {
                                isCurrent -> Color(0xFFFEF3C7)
                                isHelper -> Color(0xFFEFF6FF)
                                else -> Color(0xFFF1F5F9)
                            },
                            border = BorderStroke(
                                2.dp,
                                when {
                                    isCurrent -> TutorSecondary
                                    isHelper -> TutorPrimary.copy(alpha = 0.4f)
                                    else -> TutorCardBorder
                                }
                            )
                        ) {
                            Text(
                                text = word,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isCurrent -> TutorSecondary
                                    isHelper -> TutorPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Listen to full sentence
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SpeakerIconButton(
                        onClick = {
                            scope.launch {
                                for (i in words.indices) {
                                    activeWordIndex = i
                                    val clean = words[i].replace(Regex("[^a-zA-Z]"), "")
                                    audioService.playWord(clean)
                                    delay(450)
                                }
                                activeWordIndex = -1
                                delay(200)
                                audioService.playSentence(activity.sentence)
                            }
                        },
                        size = 52,
                        backgroundColor = TutorSecondary,
                        contentDescription = "Read sentence sequentially",
                        testTag = "read_sentence_btn"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Read with Me 📖",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        KidButton(
            text = "I Can Read This! ⭐",
            onClick = {
                isReadComplete = true
                audioService.playChimeSuccess()
                onAttemptResolved(AttemptResult.INDEPENDENT)
            },
            containerColor = TutorTertiary,
            testTag = "sentence_read_done_btn"
        )

        Spacer(modifier = Modifier.height(16.dp))

        SuccessOverlay(
            visible = isReadComplete,
            title = "Wonderful Sentence Reading! 🌟",
            subtitle = "You are reading whole sentences by yourself!",
            onContinue = onNext
        )
    }
}

@Composable
private fun AssessmentScreen(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isResolved by remember(activity.id) { mutableStateOf(false) }
    var showRetry by remember(activity.id) { mutableStateOf(false) }
    var requestedSoundHelp by remember(activity.id) { mutableStateOf(false) }
    var madeWrongAttempt by remember(activity.id) { mutableStateOf(false) }
    var selectedWrongOption by remember(activity.id) { mutableStateOf("") }

    val playAssessmentSound = {
        when {
            activity.targetSound.isNotBlank() -> audioService.playSound(activity.targetSound)
            activity.targetWord.isNotBlank() -> audioService.playWord(activity.targetWord)
            else -> audioService.playInstruction(activity.instruction)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skill Category Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TutorPrimary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, TutorPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TutorSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activity.skillName.ifBlank { "Reading Assessment" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TutorPrimary
                )
            }
        }

        SpokenInstructionBanner(
            instruction = activity.instruction,
            onReplayAudio = {
                audioService.playInstruction(activity.instruction)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Question Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(2.dp, TutorPrimary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activity.emoji.isNotBlank()) {
                    Text(text = activity.emoji, fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // If missing slot prompt
                if (activity.prefix.isNotBlank()) {
                    Text(
                        text = "${activity.prefix}  _  ${activity.suffix}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TutorPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Audio Help Button (clearly counts as ASSISTED if tapped)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SpeakerIconButton(
                        onClick = {
                            requestedSoundHelp = true
                            playAssessmentSound()
                        },
                        size = 52,
                        backgroundColor = TutorSecondary,
                        contentDescription = "Hear Sound Hint",
                        testTag = "assessment_hint_btn"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Hear Sound 🔊",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        GentleRetryBanner(
            visible = showRetry,
            onListenAgain = {
                playAssessmentSound()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Options List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activity.options.forEach { option ->
                val isWrong = selectedWrongOption == option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(enabled = !isResolved) {
                            if (option.equals(activity.correctAnswer, ignoreCase = true)) {
                                isResolved = true
                                showRetry = false
                                audioService.playChimeSuccess()

                                val outcome = when {
                                    madeWrongAttempt -> AttemptResult.RETRY
                                    requestedSoundHelp -> AttemptResult.ASSISTED
                                    else -> AttemptResult.INDEPENDENT
                                }
                                onAttemptResolved(outcome)
                            } else {
                                madeWrongAttempt = true
                                showRetry = true
                                selectedWrongOption = option
                                audioService.playChimeGentleRetry()
                                playAssessmentSound()
                            }
                        }
                        .testTag("assess_opt_${option.lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(22.dp),
                    color = if (isWrong) Color(0xFFFEF2F2) else Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(
                        2.dp,
                        if (isWrong) TutorCoral else TutorCardBorder
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = if (option.length <= 2) 34.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWrong) TutorCoral else TutorPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SuccessOverlay(
            visible = isResolved,
            title = "Checked! Well Done! ⭐",
            subtitle = if (requestedSoundHelp) "Great job with sound hint!" else "Independent reading power!",
            onContinue = onNext
        )
    }
}
