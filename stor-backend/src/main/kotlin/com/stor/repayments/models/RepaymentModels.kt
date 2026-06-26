package com.stor.repayments.models

import kotlinx.serialization.Serializable

@Serializable
data class RepaymentDto(
    val id: String,
    val loanId: String,
    val amountPaid: Double,
    val date: String,
    val notes: String?,
    val createdAt: String
)

@Serializable
data class CreateRepaymentRequest(
    val amountPaid: Double,
    val date: String,
    val notes: String? = null
)

@Serializable
data class RepaymentListResponse(
    val repayments: List<RepaymentDto>,
    val totalPaid: Double,
    val totalRemaining: Double
)
