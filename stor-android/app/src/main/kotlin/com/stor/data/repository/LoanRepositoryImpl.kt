package com.stor.data.repository

import com.stor.data.local.dao.LoanDao
import com.stor.data.local.dao.RepaymentDao
import com.stor.data.local.entities.LoanEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateLoanRequest
import com.stor.data.remote.api.getErrorMessage
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
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
class LoanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: StorApi,
    private val dao: LoanDao,
    private val repaymentDao: RepaymentDao
) : LoanRepository {

    override fun getLoans(): Flow<List<Loan>> =
        dao.getAllLoans().map { list -> 
            list.map { e -> 
                val totalRepaid = repaymentDao.getTotalRepaidForLoan(e.id) ?: 0.0
                val domain = e.toDomain()
                domain.copy(remainingBalance = maxOf(0.0, domain.originalAmount - totalRepaid))
            } 
        }

    override fun getActiveLoans(): Flow<List<Loan>> =
        dao.getActiveLoans().map { list -> 
            list.map { e -> 
                val totalRepaid = repaymentDao.getTotalRepaidForLoan(e.id) ?: 0.0
                val domain = e.toDomain()
                domain.copy(remainingBalance = maxOf(0.0, domain.originalAmount - totalRepaid))
            } 
        }

    override suspend fun getLoanById(id: String): Loan? {
        val entity = dao.getLoanById(id) ?: return null
        val totalRepaid = repaymentDao.getTotalRepaidForLoan(id) ?: 0.0
        val domain = entity.toDomain()
        return domain.copy(remainingBalance = maxOf(0.0, domain.originalAmount - totalRepaid))
    }

    override suspend fun createLoan(loan: Loan): Result<Loan> = runCatching {
        try {
            val response = api.createLoan(
                CreateLoanRequest(
                    name = loan.name, lender = loan.lender,
                    originalAmount = loan.originalAmount,
                    interestRate = loan.interestRate, monthlyPayment = loan.monthlyPayment,
                    dueDay = loan.dueDay, startDate = loan.startDate, endDate = loan.endDate
                )
            )
            if (!response.isSuccessful) throw Exception(response.getErrorMessage())
            val body = response.body() ?: error("Empty response")
            dao.insertLoan(body.toEntity(isSynced = true))
            body.toDomain()
        } catch (e: IOException) {
            val localId = "LOCAL_${UUID.randomUUID()}"
            val localEntity = LoanEntity(
                id = localId,
                name = loan.name,
                lender = loan.lender,
                originalAmount = loan.originalAmount,
                remainingBalance = loan.originalAmount,
                interestRate = loan.interestRate,
                monthlyPayment = loan.monthlyPayment,
                dueDay = loan.dueDay,
                startDate = loan.startDate,
                endDate = loan.endDate,
                status = "active",
                percentagePaid = 0.0,
                createdAt = loan.startDate,
                isSynced = false
            )
            dao.insertLoan(localEntity)
            SyncWorker.enqueue(context)
            localEntity.toDomain()
        }
    }

    override suspend fun updateLoan(loan: Loan): Result<Loan> = runCatching {
        val response = api.updateLoan(
            loan.id,
            CreateLoanRequest(
                name = loan.name, lender = loan.lender,
                originalAmount = loan.originalAmount,
                interestRate = loan.interestRate, monthlyPayment = loan.monthlyPayment,
                dueDay = loan.dueDay, startDate = loan.startDate, endDate = loan.endDate
            )
        )
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val body = response.body() ?: error("Empty response")
        dao.insertLoan(body.toEntity(isSynced = true))
        body.toDomain()
    }

    override suspend fun deleteLoan(id: String): Result<Unit> = runCatching {
        if (id.startsWith("LOCAL_")) {
            dao.deleteLoanById(id)
            return@runCatching
        }
        val response = api.deleteLoan(id)
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        dao.deleteLoanById(id)
    }

    override suspend fun syncLoans(): Result<Unit> = runCatching {
        // 1. Push unsynced local loans first
        val unsynced = dao.getUnsynced()
        for (entity in unsynced) {
            try {
                val response = api.createLoan(
                    CreateLoanRequest(
                        name = entity.name, lender = entity.lender,
                        originalAmount = entity.originalAmount,
                        interestRate = entity.interestRate, monthlyPayment = entity.monthlyPayment,
                        dueDay = entity.dueDay, startDate = entity.startDate, endDate = entity.endDate
                    )
                )
                if (response.isSuccessful) {
                    dao.hardDelete(entity.id)
                    response.body()?.let { dao.insertLoan(it.toEntity(isSynced = true)) }
                }
            } catch (_: IOException) { }
        }

        // 2. Full refresh
        val response = api.getLoans()
        if (!response.isSuccessful) throw Exception(response.getErrorMessage())
        val loansList = response.body()?.loans ?: error("Sync failed")
        
        val stillUnsynced = dao.getUnsynced().map { it.id }.toSet()
        dao.getAllLoansList().filter { it.id !in stillUnsynced }.forEach { dao.hardDelete(it.id) }
        dao.insertLoans(loansList.map { it.toEntity(isSynced = true) })
    }
}

fun LoanEntity.toDomain() = Loan(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment,
    dueDay = dueDay, startDate = startDate, endDate = endDate,
    status = status, createdAt = createdAt, isSynced = isSynced
)

fun com.stor.data.remote.dto.LoanDto.toDomain() = Loan(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment,
    dueDay = dueDay, startDate = startDate, endDate = endDate,
    status = status, createdAt = createdAt, isSynced = true
)

fun com.stor.data.remote.dto.LoanDto.toEntity(isSynced: Boolean = true) = LoanEntity(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment, dueDay = dueDay,
    startDate = startDate, endDate = endDate, status = status,
    percentagePaid = if (originalAmount > 0) ((originalAmount - remainingBalance) / originalAmount) * 100 else 0.0,
    createdAt = createdAt, isSynced = isSynced
)
