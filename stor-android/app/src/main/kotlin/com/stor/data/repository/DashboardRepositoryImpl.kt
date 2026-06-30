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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        // If we have local unsynced records, the backend dashboard data will be stale.
        // We must compute the dashboard locally until the SyncWorker clears the queue.
        val hasUnsynced = expenseDao.getUnsynced().isNotEmpty() ||
                          incomeDao.getUnsynced().isNotEmpty() ||
                          loanDao.getUnsynced().isNotEmpty()

        if (hasUnsynced) {
            return@runCatching buildOfflineDashboard().copy(isOffline = false)
        }

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

    /** Build a dashboard approximation from locally cached Room data with proper date filtering. */
    private suspend fun buildOfflineDashboard(): Dashboard {
        val allExpenses = expenseDao.getAllExpensesList()
        val allLoans    = loanDao.getAllLoansList()
        val allIncome   = incomeDao.getAllIncomeList()

        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)          // "yyyy-MM-dd"
        val currentYearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM")) // "yyyy-MM"

        // Filter to current month only — date field is stored as "yyyy-MM-dd"
        val thisMonthExpenses = allExpenses.filter { it.date.startsWith(currentYearMonth) }
        val thisMonthIncome   = allIncome.filter   { it.date.startsWith(currentYearMonth) }

        // Today's spending — expenses whose date matches today exactly
        val todaySpending = allExpenses
            .filter { it.date.startsWith(todayStr) }
            .sumOf { it.amount }

        val monthlyIncome   = thisMonthIncome.sumOf { it.amount }
        val monthlyExpenses = thisMonthExpenses.sumOf { it.amount }
        val outstandingDebt = allLoans.filter { it.status == "active" }.sumOf { it.remainingBalance }
        val totalBalance    = monthlyIncome - monthlyExpenses

        // Recent transactions — all local expenses sorted newest first
        val recent = allExpenses
            .sortedByDescending { it.date }
            .take(5)
            .map { e ->
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
