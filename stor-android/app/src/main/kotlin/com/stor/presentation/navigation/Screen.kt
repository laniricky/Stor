package com.stor.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Main
    object Dashboard : Screen("dashboard")
    
    // Expenses
    object Expenses : Screen("expenses")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{id}") {
        fun createRoute(id: String) = "edit_expense/$id"
    }

    // Income
    object Income : Screen("income")
    object AddIncome : Screen("add_income")
    object EditIncome : Screen("edit_income/{id}") {
        fun createRoute(id: String) = "edit_income/$id"
    }

    // Loans
    object Loans : Screen("loans")
    object AddLoan : Screen("add_loan")
    object LoanDetail : Screen("loan_detail/{id}") {
        fun createRoute(id: String) = "loan_detail/$id"
    }

    // Repayments
    object Repayments : Screen("repayments/{loanId}") {
        fun createRoute(loanId: String) = "repayments/$loanId"
    }
    object AddRepayment : Screen("add_repayment/{loanId}") {
        fun createRoute(loanId: String) = "add_repayment/$loanId"
    }

    // Reports & Search
    object Reports : Screen("reports")
    object Search : Screen("search")
    
    // More / Settings
    object More : Screen("more")
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object BudgetSettings : Screen("budget_settings")
    object NotificationSettings : Screen("notification_settings")
    object ExportReports : Screen("export_reports")
}
