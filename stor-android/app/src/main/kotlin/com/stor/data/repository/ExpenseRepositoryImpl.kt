package com.stor.data.repository

import com.stor.data.local.dao.ExpenseDao
import com.stor.data.local.entities.ExpenseEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateExpenseRequest
import com.stor.domain.model.Expense
import com.stor.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { it.map { entity -> entity.toDomain() } }

    override suspend fun getExpenseById(id: String): Expense? =
        dao.getExpenseById(id)?.toDomain()

    override suspend fun createExpense(expense: Expense): Result<Expense> = runCatching {
        val response = api.createExpense(
            CreateExpenseRequest(
                title = expense.title,
                description = expense.description,
                amount = expense.amount,
                category = expense.category,
                paymentMethod = expense.paymentMethod,
                date = expense.date,
                notes = expense.notes
            )
        )
        val body = response.body() ?: error("Empty response")
        dao.insertExpense(body.toEntity())
        body.toDomain()
    }

    override suspend fun updateExpense(expense: Expense): Result<Expense> = runCatching {
        val response = api.updateExpense(
            expense.id,
            CreateExpenseRequest(
                title = expense.title,
                description = expense.description,
                amount = expense.amount,
                category = expense.category,
                paymentMethod = expense.paymentMethod,
                date = expense.date,
                notes = expense.notes
            )
        )
        val body = response.body() ?: error("Empty response")
        dao.insertExpense(body.toEntity())
        body.toDomain()
    }

    override suspend fun deleteExpense(id: String): Result<Unit> = runCatching {
        api.deleteExpense(id)
        dao.deleteExpenseById(id)
    }

    override suspend fun syncExpenses(): Result<Unit> = runCatching {
        val response = api.getExpenses()
        val expenses = response.body() ?: error("Sync failed")
        dao.clearAll()
        dao.insertExpenses(expenses.map { it.toEntity() })
    }
}

// Mappers
fun ExpenseEntity.toDomain() = Expense(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt
)

fun com.stor.data.remote.dto.ExpenseDto.toDomain() = Expense(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt
)

fun com.stor.data.remote.dto.ExpenseDto.toEntity() = ExpenseEntity(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt
)
