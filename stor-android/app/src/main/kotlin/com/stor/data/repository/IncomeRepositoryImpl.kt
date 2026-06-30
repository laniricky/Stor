package com.stor.data.repository

import com.stor.data.local.dao.IncomeDao
import com.stor.data.local.entities.IncomeEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateIncomeRequest
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.Income
import com.stor.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.stor.data.sync.SyncWorker

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: StorApi,
    private val dao: IncomeDao
) : IncomeRepository {

    override fun getIncome(): Flow<List<Income>> =
        dao.getAllIncome().map { it.map { entity -> entity.toDomain() } }

    override suspend fun getIncomeById(id: String): Income? =
        dao.getIncomeById(id)?.toDomain()

    override suspend fun createIncome(income: Income): Result<Income> = runCatching {
        try {
            val response = api.createIncome(
                CreateIncomeRequest(source = income.source, amount = income.amount, date = income.date, notes = income.notes)
            )
            if (!response.isSuccessful) throw Exception(response.getErrorMessage())
            val body = response.body() ?: error("Empty response")
            dao.insertIncome(body.toEntity(isSynced = true))
            body.toDomain()
        } catch (e: IOException) {
            val localId = "LOCAL_${UUID.randomUUID()}"
            val localEntity = IncomeEntity(
                id = localId,
                source = income.source,
                amount = income.amount,
                date = income.date,
                notes = income.notes,
                createdAt = income.date,
                isSynced = false
            )
            dao.insertIncome(localEntity)
            SyncWorker.enqueue(context)
            localEntity.toDomain()
        }
    }

    override suspend fun updateIncome(income: Income): Result<Income> = runCatching {
        val response = api.updateIncome(
            income.id,
            CreateIncomeRequest(source = income.source, amount = income.amount, date = income.date, notes = income.notes)
        )
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertIncome(body.toEntity(isSynced = true))
        body.toDomain()
    }

    override suspend fun deleteIncome(id: String): Result<Unit> = runCatching {
        val response = api.deleteIncome(id)
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        dao.deleteIncomeById(id)
    }

    override suspend fun syncIncome(): Result<Unit> = runCatching {
        // 1. Push unsynced local records first
        val unsynced = dao.getUnsynced()
        for (entity in unsynced) {
            try {
                val response = api.createIncome(
                    CreateIncomeRequest(source = entity.source, amount = entity.amount, date = entity.date, notes = entity.notes)
                )
                if (response.isSuccessful) {
                    dao.hardDelete(entity.id)
                    response.body()?.let { dao.insertIncome(it.toEntity(isSynced = true)) }
                }
            } catch (_: IOException) { }
        }

        // 2. Full refresh
        val response = api.getIncome()
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val items = response.body() ?: error("Sync failed")
        val stillUnsynced = dao.getUnsynced().map { it.id }.toSet()
        dao.getAllIncomeList().filter { it.id !in stillUnsynced }.forEach { dao.hardDelete(it.id) }
        dao.insertIncomes(items.map { it.toEntity(isSynced = true) })
    }
}

fun IncomeEntity.toDomain() = Income(id, source, amount, date, notes, createdAt, isSynced)

fun com.stor.data.remote.dto.IncomeDto.toDomain() = Income(id, source, amount, date, notes, createdAt, isSynced = true)

fun com.stor.data.remote.dto.IncomeDto.toEntity(isSynced: Boolean = true) = IncomeEntity(id, source, amount, date, notes, createdAt, isSynced)
