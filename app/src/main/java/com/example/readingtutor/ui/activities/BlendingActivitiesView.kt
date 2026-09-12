package com.example.readingtutor.ui.activities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun BlendingActivitiesView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    when (activity.type) {
        "BLENDING" -> {
            SimpleBlendingView(
                activity = activity,
                audioService = audioService,
                onAttemptResolved = onAttemptResolved,
                onNext = onNext,
                modifier = modifier
            )
        }
        "FINGER_SLIDE_BLENDING" -> {
            FingerSlideBlendingView(
                activity = activity,
                audioService = audioService,
                onAttemptResolved = onAttemptResolved,
                onNext = onNext,
                modifier = modifier
            )
        }
        "MISSING_LETTER" -> {
            MissingLetterView(
                activity = activity,
                audioService = audioService,
                onAttemptResolved = onAttemptResolved,
                onNext = onNext,
                modifier = modifier
            )
        }
        else -> {
            Text("Activity not supported: ${activity.type}")
        }
    }
}

@Composable
private fun SimpleBlendingView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isBlended by remember(activity.id) { mutableStateOf(false) }
    var activeSoundIndex by remember(activity.id) { mutableIntStateOf(-1) }
    var isCompleted by remember(activity.id) { mutableStateOf(false) }

    val letterSpacingAnim by animateDpAsState(
        targetValue = if (isBlended) 4.dp else 40.dp,
        animationSpec = tween(durationMillis = 600),
        label = "letterSpacing"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpokenInstructionBanner(
            instruction = activity.instruction.ifBlank { "Tap each letter sound, then put them together!" },
            onReplayAudio = {
                audioService.playInstruction(activity.instruction)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Blending Canvas Area
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
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Individual or Blended Letters
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activity.letters.forEachIndexed { index, letter ->
                        val sound = activity.sounds.getOrElse(index) { letter }
                        val isHighlighted = activeSoundIndex == index

                        Surface(
                            modifier = Modifier
                                .size(if (isBlended) 70.dp else 84.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    activeSoundIndex = index
                                    audioService.playSound(sound)
                                }
                                .testTag("blend_letter_$index"),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isHighlighted) Color(0xFFFEF3C7) else Color(0xFFEEF2FF),
                            border = BorderStroke(
                                2.dp,
                                if (isHighlighted) TutorSecondary else TutorPrimary.copy(alpha = 0.3f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isHighlighted) TutorSecondary else TutorPrimary
                                )
                            }
                        }

                        if (index < activity.letters.size - 1) {
                            Spacer(modifier = Modifier.width(letterSpacingAnim))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isBlended) {
                    KidButton(
                        text = "Slide Together 🔗",
                        onClick = {
                            isBlended = true
                            activeSoundIndex = -1
                            audioService.playChimeSuccess()
                            audioService.playWord(activity.blendedWord)
                            isCompleted = true
                            onAttemptResolved(AttemptResult.INDEPENDENT)
                        },
                        containerColor = TutorSecondary,
                        testTag = "slide_together_btn"
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activity.blendedWord.uppercase(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = TutorTertiary,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SpeakerIconButton(
                            onClick = { audioService.playWord(activity.blendedWord) },
                            size = 52,
                            backgroundColor = TutorTertiary,
                            contentDescription = "Hear word"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SuccessOverlay(
            visible = isCompleted,
            title = "Blended: ${activity.blendedWord.uppercase()}! ⭐",
            subtitle = "You put the sounds together to make a word!",
            onContinue = onNext
        )
    }
}

@Composable
private fun FingerSlideBlendingView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var sliderPos by remember(activity.id) { mutableFloatStateOf(0f) }
    var lastTriggeredStage by remember(activity.id) { mutableIntStateOf(-1) }
    var isWordRevealed by remember(activity.id) { mutableStateOf(false) }
    var isResolved by remember(activity.id) { mutableStateOf(false) }

    val numLetters = activity.letters.size
    val stepSize = 1f / numLetters.toFloat()

    // Handle slider motion across letters
    LaunchedEffect(sliderPos) {
        val currentStage = when {
            sliderPos < 0.2f -> 0
            sliderPos < 0.65f -> if (numLetters >= 3) 1 else 0
            sliderPos < 0.95f -> if (numLetters >= 3) 2 else 1
            else -> numLetters
        }

        if (currentStage != lastTriggeredStage && currentStage in 0 until numLetters) {
            lastTriggeredStage = currentStage
            val sound = activity.sounds.getOrElse(currentStage) { activity.letters[currentStage] }
            audioService.playChimeSlideTone(currentStage)
            audioService.playSound(sound)
        }

        // Reached end of slide track!
        if (sliderPos >= 0.95f && !isWordRevealed) {
            isWordRevealed = true
            isResolved = true
            audioService.playChimeSuccess()
            delay(250)
            audioService.playWord(activity.blendedWord)
            onAttemptResolved(AttemptResult.INDEPENDENT)
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
            instruction = activity.instruction.ifBlank { "Slide your finger to blend the sounds!" },
            onReplayAudio = {
                audioService.playInstruction(activity.instruction)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                // If word is revealed, show cute emoji illustration!
                if (isWordRevealed && activity.emoji.isNotBlank()) {
                    Text(
                        text = activity.emoji,
                        fontSize = 72.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Letters on track
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activity.letters.forEachIndexed { index, letter ->
                        val isActive = when (index) {
                            0 -> sliderPos >= 0f
                            1 -> sliderPos >= (if (numLetters >= 3) 0.35f else 0.8f)
                            2 -> sliderPos >= 0.75f
                            else -> false
                        }

                        Surface(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                2.dp,
                                if (isActive) TutorSecondary else TutorCardBorder
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isActive) TutorSecondary else TutorPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Track Visual Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Slide finger",
                        tint = TutorSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isWordRevealed) "Word Blended!" else "Slide across letters 👉",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWordRevealed) TutorTertiary else TutorSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Slider Track
                Slider(
                    value = sliderPos,
                    onValueChange = { newValue ->
                        sliderPos = newValue
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .testTag("finger_slide_track"),
                    colors = SliderDefaults.colors(
                        thumbColor = TutorSecondary,
                        activeTrackColor = TutorSecondary,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auto demonstration button for children who want assistance
                if (!isWordRevealed) {
                    KidButton(
                        text = "Show Me ▶",
                        onClick = {
                            scope.launch {
                                sliderPos = 0.1f
                                delay(350)
                                sliderPos = 0.5f
                                delay(350)
                                sliderPos = 1.0f
                            }
                        },
                        containerColor = TutorPrimary,
                        testTag = "auto_slide_demo_btn"
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activity.blendedWord.uppercase(),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = TutorTertiary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        SpeakerIconButton(
                            onClick = { audioService.playWord(activity.blendedWord) },
                            size = 50,
                            backgroundColor = TutorTertiary,
                            contentDescription = "Hear full word"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SuccessOverlay(
            visible = isResolved,
            title = "Read: ${activity.blendedWord.uppercase()}! 🎉",
            subtitle = "You slid the sounds into a complete word!",
            onContinue = onNext
        )
    }
}

@Composable
private fun MissingLetterView(
    activity: ActivityItem,
    audioService: AudioService,
    onAttemptResolved: (AttemptResult) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedLetter by remember(activity.id) { mutableStateOf<String?>(null) }
    var isResolved by remember(activity.id) { mutableStateOf(false) }
    var showRetry by remember(activity.id) { mutableStateOf(false) }
    var hasMadeMistake by remember(activity.id) { mutableStateOf(false) }
    var wrongOptionSelected by remember(activity.id) { mutableStateOf("") }

    // Play word sound initially
    LaunchedEffect(activity.id) {
        audioService.playWord(activity.targetWord)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpokenInstructionBanner(
            instruction = activity.instruction.ifBlank { "Which letter is missing?" },
            onReplayAudio = {
                audioService.playInstruction(activity.instruction) {
                    audioService.playWord(activity.targetWord)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center Slot Card
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
                // Word Emoji
                if (activity.emoji.isNotBlank()) {
                    Text(text = activity.emoji, fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Word with missing slot: [ D ] [ ? ] [ G ]
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prefix letter (e.g. 'D')
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(2.dp, TutorPrimary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activity.prefix,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = TutorPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Missing Slot (e.g. blank or filled)
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("missing_letter_slot"),
                        shape = RoundedCornerShape(18.dp),
                        color = if (selectedLetter != null) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                        border = BorderStroke(
                            2.dp,
                            if (selectedLetter != null) TutorTertiary else TutorSecondary
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = selectedLetter ?: "?",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedLetter != null) TutorTertiary else TutorSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Suffix letter (e.g. 'G')
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(2.dp, TutorPrimary.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activity.suffix,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = TutorPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SpeakerIconButton(
                    onClick = { audioService.playWord(activity.targetWord) },
                    size = 50,
                    backgroundColor = TutorSecondary,
                    contentDescription = "Hear full word"
                )
            }
        }

        GentleRetryBanner(
            visible = showRetry,
            onListenAgain = {
                audioService.playWord(activity.targetWord)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Vowel choices
        Text(
            text = "Choose the missing vowel:",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            activity.options.forEach { option ->
                val isWrong = wrongOptionSelected == option
                Surface(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(enabled = !isResolved) {
                            if (option.equals(activity.correctAnswer, ignoreCase = true)) {
                                selectedLetter = option
                                isResolved = true
                                showRetry = false
                                audioService.playChimeSuccess()
                                audioService.playWord(activity.targetWord)
                                onAttemptResolved(
                                    if (hasMadeMistake) AttemptResult.RETRY else AttemptResult.INDEPENDENT
                                )
                            } else {
                                hasMadeMistake = true
                                showRetry = true
                                wrongOptionSelected = option
                                audioService.playChimeGentleRetry()
                                audioService.playWord(activity.targetWord)
                            }
                        }
                        .testTag("missing_opt_${option.lowercase()}"),
                    shape = RoundedCornerShape(22.dp),
                    color = if (isWrong) Color(0xFFFEF2F2) else Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(
                        2.dp,
                        if (isWrong) TutorCoral else TutorPrimary.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWrong) TutorCoral else TutorPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SuccessOverlay(
            visible = isResolved,
            title = "Completed: ${activity.targetWord.uppercase()}! 🌟",
            subtitle = "You found the right missing vowel!",
            onContinue = onNext
        )
    }
}
