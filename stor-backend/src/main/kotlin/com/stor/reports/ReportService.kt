package com.stor.reports

import com.stor.expenses.ExpenseRepository
import com.stor.income.IncomeRepository
import com.stor.loans.LoanRepository
import com.stor.repayments.RepaymentRepository
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

@Serializable
data class MonthlyReport(
    val month: Int,
    val year: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netSavings: Double,
    val totalLoansDue: Double,
    val expensesByCategory: Map<String, Double>
)

@Serializable
data class YearlyReport(
    val year: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netSavings: Double,
    val monthlyBreakdown: List<MonthSummary>
)

@Serializable
data class MonthSummary(
    val month: Int,
    val monthName: String,
    val income: Double,
    val expenses: Double,
    val savings: Double
)

@Serializable
data class CategoryReport(
    val month: Int,
    val year: Int,
    val categories: Map<String, Double>,
    val total: Double
)

class ReportService {
    private val expenseRepo = ExpenseRepository()
    private val incomeRepo = IncomeRepository()
    private val loanRepo = LoanRepository()

    fun monthlyReport(userId: String, month: Int, year: Int): MonthlyReport {
        val uuid = UUID.fromString(userId)
        val totalIncome = incomeRepo.monthlyTotal(uuid, month, year)
        val totalExpenses = expenseRepo.monthlyTotal(uuid, month, year)
        val byCategory = expenseRepo.sumByCategory(uuid, month, year)
        val loanData = loanRepo.findAll(uuid, "active")
        val loansDue = loanData.loans.sumOf { it.monthlyPayment ?: 0.0 }

        return MonthlyReport(
            month = month,
            year = year,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavings = totalIncome - totalExpenses,
            totalLoansDue = loansDue,
            expensesByCategory = byCategory
        )
    }

    fun yearlyReport(userId: String, year: Int): YearlyReport {
        val uuid = UUID.fromString(userId)
        val totalIncome = incomeRepo.annualTotal(uuid, year)
        val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        val breakdown = (1..12).map { m ->
            val inc = incomeRepo.monthlyTotal(uuid, m, year)
            val exp = expenseRepo.monthlyTotal(uuid, m, year)
            MonthSummary(
                month = m,
                monthName = monthNames[m],
                income = inc,
                expenses = exp,
                savings = inc - exp
            )
        }

        val totalExpenses = breakdown.sumOf { it.expenses }
        return YearlyReport(
            year = year,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavings = totalIncome - totalExpenses,
            monthlyBreakdown = breakdown
        )
    }

    fun categoryReport(userId: String, month: Int, year: Int): CategoryReport {
        val uuid = UUID.fromString(userId)
        val byCategory = expenseRepo.sumByCategory(uuid, month, year)
        return CategoryReport(
            month = month,
            year = year,
            categories = byCategory,
            total = byCategory.values.sum()
        )
    }
}
