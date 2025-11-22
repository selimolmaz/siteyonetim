package com.makak.learnactivityapp.di

import com.makak.learnactivityapp.database.dao.BlockDao
import com.makak.learnactivityapp.database.dao.MonthDao
import com.makak.learnactivityapp.database.dao.PaymentDao
import com.makak.learnactivityapp.database.dao.PersonDao
import com.makak.learnactivityapp.database.dao.SiteDao
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSiteRepository(siteDao: SiteDao): SiteRepository {
        return SiteRepository(siteDao)
    }

    @Provides
    @Singleton
    fun provideMonthRepository(monthDao: MonthDao): MonthRepository {
        return MonthRepository(monthDao)
    }

    @Provides
    @Singleton
    fun provideBlockRepository(blockDao: BlockDao): BlockRepository {
        return BlockRepository(blockDao)
    }

    @Provides
    @Singleton
    fun providePersonRepository(personDao: PersonDao): PersonRepository {
        return PersonRepository(personDao)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(paymentDao: PaymentDao): PaymentRepository {
        return PaymentRepository(paymentDao)
    }
}