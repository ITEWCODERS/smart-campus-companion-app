package com.example.smartcompanionapp.data.database.announcement

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.data.model.Announcement

@Database(entities = [Announcement::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_campus_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}