package com.makak.learnactivityapp.di

import android.content.Context
import androidx.room.Room
import com.makak.learnactivityapp.database.dao.BlockDao
import com.makak.learnactivityapp.database.dao.MonthDao
import com.makak.learnactivityapp.database.dao.PaymentDao
import com.makak.learnactivityapp.database.dao.PersonDao
import com.makak.learnactivityapp.database.dao.SiteDao
import com.makak.learnactivityapp.database.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "apartment_management_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSiteDao(database: AppDatabase): SiteDao {
        return database.siteDao()
    }

    @Provides
    @Singleton
    fun provideMonthDao(database: AppDatabase): MonthDao {
        return database.monthDao()
    }

    @Provides
    @Singleton
    fun provideBlockDao(database: AppDatabase): BlockDao {
        return database.blockDao()
    }

    @Provides
    @Singleton
    fun providePersonDao(database: AppDatabase): PersonDao {
        return database.personDao()
    }

    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }
}