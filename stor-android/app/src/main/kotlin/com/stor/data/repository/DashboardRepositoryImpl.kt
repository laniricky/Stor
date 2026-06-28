package com.stor.data.repository

import com.stor.data.remote.api.StorApi
import com.stor.domain.model.Dashboard
import com.stor.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: StorApi
) : DashboardRepository {

    override suspend fun getDashboard(): Result<Dashboard> = runCatching {
        val response = api.getDashboard()
        val body = response.body() ?: error("Failed to load dashboard")
        Dashboard(
            totalBalance = body.totalBalance,
            monthlyIncome = body.monthlyIncome,
            monthlySpending = body.monthlySpending,
            outstandingDebt = body.outstandingDebt,
            todaySpending = body.todaySpending,
            recentExpenses = body.recentExpenses.map { it.toDomain() },
            upcomingLoanPayments = body.upcomingLoanPayments.map { it.toDomain() }
        )
    }
}
