package com.stor.data.repository

import com.stor.data.local.dao.ExpenseDao
import com.stor.data.local.entities.ExpenseEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateExpenseRequest
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.Expense
import com.stor.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.stor.data.sync.SyncWorker

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: StorApi,
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { it.map { entity -> entity.toDomain() } }

    override suspend fun getExpenseById(id: String): Expense? =
        dao.getExpenseById(id)?.toDomain()

    override suspend fun createExpense(expense: Expense): Result<Expense> = runCatching {
        try {
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
            if (!response.isSuccessful) throw Exception(response.getErrorMessage())
            val body = response.body() ?: error("Empty response")
            dao.insertExpense(body.toEntity(isSynced = true))
            body.toDomain()
        } catch (e: IOException) {
            // Offline — save locally with a temporary ID
            val localId = "LOCAL_${UUID.randomUUID()}"
            val localEntity = ExpenseEntity(
                id = localId,
                title = expense.title,
                description = expense.description,
                amount = expense.amount,
                category = expense.category,
                paymentMethod = expense.paymentMethod,
                date = expense.date,
                notes = expense.notes,
                createdAt = expense.date, // use date as placeholder
                isSynced = false
            )
            dao.insertExpense(localEntity)
            SyncWorker.enqueue(context)
            localEntity.toDomain()
        }
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
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertExpense(body.toEntity(isSynced = true))
        body.toDomain()
    }

    override suspend fun deleteExpense(id: String): Result<Unit> = runCatching {
        if (id.startsWith("LOCAL_")) {
            dao.deleteExpenseById(id)
            return@runCatching
        }
        val response = api.deleteExpense(id)
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        dao.deleteExpenseById(id)
    }

    override suspend fun syncExpenses(): Result<Unit> = runCatching {
        try {
            // 1. Push any locally-created (unsynced) expenses to the server
            val unsynced = dao.getUnsynced()
            for (entity in unsynced) {
                try {
                    val response = api.createExpense(
                        CreateExpenseRequest(
                            title = entity.title,
                            description = entity.description,
                            amount = entity.amount,
                            category = entity.category,
                            paymentMethod = entity.paymentMethod,
                            date = entity.date,
                            notes = entity.notes
                        )
                    )
                    if (response.isSuccessful) {
                        // Replace temp local record with server-assigned record
                        dao.hardDelete(entity.id)
                        response.body()?.let { dao.insertExpense(it.toEntity(isSynced = true)) }
                    }
                } catch (_: IOException) { /* stay offline, retry next time */ }
            }

            // 2. Full refresh from server
            val response = api.getExpenses()
            if (!response.isSuccessful) throw Exception(response.getErrorMessage())
            val expenses = response.body()?.expenses ?: error("Sync failed")
            // Remove only synced records before replacing (preserve any still-pending local ones)
            val stillUnsynced = dao.getUnsynced().map { it.id }.toSet()
            dao.getAllExpensesList().filter { it.id !in stillUnsynced }.forEach { dao.hardDelete(it.id) }
            dao.insertExpenses(expenses.map { it.toEntity(isSynced = true) })
        } catch (e: Exception) {
            Log.e("SYNC_ERROR", "syncExpenses failed", e)
            throw e
        }
    }
}

// Mappers
fun ExpenseEntity.toDomain() = Expense(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt, isSynced = isSynced
)

fun com.stor.data.remote.dto.ExpenseDto.toDomain() = Expense(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt, isSynced = true
)

fun com.stor.data.remote.dto.ExpenseDto.toEntity(isSynced: Boolean = true) = ExpenseEntity(
    id = id, title = title, description = description,
    amount = amount, category = category, paymentMethod = paymentMethod,
    date = date, notes = notes, createdAt = createdAt, isSynced = isSynced
)
