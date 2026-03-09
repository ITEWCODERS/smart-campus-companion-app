package com.example.smartcompanionapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartcompanionapp.data.TaskDao
import com.example.smartcompanionapp.data.model.Task

// ✅ Removed Tasks::class — we only have ONE unified entity now
// ✅ version bumped to 2 — required because Task schema changed (added date, subject, description)
// ✅ fallbackToDestructiveMigration() — wipes old DB and starts fresh instead of crashing
@Database(
    entities = [Task::class],
    version = 2,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}