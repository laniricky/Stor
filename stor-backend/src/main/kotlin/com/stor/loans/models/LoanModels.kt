package com.stor.loans.models

import kotlinx.serialization.Serializable

@Serializable
data class LoanDto(
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
    val percentagePaid: Double,
    val createdAt: String
)

@Serializable
data class CreateLoanRequest(
    val name: String,
    val lender: String,
    val originalAmount: Double,
    val interestRate: Double? = null,
    val monthlyPayment: Double? = null,
    val dueDay: Int? = null,
    val startDate: String,
    val endDate: String? = null
)

@Serializable
data class UpdateLoanRequest(
    val name: String? = null,
    val lender: String? = null,
    val interestRate: Double? = null,
    val monthlyPayment: Double? = null,
    val dueDay: Int? = null,
    val endDate: String? = null,
    val status: String? = null
)

@Serializable
data class LoanListResponse(
    val loans: List<LoanDto>,
    val totalOutstanding: Double,
    val activeLoanCount: Int
)
