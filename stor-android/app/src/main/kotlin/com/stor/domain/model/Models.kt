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
    val createdAt: String,
    val isSynced: Boolean = true
)

// Income domain model
data class Income(
    val id: String,
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String?,
    val createdAt: String,
    val isSynced: Boolean = true
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
    val createdAt: String,
    val isSynced: Boolean = true
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
data class UpcomingPayment(
    val loanId: String,
    val loanName: String,
    val amount: Double,
    val dueLabel: String
)

data class RecentTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String
)

data class ChartDataPoint(
    val label: String,
    val income: Double,
    val expenses: Double
)

data class Dashboard(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlySpending: Double,
    val outstandingDebt: Double,
    val todaySpending: Double,
    val recentTransactions: List<RecentTransaction>,
    val upcomingLoanPayments: List<UpcomingPayment>,
    val monthlyChart: List<ChartDataPoint>,
    val isOffline: Boolean = false
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
