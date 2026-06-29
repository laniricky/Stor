package com.stor.data.repository

import com.stor.data.local.dao.DashboardCacheDao
import com.stor.data.local.dao.ExpenseDao
import com.stor.data.local.dao.IncomeDao
import com.stor.data.local.dao.LoanDao
import com.stor.data.local.entities.DashboardCacheEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.ChartDataPoint
import com.stor.domain.model.Dashboard
import com.stor.domain.model.RecentTransaction
import com.stor.domain.model.UpcomingPayment
import com.stor.domain.repository.DashboardRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val cacheDao: DashboardCacheDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val loanDao: LoanDao
) : DashboardRepository {

    override suspend fun getDashboard(): Result<Dashboard> = runCatching {
        try {
            // ── Online path ─────────────────────────────────────────────────
            val response = api.getDashboard()
            if (!response.isSuccessful) throw Exception(response.getErrorMessage())
            val body = response.body() ?: error("Failed to load dashboard")

            // Persist to cache for offline use
            cacheDao.upsert(
                DashboardCacheEntity(
                    totalBalance    = body.totalBalance,
                    monthlyIncome   = body.monthlyIncome,
                    monthlyExpenses = body.monthlyExpenses,
                    todaySpending   = body.todaySpending,
                    outstandingDebt = body.outstandingDebt
                )
            )

            Dashboard(
                totalBalance       = body.totalBalance,
                monthlyIncome      = body.monthlyIncome,
                monthlySpending    = body.monthlyExpenses,
                outstandingDebt    = body.outstandingDebt,
                todaySpending      = body.todaySpending,
                recentTransactions = body.recentExpenses.map { dto ->
                    RecentTransaction(
                        id       = dto.id,
                        title    = dto.title,
                        amount   = dto.amount,
                        category = dto.category,
                        date     = dto.date,
                        type     = dto.type
                    )
                },
                upcomingLoanPayments = body.upcomingLoanPayments.map { dto ->
                    UpcomingPayment(
                        loanId   = dto.loanId,
                        loanName = dto.loanName,
                        amount   = dto.amount,
                        dueLabel = dto.dueLabel
                    )
                },
                monthlyChart = body.monthlyChart.map { dto ->
                    ChartDataPoint(label = dto.label, income = dto.income, expenses = dto.expenses)
                },
                isOffline = false
            )
        } catch (e: IOException) {
            // ── Offline path — compute from local Room cache ─────────────────
            buildOfflineDashboard()
        }
    }

    /** Build a dashboard approximation from locally cached Room data. */
    private suspend fun buildOfflineDashboard(): Dashboard {
        val cached      = cacheDao.get()
        val allExpenses = expenseDao.getAllExpensesList()
        val allLoans    = loanDao.getAllLoansList()

        // Use cached totals if available, otherwise compute from local records
        val monthlyIncome   = cached?.monthlyIncome ?: 0.0
        val monthlyExpenses = cached?.monthlyExpenses ?: allExpenses.filter { !it.id.startsWith("LOCAL_") || true }
                                  .sumOf { it.amount }
        val outstandingDebt = cached?.outstandingDebt
            ?: allLoans.filter { it.status == "active" }.sumOf { it.remainingBalance }
        val todaySpending   = cached?.todaySpending ?: 0.0
        val totalBalance    = cached?.totalBalance ?: (monthlyIncome - monthlyExpenses)

        // Recent transactions from local expense records
        val recent = allExpenses.take(5).map { e ->
            RecentTransaction(
                id       = e.id,
                title    = e.title,
                amount   = -e.amount,
                category = e.category,
                date     = e.date,
                type     = "expense"
            )
        }

        // Upcoming payments from local loan records
        val upcoming = allLoans.filter { it.status == "active" && it.dueDay != null }.map { l ->
            UpcomingPayment(
                loanId   = l.id,
                loanName = l.name,
                amount   = l.monthlyPayment ?: 0.0,
                dueLabel = "Due day ${l.dueDay}"
            )
        }

        return Dashboard(
            totalBalance         = totalBalance,
            monthlyIncome        = monthlyIncome,
            monthlySpending      = monthlyExpenses,
            outstandingDebt      = outstandingDebt,
            todaySpending        = todaySpending,
            recentTransactions   = recent,
            upcomingLoanPayments = upcoming,
            monthlyChart         = emptyList(), // chart requires server-side aggregation
            isOffline            = true
        )
    }
}
