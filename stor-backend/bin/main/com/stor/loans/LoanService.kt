package com.stor.loans

import com.stor.common.ApiException
import com.stor.loans.models.*
import java.util.UUID

class LoanService(private val repo: LoanRepository = LoanRepository()) {

    fun list(userId: String, status: String?) = repo.findAll(UUID.fromString(userId), status)

    fun get(userId: String, id: String) = repo.findById(UUID.fromString(id), UUID.fromString(userId))

    fun create(userId: String, req: CreateLoanRequest): LoanDto {
        if (req.name.isBlank()) throw ApiException.badRequest("Loan name is required")
        if (req.lender.isBlank()) throw ApiException.badRequest("Lender is required")
        if (req.originalAmount <= 0) throw ApiException.badRequest("Amount must be positive")
        return repo.create(UUID.fromString(userId), req)
    }

    fun update(userId: String, id: String, req: UpdateLoanRequest): LoanDto {
        req.status?.let {
            if (it !in setOf("active", "archived")) throw ApiException.badRequest("Invalid status")
        }
        return repo.update(UUID.fromString(id), UUID.fromString(userId), req)
    }

    fun delete(userId: String, id: String) = repo.delete(UUID.fromString(id), UUID.fromString(userId))
}
