package com.stor.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lender: String,
    @ColumnInfo(name = "original_amount") val originalAmount: Double,
    @ColumnInfo(name = "remaining_balance") val remainingBalance: Double,
    @ColumnInfo(name = "interest_rate") val interestRate: Double?,
    @ColumnInfo(name = "monthly_payment") val monthlyPayment: Double?,
    @ColumnInfo(name = "due_day") val dueDay: Int?,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String?,
    val status: String,
    @ColumnInfo(name = "percentage_paid") val percentagePaid: Double,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = true
)
