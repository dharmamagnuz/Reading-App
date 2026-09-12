package com.example.readingtutor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profile")
data class ChildProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Learner",
    val currentDay: Int = 1,
    val streakDays: Int = 1,
    val totalStars: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "day_progress")
data class DayProgressEntity(
    @PrimaryKey val dayNumber: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0,
    val timeSpentSeconds: Long = 0,
    val completedActivitiesCount: Int = 0,
    val lastActivityIndex: Int = 0
)

@Entity(tableName = "activity_attempts")
data class ActivityAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int,
    val activityId: String,
    val activityType: String,
    val targetItem: String,
    val result: String, // "INDEPENDENT", "ASSISTED", "RETRY"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sound_mastery")
data class SoundMasteryEntity(
    @PrimaryKey val itemKey: String,
    val itemType: String, // "SOUND" or "WORD"
    val independentCount: Int = 0,
    val assistedCount: Int = 0,
    val retryCount: Int = 0,
    val needsPractice: Boolean = false
)
