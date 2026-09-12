package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.readingtutor.curriculum.CurriculumRepository
import com.example.readingtutor.data.ReadingTutorDatabase
import com.example.readingtutor.data.ReadingTutorRepository
import com.example.readingtutor.models.AttemptResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Reading Tutor", appName)
    }

    @Test
    fun `test curriculum course loading`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val course = CurriculumRepository.loadCourseInfo(context)
        assertEquals(7, course.totalDays)
        assertEquals(7, course.days.size)
        assertEquals("Letters Have Sounds", course.days[0].title)
        assertEquals("Practice & Assessment", course.days[6].title)
    }

    @Test
    fun `test all 7 days load without error`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (day in 1..7) {
            val plan = CurriculumRepository.loadDayPlan(context, day)
            assertEquals(day, plan.dayNumber)
            assertTrue("Day $day should have activities", plan.activities.isNotEmpty())
            assertTrue("Day $day should have target sounds", plan.targetSounds.isNotEmpty())
        }
    }

    @Test
    fun `test repository progress and unlocking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = Room.inMemoryDatabaseBuilder(
            context,
            ReadingTutorDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = ReadingTutorRepository(inMemoryDb.dao(), context)
        repository.initializeIfEmpty()

        val day1 = repository.getDayProgress(1)
        assertNotNull(day1)
        assertTrue("Day 1 must be unlocked initially", day1!!.isUnlocked)

        val day2 = repository.getDayProgress(2)
        assertNotNull(day2)
        assertEquals("Day 2 must be locked initially", false, day2!!.isUnlocked)

        // Complete Day 1
        repository.updateActivityProgress(
            dayNumber = 1,
            activityIndex = 7, // last activity
            totalActivities = 8
        )

        // Day 2 should now be unlocked!
        val day2After = repository.getDayProgress(2)
        assertNotNull(day2After)
        assertTrue("Day 2 should unlock after Day 1 completes", day2After!!.isUnlocked)

        // Record an independent attempt and test parent report
        repository.recordAttempt(
            dayNumber = 1,
            activityId = "d1_test",
            activityType = "LISTEN_AND_CHOOSE",
            targetItem = "M",
            result = AttemptResult.INDEPENDENT
        )

        val report = repository.getParentReport()
        assertTrue("Report should reflect independent count", report.independentCount >= 1)

        inMemoryDb.close()
    }
}
