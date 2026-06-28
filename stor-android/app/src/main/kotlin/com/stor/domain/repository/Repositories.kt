package com.stor.domain.repository

import com.stor.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpenses(): Flow<List<Expense>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun createExpense(expense: Expense): Result<Expense>
    suspend fun updateExpense(expense: Expense): Result<Expense>
    suspend fun deleteExpense(id: String): Result<Unit>
    suspend fun syncExpenses(): Result<Unit>
}

interface IncomeRepository {
    fun getIncome(): Flow<List<Income>>
    suspend fun getIncomeById(id: String): Income?
    suspend fun createIncome(income: Income): Result<Income>
    suspend fun updateIncome(income: Income): Result<Income>
    suspend fun deleteIncome(id: String): Result<Unit>
    suspend fun syncIncome(): Result<Unit>
}

interface LoanRepository {
    fun getLoans(): Flow<List<Loan>>
    fun getActiveLoans(): Flow<List<Loan>>
    suspend fun getLoanById(id: String): Loan?
    suspend fun createLoan(loan: Loan): Result<Loan>
    suspend fun updateLoan(loan: Loan): Result<Loan>
    suspend fun deleteLoan(id: String): Result<Unit>
    suspend fun syncLoans(): Result<Unit>
}

interface RepaymentRepository {
    fun getRepayments(loanId: String): Flow<List<Repayment>>
    suspend fun createRepayment(loanId: String, repayment: Repayment): Result<Repayment>
    suspend fun syncRepayments(loanId: String): Result<Unit>
}

interface DashboardRepository {
    suspend fun getDashboard(): Result<Dashboard>
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
}
