package com.stor.dashboard

import com.stor.expenses.ExpenseRepository
import com.stor.income.IncomeRepository
import com.stor.loans.LoanRepository
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.UUID

@Serializable
data class DashboardData(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val todaySpending: Double,
    val outstandingDebt: Double,
    val upcomingLoanPayments: List<UpcomingPayment>,
    val recentExpenses: List<RecentTransaction>,
    val monthlyChart: List<ChartDataPoint>
)

@Serializable
data class UpcomingPayment(
    val loanId: String,
    val loanName: String,
    val amount: Double,
    val dueLabel: String
)

@Serializable
data class RecentTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String // "expense" | "income"
)

@Serializable
data class ChartDataPoint(
    val label: String,
    val income: Double,
    val expenses: Double
)

class DashboardService {
    private val expenseRepo = ExpenseRepository()
    private val incomeRepo = IncomeRepository()
    private val loanRepo = LoanRepository()

    fun getDashboard(userId: String): DashboardData {
        val uuid = UUID.fromString(userId)
        val now = LocalDate.now()
        val month = now.monthValue
        val year = now.year

        val monthlyExpenses = expenseRepo.monthlyTotal(uuid, month, year)
        val monthlyIncome = incomeRepo.monthlyTotal(uuid, month, year)
        val loanData = loanRepo.findAll(uuid, "active")
        val outstandingDebt = loanData.totalOutstanding

        val totalBalance = monthlyIncome - monthlyExpenses

        // Today's spending
        val (todayExpenses, _) = expenseRepo.findAll(uuid, null, month, year, null, 1, 100)
        val todaySpending = todayExpenses.filter { it.date == now.toString() }.sumOf { it.amount }

        // Recent transactions (last 5 expenses)
        val (recent, _) = expenseRepo.findAll(uuid, null, null, null, null, 1, 5)
        val recentTransactions = recent.map {
            RecentTransaction(
                id = it.id,
                title = it.title,
                amount = -it.amount,
                category = it.category,
                date = it.date,
                type = "expense"
            )
        }

        // Upcoming loan payments
        val upcoming = loanRepo.upcomingPayments(uuid).map { loan ->
            val today = LocalDate.now()
            val dueDay = loan.dueDay ?: 1
            // Ensure dueDay is at least 1 and at most the length of the month to prevent DateTimeException
            val safeDueDay = maxOf(1, minOf(dueDay, today.month.length(today.isLeapYear)))
            val dueDate = LocalDate.of(today.year, today.month, safeDueDay)
            val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
            val dueLabel = when {
                daysUntil == 0L -> "Due today"
                daysUntil == 1L -> "Due tomorrow"
                else -> "Due in $daysUntil days"
            }
            UpcomingPayment(
                loanId = loan.id,
                loanName = loan.name,
                amount = loan.monthlyPayment ?: 0.0,
                dueLabel = dueLabel
            )
        }

        // Last 6 months chart data
        val chartData = (5 downTo 0).map { offset ->
            val date = now.minusMonths(offset.toLong())
            val m = date.monthValue
            val y = date.year
            ChartDataPoint(
                label = date.month.name.take(3),
                income = incomeRepo.monthlyTotal(uuid, m, y),
                expenses = expenseRepo.monthlyTotal(uuid, m, y)
            )
        }

        return DashboardData(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            todaySpending = todaySpending,
            outstandingDebt = outstandingDebt,
            upcomingLoanPayments = upcoming,
            recentExpenses = recentTransactions,
            monthlyChart = chartData
        )
    }
}
