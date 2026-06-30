package com.stor.data.repository

import com.stor.data.local.dao.RepaymentDao
import com.stor.data.local.entities.RepaymentEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateRepaymentRequest
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.Repayment
import com.stor.domain.repository.RepaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepaymentRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val dao: RepaymentDao
) : RepaymentRepository {

    override fun getRepayments(loanId: String): Flow<List<Repayment>> =
        dao.getRepaymentsForLoan(loanId).map { it.map { e -> e.toDomain() } }

    override suspend fun createRepayment(loanId: String, repayment: Repayment): Result<Repayment> = runCatching {
        val response = api.createRepayment(
            loanId,
            CreateRepaymentRequest(amountPaid = repayment.amountPaid, date = repayment.date, notes = repayment.notes)
        )
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertRepayment(body.toEntity())
        body.toDomain()
    }

    override suspend fun syncRepayments(loanId: String): Result<Unit> = runCatching {
        val response = api.getRepayments(loanId)
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val repaymentsList = response.body()?.repayments ?: error("Sync failed")
        dao.insertRepayments(repaymentsList.map { it.toEntity() })
    }
}

fun RepaymentEntity.toDomain() = Repayment(id, loanId, amountPaid, date, notes, createdAt)

fun com.stor.data.remote.dto.RepaymentDto.toDomain() = Repayment(id, loanId, amountPaid, date, notes, createdAt)

fun com.stor.data.remote.dto.RepaymentDto.toEntity() = RepaymentEntity(id, loanId, amountPaid, date, notes, createdAt)
