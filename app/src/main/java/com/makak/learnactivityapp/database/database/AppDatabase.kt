package com.makak.learnactivityapp.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.dao.SiteDao
import com.makak.learnactivityapp.database.dao.MonthDao
import com.makak.learnactivityapp.database.dao.BlockDao
import com.makak.learnactivityapp.database.dao.PersonDao

@Database(
    entities = [Site::class, Month::class, Block::class, Person::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun monthDao(): MonthDao
    abstract fun blockDao(): BlockDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apartment_management_database"
                ).fallbackToDestructiveMigration() // Geliştirme aşamasında
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}