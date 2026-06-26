package com.stor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.stor.data.local.entities.ExpenseEntity
import com.stor.data.local.entities.IncomeEntity
import com.stor.data.local.entities.LoanEntity
import com.stor.data.local.entities.RepaymentEntity

@Database(
    entities = [
        ExpenseEntity::class,
        IncomeEntity::class,
        LoanEntity::class,
        RepaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StorDatabase : RoomDatabase() {
    // abstract fun expenseDao(): ExpenseDao
    // abstract fun incomeDao(): IncomeDao
    // abstract fun loanDao(): LoanDao
    // abstract fun repaymentDao(): RepaymentDao
}
