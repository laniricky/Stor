package com.stor.income

import com.stor.common.ApiException
import com.stor.income.models.*
import java.time.LocalDate
import java.util.UUID

class IncomeService(private val repo: IncomeRepository = IncomeRepository()) {

    fun list(userId: String, month: Int?, year: Int?): IncomeListResponse {
        val uuid = UUID.fromString(userId)
        val now = LocalDate.now()
        val m = month ?: now.monthValue
        val y = year ?: now.year
        val (list, total) = repo.findAll(uuid, m, y)
        return IncomeListResponse(
            income = list,
            total = total,
            monthlyTotal = repo.monthlyTotal(uuid, m, y),
            annualTotal = repo.annualTotal(uuid, y)
        )
    }

    fun get(userId: String, id: String) = repo.findById(UUID.fromString(id), UUID.fromString(userId))

    fun create(userId: String, req: CreateIncomeRequest): IncomeDto {
        if (req.source.isBlank()) throw ApiException.badRequest("Source is required")
        if (req.amount <= 0) throw ApiException.badRequest("Amount must be positive")
        return repo.create(UUID.fromString(userId), req)
    }

    fun update(userId: String, id: String, req: UpdateIncomeRequest): IncomeDto {
        req.amount?.let { if (it <= 0) throw ApiException.badRequest("Amount must be positive") }
        return repo.update(UUID.fromString(id), UUID.fromString(userId), req)
    }

    fun delete(userId: String, id: String) = repo.delete(UUID.fromString(id), UUID.fromString(userId))
}
