package com.stor.data.repository

import com.stor.data.local.dao.LoanDao
import com.stor.data.local.entities.LoanEntity
import com.stor.data.remote.api.StorApi
import com.stor.data.remote.dto.CreateLoanRequest
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepositoryImpl @Inject constructor(
    private val api: StorApi,
    private val dao: LoanDao
) : LoanRepository {

    override fun getLoans(): Flow<List<Loan>> =
        dao.getAllLoans().map { it.map { e -> e.toDomain() } }

    override fun getActiveLoans(): Flow<List<Loan>> =
        dao.getActiveLoans().map { it.map { e -> e.toDomain() } }

    override suspend fun getLoanById(id: String): Loan? = dao.getLoanById(id)?.toDomain()

    override suspend fun createLoan(loan: Loan): Result<Loan> = runCatching {
        val response = api.createLoan(
            CreateLoanRequest(
                name = loan.name, lender = loan.lender,
                originalAmount = loan.originalAmount,
                interestRate = loan.interestRate, monthlyPayment = loan.monthlyPayment,
                dueDay = loan.dueDay, startDate = loan.startDate, endDate = loan.endDate
            )
        )
        val body = response.body() ?: error("Empty response")
        dao.insertLoan(body.toEntity())
        body.toDomain()
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
        val body = response.body() ?: error("Empty response")
        dao.insertLoan(body.toEntity())
        body.toDomain()
    }

    override suspend fun deleteLoan(id: String): Result<Unit> = runCatching {
        api.deleteLoan(id)
        dao.deleteLoanById(id)
    }

    override suspend fun syncLoans(): Result<Unit> = runCatching {
        val response = api.getLoans()
        val loans = response.body() ?: error("Sync failed")
        dao.clearAll()
        dao.insertLoans(loans.map { it.toEntity() })
    }
}

fun LoanEntity.toDomain() = Loan(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment,
    dueDay = dueDay, startDate = startDate, endDate = endDate,
    status = status, createdAt = createdAt
)

fun com.stor.data.remote.dto.LoanDto.toDomain() = Loan(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment,
    dueDay = dueDay, startDate = startDate, endDate = endDate,
    status = status, createdAt = createdAt
)

fun com.stor.data.remote.dto.LoanDto.toEntity() = LoanEntity(
    id = id, name = name, lender = lender,
    originalAmount = originalAmount, remainingBalance = remainingBalance,
    interestRate = interestRate, monthlyPayment = monthlyPayment, dueDay = dueDay,
    startDate = startDate, endDate = endDate, status = status,
    percentagePaid = if (originalAmount > 0) ((originalAmount - remainingBalance) / originalAmount) * 100 else 0.0,
    createdAt = createdAt
)
