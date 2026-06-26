package com.stor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val lender: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val interestRate: Double?,
    val monthlyPayment: Double?,
    val dueDay: Int?,
    val startDate: String,
    val endDate: String?,
    val status: String,
    val percentagePaid: Double,
    val createdAt: String,
    val isSynced: Boolean = true
)
