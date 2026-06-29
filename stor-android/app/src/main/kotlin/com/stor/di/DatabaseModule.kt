package com.stor.di

import android.content.Context
import androidx.room.Room
import com.stor.data.local.StorDatabase
import com.stor.data.local.dao.DashboardCacheDao
import com.stor.data.local.dao.ExpenseDao
import com.stor.data.local.dao.IncomeDao
import com.stor.data.local.dao.LoanDao
import com.stor.data.local.dao.RepaymentDao
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
    fun provideDatabase(@ApplicationContext context: Context): StorDatabase {
        return Room.databaseBuilder(
            context,
            StorDatabase::class.java,
            "stor_database"
        )
            .addMigrations(StorDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideExpenseDao(db: StorDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideIncomeDao(db: StorDatabase): IncomeDao = db.incomeDao()

    @Provides
    fun provideLoanDao(db: StorDatabase): LoanDao = db.loanDao()

    @Provides
    fun provideRepaymentDao(db: StorDatabase): RepaymentDao = db.repaymentDao()

    @Provides
    fun provideDashboardCacheDao(db: StorDatabase): DashboardCacheDao = db.dashboardCacheDao()
}
