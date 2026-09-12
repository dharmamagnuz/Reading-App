package com.example.readingtutor.ui.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readingtutor.data.DayProgressEntity
import com.example.readingtutor.data.ReadingTutorRepository
import com.example.readingtutor.models.ParentReportData
import com.example.ui.theme.TutorBackground
import com.example.ui.theme.TutorCardBorder
import com.example.ui.theme.TutorCoral
import com.example.ui.theme.TutorPrimary
import com.example.ui.theme.TutorSecondary
import com.example.ui.theme.TutorTertiary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParentDashboardScreen(
    repository: ReadingTutorRepository,
    onBack: () -> Unit,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val dayProgressList by repository.dayProgressFlow.collectAsState(initial = emptyList())
    var reportData by remember { mutableStateOf<ParentReportData?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val refreshReport = {
        scope.launch {
            reportData = repository.getParentReport()
        }
    }

    LaunchedEffect(Unit) {
        refreshReport()
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset all days back to Day 1 and clear the learning history.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.resetAllProgress()
                            showResetDialog = false
                            refreshReport()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TutorCoral)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Parent Dashboard & Reports",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("parent_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TutorPrimary
                        )
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Stats Grid
            val report = reportData
            if (report != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Time Spent",
                        value = "${report.totalTimeMinutes}m",
                        subtitle = "total reading",
                        color = TutorPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Completed",
                        value = "${report.totalActivitiesCompleted}",
                        subtitle = "activities",
                        color = TutorTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Independence vs Assistance Breakdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TutorCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Reading Independence Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatusPill(
                                label = "Independent",
                                count = report.independentCount,
                                color = TutorTertiary
                            )
                            StatusPill(
                                label = "Assisted",
                                count = report.assistedCount,
                                color = TutorSecondary
                            )
                            StatusPill(
                                label = "Retried",
                                count = report.needsPracticeCount,
                                color = TutorCoral
                            )
                        }
                    }
                }

                // Pedagogical Advice Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "💡", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Next Learning Step",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TutorPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.actionAdvice,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Sounds Mastery Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TutorCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Phonics & Sounds Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Strong Sounds:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TutorTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.strongSounds.forEach { sound ->
                                TagChip(text = sound, color = TutorTertiary)
                            }
                        }

                        if (report.needsPracticeSounds.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Needs Review:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TutorSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                report.needsPracticeSounds.forEach { sound ->
                                    TagChip(text = sound, color = TutorSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // 7-Day Curriculum Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TutorCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Curriculum Course",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    repository.unlockAllDays()
                                    refreshReport()
                                }
                            }
                        ) {
                            Text("Unlock All", fontSize = 14.sp, color = TutorPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    dayProgressList.forEach { progress ->
                        DayRowItem(
                            progress = progress,
                            onSelect = { onSelectDay(progress.dayNumber) }
                        )
                    }
                }
            }

            // Reset Button
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth().testTag("reset_progress_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TutorCoral),
                border = BorderStroke(1.dp, TutorCoral.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Progress", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TutorCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun StatusPill(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DayRowItem(
    progress: DayProgressEntity,
    onSelect: () -> Unit
) {
    val dayTitles = listOf(
        "Letters Have Sounds",
        "The Vowel Helpers",
        "Putting Sounds Together",
        "First 3-Letter Words",
        "More 3-Letter Words",
        "First Simple Sentences",
        "Practice & Assessment"
    )
    val title = dayTitles.getOrElse(progress.dayNumber - 1) { "Day ${progress.dayNumber}" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = progress.isUnlocked) { onSelect() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when {
                    progress.isCompleted -> Icons.Default.CheckCircle
                    progress.isUnlocked -> Icons.Default.LockOpen
                    else -> Icons.Default.Lock
                },
                contentDescription = null,
                tint = when {
                    progress.isCompleted -> TutorTertiary
                    progress.isUnlocked -> TutorPrimary
                    else -> Color(0xFF94A3B8)
                },
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Day ${progress.dayNumber}: $title",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (progress.isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8)
                )
                if (progress.completedActivitiesCount > 0) {
                    Text(
                        text = "${progress.completedActivitiesCount} activities done",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        if (progress.isUnlocked) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TutorPrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Practice",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TutorPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
