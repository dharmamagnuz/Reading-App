package com.example.readingtutor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChildProfile::class,
        DayProgressEntity::class,
        ActivityAttemptEntity::class,
        SoundMasteryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReadingTutorDatabase : RoomDatabase() {
    abstract fun dao(): ReadingTutorDao

    companion object {
        @Volatile
        private var INSTANCE: ReadingTutorDatabase? = null

        fun getDatabase(context: Context): ReadingTutorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReadingTutorDatabase::class.java,
                    "reading_tutor_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
