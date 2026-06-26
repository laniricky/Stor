package com.stor.expenses

import com.stor.common.ApiException
import com.stor.expenses.models.*
import java.time.LocalDate
import java.util.UUID

class ExpenseService(private val repo: ExpenseRepository = ExpenseRepository()) {

    fun listExpenses(
        userId: String,
        category: String?,
        month: Int?,
        year: Int?,
        search: String?,
        page: Int,
        pageSize: Int
    ): ExpenseListResponse {
        val uuid = UUID.fromString(userId)
        val now = LocalDate.now()
        val m = month ?: now.monthValue
        val y = year ?: now.year

        val (expenses, total) = repo.findAll(uuid, category, m, y, search, page, pageSize)
        val monthlyTotal = repo.monthlyTotal(uuid, m, y)

        return ExpenseListResponse(expenses = expenses, total = total, monthlyTotal = monthlyTotal)
    }

    fun getExpense(userId: String, expenseId: String): ExpenseDto {
        return repo.findById(UUID.fromString(expenseId), UUID.fromString(userId))
    }

    fun createExpense(userId: String, req: CreateExpenseRequest): ExpenseDto {
        if (req.title.isBlank()) throw ApiException.badRequest("Title is required")
        if (req.amount <= 0) throw ApiException.badRequest("Amount must be positive")
        if (req.category !in VALID_CATEGORIES) throw ApiException.badRequest("Invalid category")
        return repo.create(UUID.fromString(userId), req)
    }

    fun updateExpense(userId: String, expenseId: String, req: UpdateExpenseRequest): ExpenseDto {
        req.amount?.let { if (it <= 0) throw ApiException.badRequest("Amount must be positive") }
        req.category?.let { if (it !in VALID_CATEGORIES) throw ApiException.badRequest("Invalid category") }
        return repo.update(UUID.fromString(expenseId), UUID.fromString(userId), req)
    }

    fun deleteExpense(userId: String, expenseId: String) {
        repo.delete(UUID.fromString(expenseId), UUID.fromString(userId))
    }
}
