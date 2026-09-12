package com.example.readingtutor.ui.lesson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingtutor.audio.AudioService
import com.example.readingtutor.curriculum.CurriculumRepository
import com.example.readingtutor.data.DayProgressEntity
import com.example.readingtutor.data.ReadingTutorRepository
import com.example.readingtutor.models.ActivityItem
import com.example.readingtutor.models.AttemptResult
import com.example.readingtutor.models.DayPlan
import com.example.readingtutor.ui.activities.BlendingActivitiesView
import com.example.readingtutor.ui.activities.ChoiceActivitiesView
import com.example.readingtutor.ui.activities.SentenceAndAssessmentView
import com.example.readingtutor.ui.activities.SoundIntroView
import com.example.readingtutor.ui.components.KidButton
import com.example.ui.theme.TutorBackground
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    dayNumber: Int,
    repository: ReadingTutorRepository,
    audioService: AudioService,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var dayPlan by remember(dayNumber) { mutableStateOf<DayPlan?>(null) }
    var currentActivityIndex by remember(dayNumber) { mutableIntStateOf(0) }
    var isDayFinished by remember(dayNumber) { mutableStateOf(false) }

    // Load curriculum and resume position from database
    LaunchedEffect(dayNumber) {
        val plan = CurriculumRepository.loadDayPlan(context, dayNumber)
        dayPlan = plan
        val progress = repository.getDayProgress(dayNumber)
        if (progress != null && progress.lastActivityIndex in 0 until plan.activities.size) {
            currentActivityIndex = progress.lastActivityIndex
        } else {
            currentActivityIndex = 0
        }
    }

    if (dayPlan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading lesson...", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        return
    }

    val plan = dayPlan!!
    val totalActivities = plan.activities.size
    val currentActivity = if (currentActivityIndex in 0 until totalActivities) {
        plan.activities[currentActivityIndex]
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Day $dayNumber: ${plan.title}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TutorPrimary
                        )
                        if (totalActivities > 0) {
                            Text(
                                text = "Activity ${currentActivityIndex + 1} of $totalActivities",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.testTag("lesson_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Return Home",
                            tint = TutorPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Stars",
                                tint = TutorSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentActivityIndex + 1}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TutorBackground
                )
            )
        },
        containerColor = TutorBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = {
                    if (totalActivities > 0) (currentActivityIndex + 1).toFloat() / totalActivities.toFloat()
                    else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = TutorSecondary,
                trackColor = Color(0xFFE2E8F0)
            )

            if (isDayFinished) {
                DayCompletionCelebration(
                    dayNumber = dayNumber,
                    dayTitle = plan.title,
                    totalActivities = totalActivities,
                    onReturnHome = onBackToHome
                )
            } else if (currentActivity != null) {
                val onAttemptResolved: (AttemptResult) -> Unit = { result ->
                    scope.launch {
                        val target = when {
                            currentActivity.targetWord.isNotBlank() -> currentActivity.targetWord
                            currentActivity.targetSound.isNotBlank() -> currentActivity.targetSound
                            currentActivity.blendedWord.isNotBlank() -> currentActivity.blendedWord
                            else -> currentActivity.letter
                        }
                        repository.recordAttempt(
                            dayNumber = dayNumber,
                            activityId = currentActivity.id,
                            activityType = currentActivity.type,
                            targetItem = target,
                            result = result
                        )
                    }
                }

                val onNextActivity: () -> Unit = {
                    scope.launch {
                        repository.updateActivityProgress(
                            dayNumber = dayNumber,
                            activityIndex = currentActivityIndex,
                            totalActivities = totalActivities
                        )
                        if (currentActivityIndex < totalActivities - 1) {
                            currentActivityIndex++
                        } else {
                            isDayFinished = true
                            audioService.playChimeSuccess()
                        }
                    }
                }

                when (currentActivity.type) {
                    "SOUND_INTRO" -> {
                        SoundIntroView(
                            activity = currentActivity,
                            audioService = audioService,
                            onComplete = onNextActivity,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "LISTEN_AND_CHOOSE", "BEGINNING_SOUND", "SOUND_TO_PICTURE",
                    "PICTURE_TO_WORD", "WORD_TO_PICTURE", "LISTEN_AND_FIND" -> {
                        ChoiceActivitiesView(
                            activity = currentActivity,
                            audioService = audioService,
                            onAttemptResolved = onAttemptResolved,
                            onNext = onNextActivity,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "BLENDING", "FINGER_SLIDE_BLENDING", "MISSING_LETTER" -> {
                        BlendingActivitiesView(
                            activity = currentActivity,
                            audioService = audioService,
                            onAttemptResolved = onAttemptResolved,
                            onNext = onNextActivity,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "SENTENCE_READING", "ASSESSMENT" -> {
                        SentenceAndAssessmentView(
                            activity = currentActivity,
                            audioService = audioService,
                            onAttemptResolved = onAttemptResolved,
                            onNext = onNextActivity,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Activity ready.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCompletionCelebration(
    dayNumber: Int,
    dayTitle: String,
    totalActivities: Int,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            border = BorderStroke(2.dp, TutorTertiary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏆", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Day $dayNumber Completed!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TutorTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Great job finishing $dayTitle!",
                    fontSize = 18.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Text(
                            text = "⭐",
                            fontSize = 36.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                KidButton(
                    text = "Back to Home 🏠",
                    onClick = onReturnHome,
                    containerColor = TutorTertiary,
                    testTag = "finish_day_home_btn"
                )
            }
        }
    }
}
