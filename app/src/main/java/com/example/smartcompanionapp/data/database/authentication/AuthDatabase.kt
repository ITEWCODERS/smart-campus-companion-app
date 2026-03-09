package com.example.smartcompanionapp.data.database.authentication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AuthDatabase : RoomDatabase() {

    abstract fun authDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: AuthDatabase? = null

        fun getDatabase(context: Context): AuthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuthDatabase::class.java,
                    "auth_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
