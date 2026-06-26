package com.stor.expenses.models

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
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

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val description: String? = null,
    val amount: Double,
    val category: String,
    val paymentMethod: String = "Cash",
    val date: String,
    val notes: String? = null
)

@Serializable
data class UpdateExpenseRequest(
    val title: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val category: String? = null,
    val paymentMethod: String? = null,
    val date: String? = null,
    val notes: String? = null
)

@Serializable
data class ExpenseListResponse(
    val expenses: List<ExpenseDto>,
    val total: Int,
    val monthlyTotal: Double
)

val VALID_CATEGORIES = setOf(
    "Food", "Transport", "Rent", "Utilities", "Shopping",
    "Entertainment", "Health", "Education", "Other"
)

val VALID_PAYMENT_METHODS = setOf("Cash", "Card", "M-Pesa", "Bank Transfer", "Other")
