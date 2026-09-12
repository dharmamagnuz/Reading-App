package com.example.readingtutor.ui.home

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingtutor.audio.AudioService
import com.example.readingtutor.data.ChildProfile
import com.example.readingtutor.data.DayProgressEntity
import com.example.readingtutor.data.ReadingTutorRepository
import com.example.readingtutor.ui.components.KidButton
import com.example.readingtutor.ui.components.SpeakerIconButton
import com.example.readingtutor.ui.parent.ParentGateDialog
import com.example.ui.theme.TutorBackground
import com.example.ui.theme.TutorCardBorder
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary

@Composable
fun HomeScreen(
    repository: ReadingTutorRepository,
    audioService: AudioService,
    onStartDay: (Int) -> Unit,
    onOpenParentZone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val profile by repository.profileFlow.collectAsState(initial = null)
    val dayProgressList by repository.dayProgressFlow.collectAsState(initial = emptyList())

    var showParentGate by remember { mutableStateOf(false) }

    val currentDayNumber = profile?.currentDay ?: 1
    val totalStars = profile?.totalStars ?: 0

    val dayTitles = listOf(
        "Letters Have Sounds",
        "The Vowel Helpers",
        "Putting Sounds Together",
        "First 3-Letter Words",
        "More 3-Letter Words",
        "First Simple Sentences",
        "Practice & Assessment"
    )
    val dayIcons = listOf("🔤", "🅰️", "🔗", "🐱", "🐶", "📖", "🏆")

    if (showParentGate) {
        ParentGateDialog(
            onDismiss = { showParentGate = false },
            onSuccess = {
                showParentGate = false
                onOpenParentZone()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TutorBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, Super Reader! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TutorPrimary
                )
                Text(
                    text = "Let's read new words today",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Star Counter Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalStars",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                // Grown-up zone gate button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, TutorCardBorder),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { showParentGate = true }
                        .testTag("parent_gate_icon_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Parent Dashboard",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Hero Learning Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(28.dp))
                .testTag("home_hero_banner"),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(2.dp, TutorPrimary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TutorPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "DAY $currentDayNumber OF 7",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TutorPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    SpeakerIconButton(
                        onClick = {
                            val title = dayTitles.getOrElse(currentDayNumber - 1) { "Reading lesson" }
                            audioService.playInstruction("Today is Day $currentDayNumber: $title. Let's learn to read!")
                        },
                        size = 38,
                        backgroundColor = TutorSecondary,
                        contentDescription = "Hear intro"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dayIcons.getOrElse(currentDayNumber - 1) { "⭐" },
                    fontSize = 52.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dayTitles.getOrElse(currentDayNumber - 1) { "Day $currentDayNumber" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap below to start today's fun reading lesson!",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(18.dp))

                KidButton(
                    text = "START READING ▶",
                    onClick = {
                        audioService.playChimeSuccess()
                        onStartDay(currentDayNumber)
                    },
                    containerColor = TutorSecondary,
                    testTag = "continue_learning_btn"
                )
            }
        }

        // 7-Day Reading Journey Map
        Text(
            text = "Your 7-Day Reading Adventure 🗺️",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (1..7).forEach { dayNum ->
                val progress = dayProgressList.find { it.dayNumber == dayNum }
                val isUnlocked = progress?.isUnlocked ?: (dayNum == 1)
                val isCompleted = progress?.isCompleted ?: false
                val title = dayTitles.getOrElse(dayNum - 1) { "Day $dayNum" }
                val icon = dayIcons.getOrElse(dayNum - 1) { "⭐" }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(enabled = isUnlocked) {
                            audioService.playChimeTap()
                            onStartDay(dayNum)
                        }
                        .testTag("day_card_$dayNum"),
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isCompleted -> Color(0xFFF0FDF4)
                        dayNum == currentDayNumber -> Color.White
                        isUnlocked -> Color.White
                        else -> Color(0xFFF8FAFC)
                    },
                    shadowElevation = if (dayNum == currentDayNumber) 6.dp else 2.dp,
                    border = BorderStroke(
                        if (dayNum == currentDayNumber) 2.dp else 1.dp,
                        when {
                            isCompleted -> TutorTertiary.copy(alpha = 0.5f)
                            dayNum == currentDayNumber -> TutorPrimary
                            isUnlocked -> TutorCardBorder
                            else -> Color(0xFFE2E8F0)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Day $dayNum: $title",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                                Text(
                                    text = when {
                                        isCompleted -> "Completed ⭐⭐⭐"
                                        dayNum == currentDayNumber -> "Ready to learn!"
                                        isUnlocked -> "Unlocked"
                                        else -> "Locked"
                                    },
                                    fontSize = 12.sp,
                                    color = when {
                                        isCompleted -> TutorTertiary
                                        dayNum == currentDayNumber -> TutorPrimary
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = TutorTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = TutorPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
