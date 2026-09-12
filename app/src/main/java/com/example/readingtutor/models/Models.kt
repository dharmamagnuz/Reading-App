package com.example.readingtutor.models

enum class AttemptResult {
    INDEPENDENT,
    ASSISTED,
    RETRY
}

enum class DayStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED
}

data class DaySummary(
    val dayNumber: Int,
    val title: String,
    val summary: String,
    val icon: String
)

data class CourseInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val totalDays: Int,
    val days: List<DaySummary>
)

data class ExampleItem(
    val word: String,
    val emoji: String,
    val sound: String
)

data class PictureOption(
    val word: String,
    val emoji: String
)

data class ActivityItem(
    val id: String,
    val type: String,
    val instruction: String,
    val letter: String = "",
    val sound: String = "",
    val targetSound: String = "",
    val targetWord: String = "",
    val emoji: String = "",
    val prefix: String = "",
    val suffix: String = "",
    val correctAnswer: String = "",
    val blendedWord: String = "",
    val meaning: String = "",
    val sentence: String = "",
    val category: String = "",
    val skillName: String = "",
    val letters: List<String> = emptyList(),
    val sounds: List<String> = emptyList(),
    val options: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val helperWords: List<String> = emptyList(),
    val decodableWords: List<String> = emptyList(),
    val examples: List<ExampleItem> = emptyList(),
    val pictureOptions: List<PictureOption> = emptyList()
)

data class DayPlan(
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val targetSounds: List<String>,
    val activities: List<ActivityItem>
)

data class ParentReportData(
    val totalTimeMinutes: Int,
    val totalActivitiesCompleted: Int,
    val totalWordsAttempted: Int,
    val independentCount: Int,
    val assistedCount: Int,
    val needsPracticeCount: Int,
    val strongSounds: List<String>,
    val needsPracticeSounds: List<String>,
    val independentWords: List<String>,
    val needsPracticeWords: List<String>,
    val actionAdvice: String
)
