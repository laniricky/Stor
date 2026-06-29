package com.stor.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Auth
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String
)

@Serializable
data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

// Expense
@Serializable
data class ExpenseDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val amount: Double,
    val category: String,
    @SerialName("payment_method") val paymentMethod: String,
    val date: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val description: String? = null,
    val amount: Double,
    val category: String,
    @SerialName("payment_method") val paymentMethod: String,
    val date: String,
    val notes: String? = null
)

// Income
@Serializable
data class IncomeDto(
    val id: String,
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateIncomeRequest(
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String? = null
)

// Loan
@Serializable
data class LoanDto(
    val id: String,
    val name: String,
    val lender: String,
    @SerialName("original_amount") val originalAmount: Double,
    @SerialName("remaining_balance") val remainingBalance: Double,
    @SerialName("interest_rate") val interestRate: Double? = null,
    @SerialName("monthly_payment") val monthlyPayment: Double? = null,
    @SerialName("due_day") val dueDay: Int? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateLoanRequest(
    val name: String,
    val lender: String,
    @SerialName("original_amount") val originalAmount: Double,
    @SerialName("interest_rate") val interestRate: Double? = null,
    @SerialName("monthly_payment") val monthlyPayment: Double? = null,
    @SerialName("due_day") val dueDay: Int? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null
)

// Repayment
@Serializable
data class RepaymentDto(
    val id: String,
    @SerialName("loan_id") val loanId: String,
    @SerialName("amount_paid") val amountPaid: Double,
    val date: String,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateRepaymentRequest(
    @SerialName("amount_paid") val amountPaid: Double,
    val date: String,
    val notes: String? = null
)

// Dashboard - matches DashboardData from backend
@Serializable
data class DashboardDto(
    @SerialName("total_balance") val totalBalance: Double,
    @SerialName("monthly_income") val monthlyIncome: Double,
    @SerialName("monthly_expenses") val monthlyExpenses: Double,
    @SerialName("today_spending") val todaySpending: Double,
    @SerialName("outstanding_debt") val outstandingDebt: Double,
    @SerialName("upcoming_loan_payments") val upcomingLoanPayments: List<UpcomingPaymentDto>,
    @SerialName("recent_expenses") val recentExpenses: List<RecentTransactionDto>,
    @SerialName("monthly_chart") val monthlyChart: List<ChartDataPointDto>
)

@Serializable
data class UpcomingPaymentDto(
    @SerialName("loan_id") val loanId: String,
    @SerialName("loan_name") val loanName: String,
    val amount: Double,
    @SerialName("due_label") val dueLabel: String
)

@Serializable
data class RecentTransactionDto(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String
)

@Serializable
data class ChartDataPointDto(
    val label: String,
    val income: Double,
    val expenses: Double
)

// Reports
@Serializable
data class MonthlyReportDto(
    val month: Int,
    val year: Int,
    @SerialName("total_income") val totalIncome: Double,
    @SerialName("total_expenses") val totalExpenses: Double,
    val savings: Double,
    @SerialName("expense_by_category") val expenseByCategory: Map<String, Double>
)

@Serializable
data class CategoryReportDto(
    val category: String,
    val total: Double,
    val percentage: Double
)

// Generic wrapper
@Serializable
data class MessageResponse(val message: String)
