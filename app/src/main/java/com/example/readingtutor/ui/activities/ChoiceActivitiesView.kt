package com.example.readingtutor.ui.activities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.readingtutor.ui.components.SpeakerIconButton
import com.example.readingtutor.ui.components.SpokenInstructionBanner
import com.example.readingtutor.ui.components.SuccessOverlay
import com.example.ui.theme.TutorCardBorder
import com.example.ui.theme.TutorCoral
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary

@Composable
fun ChoiceActivitiesView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isResolved by remember(activity.id) { mutableStateOf(false) }
    var showRetry by remember(activity.id) { mutableStateOf(false) }
    var hasRequestedHelp by remember(activity.id) { mutableStateOf(false) }
    var hasMadeMistake by remember(activity.id) { mutableStateOf(false) }
    var selectedWrongOption by remember(activity.id) { mutableStateOf("") }

    // Play target audio when screen loads
    LaunchedEffect(activity.id) {
        when (activity.type) {
            "LISTEN_AND_CHOOSE", "SOUND_TO_PICTURE" -> {
                audioService.playSound(activity.targetSound)
            }
            "LISTEN_AND_FIND" -> {
                audioService.playWord(activity.targetWord)
            }
            "BEGINNING_SOUND" -> {
                audioService.playWord(activity.targetWord)
            }
            "WORD_TO_PICTURE", "PICTURE_TO_WORD" -> {
                audioService.playWord(activity.targetWord)
            }
            else -> {
                if (activity.targetSound.isNotBlank()) {
                    audioService.playSound(activity.targetSound)
                }
            }
        }
    }

    val playQuestionAudio = {
        when (activity.type) {
            "LISTEN_AND_CHOOSE", "SOUND_TO_PICTURE" -> audioService.playSound(activity.targetSound)
            "LISTEN_AND_FIND" -> audioService.playWord(activity.targetWord)
            "BEGINNING_SOUND" -> audioService.playWord(activity.targetWord)
            else -> audioService.playWord(activity.targetWord)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpokenInstructionBanner(
            instruction = activity.instruction.ifBlank { "Listen and choose the right answer." },
            onReplayAudio = {
                hasRequestedHelp = true
                audioService.playInstruction(activity.instruction) {
                    playQuestionAudio()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Center visual / sound prompter card
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If picture is present (BeginningSound, PictureToWord)
                if (activity.emoji.isNotBlank()) {
                    Text(
                        text = activity.emoji,
                        fontSize = 76.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // If word is prompt (WordToPicture)
                if (activity.type == "WORD_TO_PICTURE") {
                    Text(
                        text = activity.targetWord,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TutorPrimary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Speaker Tap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SpeakerIconButton(
                        onClick = {
                            hasRequestedHelp = true
                            playQuestionAudio()
                        },
                        size = 54,
                        backgroundColor = TutorSecondary,
                        contentDescription = "Hear Question Sound",
                        testTag = "choice_prompt_speaker_btn"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tap to listen 🔊",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        GentleRetryBanner(
            visible = showRetry,
            onListenAgain = {
                playQuestionAudio()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option Cards: can be picture options or text/letter options
        if (activity.pictureOptions.isNotEmpty()) {
            // Picture Options Layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activity.pictureOptions.forEach { option ->
                    val isWrong = selectedWrongOption == option.word
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(enabled = !isResolved) {
                                if (option.word.equals(activity.correctAnswer, ignoreCase = true)) {
                                    isResolved = true
                                    showRetry = false
                                    audioService.playChimeSuccess()
                                    val result = when {
                                        hasMadeMistake -> AttemptResult.RETRY
                                        hasRequestedHelp -> AttemptResult.ASSISTED
                                        else -> AttemptResult.INDEPENDENT
                                    }
                                    onAttemptResolved(result)
                                } else {
                                    hasMadeMistake = true
                                    showRetry = true
                                    selectedWrongOption = option.word
                                    audioService.playChimeGentleRetry()
                                    playQuestionAudio()
                                }
                            }
                            .testTag("pic_option_${option.word.lowercase()}"),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isWrong) Color(0xFFFEF2F2) else Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(
                            2.dp,
                            if (isWrong) TutorCoral else TutorCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = option.emoji, fontSize = 40.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option.word,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            // Text / Letter Options Layout (Grid-like or Cards)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activity.options.forEach { option ->
                    val isWrong = selectedWrongOption == option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(enabled = !isResolved) {
                                if (option.equals(activity.correctAnswer, ignoreCase = true)) {
                                    isResolved = true
                                    showRetry = false
                                    audioService.playChimeSuccess()
                                    val result = when {
                                        hasMadeMistake -> AttemptResult.RETRY
                                        hasRequestedHelp -> AttemptResult.ASSISTED
                                        else -> AttemptResult.INDEPENDENT
                                    }
                                    onAttemptResolved(result)
                                } else {
                                    hasMadeMistake = true
                                    showRetry = true
                                    selectedWrongOption = option
                                    audioService.playChimeGentleRetry()
                                    playQuestionAudio()
                                }
                            }
                            .testTag("option_${option.lowercase()}"),
                        shape = RoundedCornerShape(24.dp),
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
                                fontSize = if (option.length <= 2) 36.sp else 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWrong) TutorCoral else TutorPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SuccessOverlay(
            visible = isResolved,
            title = "That's it! Well done! ⭐",
            subtitle = "You found the right match!",
            onContinue = onNext
        )
    }
}
