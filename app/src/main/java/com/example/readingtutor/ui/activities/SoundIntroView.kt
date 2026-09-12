package com.example.readingtutor.ui.activities

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingtutor.audio.AudioService
import com.example.readingtutor.models.ActivityItem
import com.example.readingtutor.ui.components.KidButton
import com.example.readingtutor.ui.components.SpeakerIconButton
import com.example.readingtutor.ui.components.SpokenInstructionBanner
import com.example.ui.theme.TutorCardBorder
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary

@Composable
fun SoundIntroView(
    activity: ActivityItem,
    audioService: AudioService,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Speak letter sound on entry
    LaunchedEffect(activity.id) {
        audioService.playSound(activity.sound.ifBlank { activity.letter })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpokenInstructionBanner(
            instruction = activity.instruction.ifBlank { "Listen to the letter sound." },
            onReplayAudio = {
                audioService.playInstruction(activity.instruction) {
                    audioService.playSound(activity.sound.ifBlank { activity.letter })
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Letter Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("sound_intro_main_card"),
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
                // Large Letter Display: e.g. "M m"
                Text(
                    text = "${activity.letter} ${activity.letter.lowercase()}",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TutorPrimary,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Speaker Tap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SpeakerIconButton(
                        onClick = {
                            audioService.playSound(activity.sound.ifBlank { activity.letter })
                        },
                        size = 56,
                        backgroundColor = TutorSecondary,
                        contentDescription = "Hear Sound"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tap to hear sound",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Example Words Section
        if (activity.examples.isNotEmpty()) {
            Text(
                text = "Words that start with this sound:",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activity.examples.forEach { example ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                audioService.playWord(example.word)
                            }
                            .testTag("example_item_${example.word.lowercase()}"),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, TutorCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = example.emoji,
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = example.word,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Starts with ${activity.letter}",
                                        fontSize = 14.sp,
                                        color = TutorPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            SpeakerIconButton(
                                onClick = { audioService.playWord(example.word) },
                                size = 42,
                                backgroundColor = TutorTertiary,
                                contentDescription = "Hear ${example.word}"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        KidButton(
            text = "I Got It! Next ▶",
            onClick = {
                audioService.playChimeSuccess()
                onComplete()
            },
            containerColor = TutorTertiary,
            testTag = "sound_intro_continue_btn"
        )
    }
}
