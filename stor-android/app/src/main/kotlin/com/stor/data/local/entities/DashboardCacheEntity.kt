package com.stor.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table caching the last successful dashboard API response.
 * Used as offline fallback when the device has no network.
 */
@Entity(tableName = "dashboard_cache")
data class DashboardCacheEntity(
    @PrimaryKey val id: Int = 1, // always single row
    @ColumnInfo(name = "total_balance") val totalBalance: Double,
    @ColumnInfo(name = "monthly_income") val monthlyIncome: Double,
    @ColumnInfo(name = "monthly_expenses") val monthlyExpenses: Double,
    @ColumnInfo(name = "today_spending") val todaySpending: Double,
    @ColumnInfo(name = "outstanding_debt") val outstandingDebt: Double,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis()
)
