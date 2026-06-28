package com.stor.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val amount: Double,
    val category: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: String,
    val date: String,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = true
)
