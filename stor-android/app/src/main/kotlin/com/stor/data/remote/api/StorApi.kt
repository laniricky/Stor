package com.stor.data.remote.api

import com.stor.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface StorApi {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<AuthResponse>

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboard(): Response<DashboardDto>

    // Expenses
    @GET("expenses")
    suspend fun getExpenses(
        @Query("category") category: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ExpensesResponse>

    @GET("expenses/{id}")
    suspend fun getExpense(@Path("id") id: String): Response<ExpenseDto>

    @POST("expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): Response<ExpenseDto>

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: CreateExpenseRequest
    ): Response<ExpenseDto>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Response<MessageResponse>

    // Income
    @GET("income")
    suspend fun getIncome(): Response<IncomeResponse>

    @GET("income/{id}")
    suspend fun getIncomeById(@Path("id") id: String): Response<IncomeDto>

    @POST("income")
    suspend fun createIncome(@Body request: CreateIncomeRequest): Response<IncomeDto>

    @PUT("income/{id}")
    suspend fun updateIncome(
        @Path("id") id: String,
        @Body request: CreateIncomeRequest
    ): Response<IncomeDto>

    @DELETE("income/{id}")
    suspend fun deleteIncome(@Path("id") id: String): Response<MessageResponse>

    // Loans
    @GET("loans")
    suspend fun getLoans(): Response<LoansResponse>

    @GET("loans/{id}")
    suspend fun getLoan(@Path("id") id: String): Response<LoanDto>

    @POST("loans")
    suspend fun createLoan(@Body request: CreateLoanRequest): Response<LoanDto>

    @PUT("loans/{id}")
    suspend fun updateLoan(
        @Path("id") id: String,
        @Body request: CreateLoanRequest
    ): Response<LoanDto>

    @DELETE("loans/{id}")
    suspend fun deleteLoan(@Path("id") id: String): Response<MessageResponse>

    // Repayments
    @GET("loans/{loanId}/repayments")
    suspend fun getRepayments(@Path("loanId") loanId: String): Response<RepaymentsResponse>

    @POST("loans/{loanId}/repayments")
    suspend fun createRepayment(
        @Path("loanId") loanId: String,
        @Body request: CreateRepaymentRequest
    ): Response<RepaymentDto>

    // Reports
    @GET("reports/monthly")
    suspend fun getMonthlyReport(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<MonthlyReportDto>

    @GET("reports/categories")
    suspend fun getCategoryReport(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<List<CategoryReportDto>>

    // Search
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null
    ): Response<SearchResultDto>
}

// Extra DTO needed only for the API interface
@kotlinx.serialization.Serializable
data class SearchResultDto(
    val expenses: List<com.stor.data.remote.dto.ExpenseDto> = emptyList(),
    val income: List<com.stor.data.remote.dto.IncomeDto> = emptyList(),
    val loans: List<com.stor.data.remote.dto.LoanDto> = emptyList()
)
