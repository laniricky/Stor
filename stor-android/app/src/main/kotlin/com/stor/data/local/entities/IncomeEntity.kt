package com.stor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey
    val id: String,
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String?,
    val createdAt: String,
    val isSynced: Boolean = true
)
