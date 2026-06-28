package com.stor.domain.model

// Expense domain model
data class Expense(
    val id: String,
    val title: String,
    val description: String?,
    val amount: Double,
    val category: String,
    val paymentMethod: String,
    val date: String,
    val notes: String?,
    val createdAt: String
)

// Income domain model
data class Income(
    val id: String,
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String?,
    val createdAt: String
)

// Loan domain model
data class Loan(
    val id: String,
    val name: String,
    val lender: String,
    val originalAmount: Double,
    val remainingBalance: Double,
    val interestRate: Double?,
    val monthlyPayment: Double?,
    val dueDay: Int?,
    val startDate: String,
    val endDate: String?,
    val status: String,
    val createdAt: String
) {
    val percentagePaid: Double
        get() = if (originalAmount > 0) ((originalAmount - remainingBalance) / originalAmount) * 100 else 0.0
    
    val amountPaid: Double
        get() = originalAmount - remainingBalance
}

// Repayment domain model
data class Repayment(
    val id: String,
    val loanId: String,
    val amountPaid: Double,
    val date: String,
    val notes: String?,
    val createdAt: String
)

// Dashboard domain model
data class Dashboard(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlySpending: Double,
    val outstandingDebt: Double,
    val todaySpending: Double,
    val recentExpenses: List<Expense>,
    val upcomingLoanPayments: List<Loan>
)

// Report domain models
data class MonthlyReport(
    val month: Int,
    val year: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val savings: Double,
    val expenseByCategory: Map<String, Double>
)

data class CategoryReport(
    val category: String,
    val total: Double,
    val percentage: Double
)

// User
data class User(
    val id: String,
    val name: String,
    val email: String
)
