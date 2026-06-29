package com.stor.data.repository

import com.stor.data.remote.api.StorApi
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.ChartDataPoint
import com.stor.domain.model.Dashboard
import com.stor.domain.model.RecentTransaction
import com.stor.domain.model.UpcomingPayment
import com.stor.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: StorApi
) : DashboardRepository {

    override suspend fun getDashboard(): Result<Dashboard> = runCatching {
        val response = api.getDashboard()
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Failed to load dashboard")

        Dashboard(
            totalBalance = body.totalBalance,
            monthlyIncome = body.monthlyIncome,
            monthlySpending = body.monthlyExpenses,
            outstandingDebt = body.outstandingDebt,
            todaySpending = body.todaySpending,
            recentTransactions = body.recentExpenses.map { dto ->
                RecentTransaction(
                    id = dto.id,
                    title = dto.title,
                    amount = dto.amount,
                    category = dto.category,
                    date = dto.date,
                    type = dto.type
                )
            },
            upcomingLoanPayments = body.upcomingLoanPayments.map { dto ->
                UpcomingPayment(
                    loanId = dto.loanId,
                    loanName = dto.loanName,
                    amount = dto.amount,
                    dueLabel = dto.dueLabel
                )
            },
            monthlyChart = body.monthlyChart.map { dto ->
                ChartDataPoint(
                    label = dto.label,
                    income = dto.income,
                    expenses = dto.expenses
                )
            }
        )
    }
}
