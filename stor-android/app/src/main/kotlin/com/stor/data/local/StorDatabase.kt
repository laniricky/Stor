package com.stor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stor.data.local.dao.DashboardCacheDao
import com.stor.data.local.dao.ExpenseDao
import com.stor.data.local.dao.IncomeDao
import com.stor.data.local.dao.LoanDao
import com.stor.data.local.dao.RepaymentDao
import com.stor.data.local.entities.DashboardCacheEntity
import com.stor.data.local.entities.ExpenseEntity
import com.stor.data.local.entities.IncomeEntity
import com.stor.data.local.entities.LoanEntity
import com.stor.data.local.entities.RepaymentEntity

@Database(
    entities = [
        ExpenseEntity::class,
        IncomeEntity::class,
        LoanEntity::class,
        RepaymentEntity::class,
        DashboardCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StorDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun dashboardCacheDao(): DashboardCacheDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dashboard_cache (
                        id INTEGER NOT NULL PRIMARY KEY,
                        total_balance REAL NOT NULL,
                        monthly_income REAL NOT NULL,
                        monthly_expenses REAL NOT NULL,
                        today_spending REAL NOT NULL,
                        outstanding_debt REAL NOT NULL,
                        cached_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
