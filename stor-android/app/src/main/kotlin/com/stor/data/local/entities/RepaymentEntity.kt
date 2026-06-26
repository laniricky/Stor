package com.stor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repayments")
data class RepaymentEntity(
    @PrimaryKey
    val id: String,
    val loanId: String,
    val amountPaid: Double,
    val date: String,
    val notes: String?,
    val createdAt: String,
    val isSynced: Boolean = true
)
