package com.stor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val date: String,
    val notes: String?,
    val createdAt: String,
    val isSynced: Boolean = true
)
