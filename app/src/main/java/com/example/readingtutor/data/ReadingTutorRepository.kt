package com.example.readingtutor.data

import android.content.Context
import com.example.readingtutor.models.AttemptResult
import com.example.readingtutor.models.ParentReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ReadingTutorRepository(
    private val dao: ReadingTutorDao,
    private val context: Context
) {

    val profileFlow: Flow<ChildProfile?> = dao.getProfileFlow()
    val dayProgressFlow: Flow<List<DayProgressEntity>> = dao.getAllDayProgressFlow()
    val masteryFlow: Flow<List<SoundMasteryEntity>> = dao.getAllMasteryFlow()

    suspend fun initializeIfEmpty() {
        val existing = dao.getAllDayProgress()
        if (existing.isEmpty()) {
            val initialDays = (1..7).map { dayNum ->
                DayProgressEntity(
                    dayNumber = dayNum,
                    isUnlocked = (dayNum == 1), // Day 1 unlocked initially
                    isCompleted = false,
                    starsEarned = 0,
                    timeSpentSeconds = 0,
                    completedActivitiesCount = 0,
                    lastActivityIndex = 0
                )
            }
            dao.insertDayProgress(initialDays)
        }

        val profile = dao.getProfile()
        if (profile == null) {
            dao.insertOrUpdateProfile(
                ChildProfile(
                    id = 1,
                    name = "Learner",
                    currentDay = 1,
                    streakDays = 1,
                    totalStars = 0
                )
            )
        }
    }

    suspend fun getDayProgress(dayNumber: Int): DayProgressEntity? {
        return dao.getDayProgress(dayNumber)
    }

    suspend fun recordAttempt(
        dayNumber: Int,
        activityId: String,
        activityType: String,
        targetItem: String,
        result: AttemptResult
    ) {
        val entity = ActivityAttemptEntity(
            dayNumber = dayNumber,
            activityId = activityId,
            activityType = activityType,
            targetItem = targetItem,
            result = result.name
        )
        dao.insertAttempt(entity)

        if (targetItem.isNotBlank()) {
            val isSound = targetItem.length <= 2 || targetItem.contains("_")
            val existing = dao.getMastery(targetItem)
            val updated = if (existing != null) {
                when (result) {
                    AttemptResult.INDEPENDENT -> existing.copy(
                        independentCount = existing.independentCount + 1,
                        needsPractice = false
                    )
                    AttemptResult.ASSISTED -> existing.copy(
                        assistedCount = existing.assistedCount + 1
                    )
                    AttemptResult.RETRY -> existing.copy(
                        retryCount = existing.retryCount + 1,
                        needsPractice = true
                    )
                }
            } else {
                SoundMasteryEntity(
                    itemKey = targetItem,
                    itemType = if (isSound) "SOUND" else "WORD",
                    independentCount = if (result == AttemptResult.INDEPENDENT) 1 else 0,
                    assistedCount = if (result == AttemptResult.ASSISTED) 1 else 0,
                    retryCount = if (result == AttemptResult.RETRY) 1 else 0,
                    needsPractice = (result == AttemptResult.RETRY)
                )
            }
            dao.insertOrUpdateMastery(updated)
        }
    }

    suspend fun updateActivityProgress(
        dayNumber: Int,
        activityIndex: Int,
        totalActivities: Int,
        earnedStars: Int = 1
    ) {
        val current = dao.getDayProgress(dayNumber) ?: return
        val isLast = activityIndex >= totalActivities - 1
        val isCompleted = current.isCompleted || isLast
        val newStars = current.starsEarned + earnedStars
        val newCompletedCount = maxOf(current.completedActivitiesCount, activityIndex + 1)

        val updated = current.copy(
            isCompleted = isCompleted,
            starsEarned = newStars,
            completedActivitiesCount = newCompletedCount,
            lastActivityIndex = if (isLast) 0 else activityIndex + 1,
            timeSpentSeconds = current.timeSpentSeconds + 45
        )
        dao.updateDayProgress(updated)

        // Unlock next day if this day was just completed
        if (isLast && dayNumber < 7) {
            val nextDay = dao.getDayProgress(dayNumber + 1)
            if (nextDay != null && !nextDay.isUnlocked) {
                dao.updateDayProgress(nextDay.copy(isUnlocked = true))
            }
        }

        // Update child profile total stars
        val profile = dao.getProfile()
        if (profile != null) {
            val nextCurrentDay = if (isLast && dayNumber < 7) dayNumber + 1 else dayNumber
            dao.insertOrUpdateProfile(
                profile.copy(
                    totalStars = profile.totalStars + earnedStars,
                    currentDay = maxOf(profile.currentDay, nextCurrentDay),
                    lastActiveTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getParentReport(): ParentReportData {
        val attempts = dao.getAllAttempts()
        val allProgress = dao.getAllDayProgress()
        val masteryList = dao.getAllMasteryFlow().firstOrNull() ?: emptyList()

        val totalTimeSeconds = allProgress.sumOf { it.timeSpentSeconds }
        val totalTimeMinutes = maxOf(1, (totalTimeSeconds / 60).toInt())
        val totalActivities = allProgress.sumOf { it.completedActivitiesCount }

        var independentCount = 0
        var assistedCount = 0
        var needsPracticeCount = 0

        for (att in attempts) {
            when (att.result) {
                "INDEPENDENT" -> independentCount++
                "ASSISTED" -> assistedCount++
                "RETRY" -> needsPracticeCount++
            }
        }

        val strongSounds = masteryList
            .filter { it.itemType == "SOUND" && it.independentCount >= 1 && it.retryCount == 0 }
            .map { it.itemKey.uppercase() }
            .distinct()

        val needsPracticeSounds = masteryList
            .filter { it.itemType == "SOUND" && (it.needsPractice || it.retryCount > 0) }
            .map { it.itemKey.uppercase() }
            .distinct()

        val independentWords = masteryList
            .filter { it.itemType == "WORD" && it.independentCount >= 1 && it.retryCount == 0 }
            .map { it.itemKey }
            .distinct()

        val needsPracticeWords = masteryList
            .filter { it.itemType == "WORD" && (it.needsPractice || it.retryCount > 0) }
            .map { it.itemKey }
            .distinct()

        val advice = when {
            needsPracticeSounds.isNotEmpty() ->
                "Focus on gentle practice for ${needsPracticeSounds.joinToString(", ")} sounds during the next review."
            needsPracticeWords.isNotEmpty() ->
                "Practice finger-slide blending for words like ${needsPracticeWords.take(3).joinToString(", ")}."
            independentCount > 10 ->
                "Outstanding reading independence! Ready to practice full simple sentences."
            else ->
                "Great start! Keep short, playful daily sessions to build phonics confidence."
        }

        return ParentReportData(
            totalTimeMinutes = totalTimeMinutes,
            totalActivitiesCompleted = totalActivities,
            totalWordsAttempted = attempts.count { it.targetItem.isNotBlank() },
            independentCount = independentCount,
            assistedCount = assistedCount,
            needsPracticeCount = needsPracticeCount,
            strongSounds = if (strongSounds.isEmpty()) listOf("M", "S", "A") else strongSounds,
            needsPracticeSounds = needsPracticeSounds,
            independentWords = independentWords,
            needsPracticeWords = needsPracticeWords,
            actionAdvice = advice
        )
    }

    suspend fun resetAllProgress() {
        dao.clearAttempts()
        dao.clearMastery()
        val initialDays = (1..7).map { dayNum ->
            DayProgressEntity(
                dayNumber = dayNum,
                isUnlocked = (dayNum == 1),
                isCompleted = false,
                starsEarned = 0,
                timeSpentSeconds = 0,
                completedActivitiesCount = 0,
                lastActivityIndex = 0
            )
        }
        dao.insertDayProgress(initialDays)
        dao.insertOrUpdateProfile(
            ChildProfile(
                id = 1,
                name = "Learner",
                currentDay = 1,
                streakDays = 1,
                totalStars = 0
            )
        )
    }

    suspend fun unlockAllDays() {
        val days = dao.getAllDayProgress()
        val updated = days.map { it.copy(isUnlocked = true) }
        dao.insertDayProgress(updated)
    }
}
