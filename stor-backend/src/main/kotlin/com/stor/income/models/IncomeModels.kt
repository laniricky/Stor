package com.stor.income.models

import kotlinx.serialization.Serializable

@Serializable
data class IncomeDto(
    val id: String,
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String?,
    val createdAt: String
)

@Serializable
data class CreateIncomeRequest(
    val source: String,
    val amount: Double,
    val date: String,
    val notes: String? = null
)

@Serializable
data class UpdateIncomeRequest(
    val source: String? = null,
    val amount: Double? = null,
    val date: String? = null,
    val notes: String? = null
)

@Serializable
data class IncomeListResponse(
    val income: List<IncomeDto>,
    val total: Int,
    val monthlyTotal: Double,
    val annualTotal: Double
)
