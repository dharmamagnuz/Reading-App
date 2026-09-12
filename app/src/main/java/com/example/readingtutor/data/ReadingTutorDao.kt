package com.example.readingtutor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingTutorDao {

    // Profile
    @Query("SELECT * FROM child_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ChildProfile?>

    @Query("SELECT * FROM child_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ChildProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ChildProfile)

    // Day Progress
    @Query("SELECT * FROM day_progress ORDER BY dayNumber ASC")
    fun getAllDayProgressFlow(): Flow<List<DayProgressEntity>>

    @Query("SELECT * FROM day_progress ORDER BY dayNumber ASC")
    suspend fun getAllDayProgress(): List<DayProgressEntity>

    @Query("SELECT * FROM day_progress WHERE dayNumber = :dayNumber LIMIT 1")
    suspend fun getDayProgress(dayNumber: Int): DayProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayProgress(progressList: List<DayProgressEntity>)

    @Update
    suspend fun updateDayProgress(progress: DayProgressEntity)

    // Attempts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ActivityAttemptEntity)

    @Query("SELECT * FROM activity_attempts ORDER BY timestamp DESC")
    fun getAllAttemptsFlow(): Flow<List<ActivityAttemptEntity>>

    @Query("SELECT * FROM activity_attempts ORDER BY timestamp DESC")
    suspend fun getAllAttempts(): List<ActivityAttemptEntity>

    @Query("SELECT * FROM activity_attempts WHERE dayNumber = :dayNumber")
    suspend fun getAttemptsForDay(dayNumber: Int): List<ActivityAttemptEntity>

    // Sound and Word Mastery
    @Query("SELECT * FROM sound_mastery ORDER BY itemKey ASC")
    fun getAllMasteryFlow(): Flow<List<SoundMasteryEntity>>

    @Query("SELECT * FROM sound_mastery WHERE itemKey = :key LIMIT 1")
    suspend fun getMastery(key: String): SoundMasteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMastery(mastery: SoundMasteryEntity)

    // Clear / Reset
    @Query("DELETE FROM activity_attempts")
    suspend fun clearAttempts()

    @Query("DELETE FROM sound_mastery")
    suspend fun clearMastery()
}
