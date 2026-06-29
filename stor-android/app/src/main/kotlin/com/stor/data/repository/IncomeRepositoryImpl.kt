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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val dao: IncomeDao
) : IncomeRepository {

    override fun getIncome(): Flow<List<Income>> =
        dao.getAllIncome().map { it.map { entity -> entity.toDomain() } }

    override suspend fun getIncomeById(id: String): Income? =
        dao.getIncomeById(id)?.toDomain()

    override suspend fun createIncome(income: Income): Result<Income> = runCatching {
        val response = api.createIncome(
            CreateIncomeRequest(source = income.source, amount = income.amount, date = income.date, notes = income.notes)
        )
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertIncome(body.toEntity())
        body.toDomain()
    }

    override suspend fun updateIncome(income: Income): Result<Income> = runCatching {
        val response = api.updateIncome(
            income.id,
            CreateIncomeRequest(source = income.source, amount = income.amount, date = income.date, notes = income.notes)
        )
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertIncome(body.toEntity())
        body.toDomain()
    }

    override suspend fun deleteIncome(id: String): Result<Unit> = runCatching {
        val response = api.deleteIncome(id)
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        dao.deleteIncomeById(id)
    }

    override suspend fun syncIncome(): Result<Unit> = runCatching {
        val response = api.getIncome()
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val items = response.body() ?: error("Sync failed")
        dao.clearAll()
        dao.insertIncomes(items.map { it.toEntity() })
    }
}

fun IncomeEntity.toDomain() = Income(id, source, amount, date, notes, createdAt)

fun com.stor.data.remote.dto.IncomeDto.toDomain() = Income(id, source, amount, date, notes, createdAt)

fun com.stor.data.remote.dto.IncomeDto.toEntity() = IncomeEntity(id, source, amount, date, notes, createdAt)
