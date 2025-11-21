package com.makak.learnactivityapp.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.dao.SiteDao

@Database(
    entities = [Site::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apartment_management_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}