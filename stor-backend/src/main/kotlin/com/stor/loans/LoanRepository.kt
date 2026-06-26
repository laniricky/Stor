package com.stor.loans

import com.stor.common.ApiException
import com.stor.loans.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class LoanRepository {

    fun findAll(userId: UUID, status: String? = null): LoanListResponse = transaction {
        var query = LoansTable.select { LoansTable.userId eq userId }
        if (status != null) query = query.andWhere { LoansTable.status eq status }

        val loans = query.orderBy(LoansTable.createdAt, SortOrder.DESC).map { it.toDto() }
        val totalOutstanding = loans.filter { it.status == "active" }.sumOf { it.remainingBalance }
        val active = loans.count { it.status == "active" }

        LoanListResponse(loans = loans, totalOutstanding = totalOutstanding, activeLoanCount = active)
    }

    fun findById(id: UUID, userId: UUID): LoanDto = transaction {
        LoansTable.select { (LoansTable.id eq id) and (LoansTable.userId eq userId) }
            .singleOrNull()?.toDto() ?: throw ApiException.notFound("Loan not found")
    }

    fun create(userId: UUID, req: CreateLoanRequest): LoanDto = transaction {
        val insertStatement = LoansTable.insert {
            it[this.userId] = userId
            it[name] = req.name.trim()
            it[lender] = req.lender.trim()
            it[originalAmount] = BigDecimal.valueOf(req.originalAmount)
            it[remainingBalance] = BigDecimal.valueOf(req.originalAmount)
            it[interestRate] = req.interestRate?.let { v -> BigDecimal.valueOf(v) }
            it[monthlyPayment] = req.monthlyPayment?.let { v -> BigDecimal.valueOf(v) }
            it[dueDay] = req.dueDay
            it[startDate] = LocalDate.parse(req.startDate)
            it[endDate] = req.endDate?.let { v -> LocalDate.parse(v) }
        }
        val newId = insertStatement[LoansTable.id]
        LoansTable.select { LoansTable.id eq newId }.single().toDto()
    }

    fun update(id: UUID, userId: UUID, req: UpdateLoanRequest): LoanDto = transaction {
        LoansTable.select { (LoansTable.id eq id) and (LoansTable.userId eq userId) }
            .singleOrNull() ?: throw ApiException.notFound("Loan not found")
        LoansTable.update({ (LoansTable.id eq id) and (LoansTable.userId eq userId) }) {
            req.name?.let { v -> it[name] = v.trim() }
            req.lender?.let { v -> it[lender] = v.trim() }
            req.interestRate?.let { v -> it[interestRate] = BigDecimal.valueOf(v) }
            req.monthlyPayment?.let { v -> it[monthlyPayment] = BigDecimal.valueOf(v) }
            req.dueDay?.let { v -> it[dueDay] = v }
            req.endDate?.let { v -> it[endDate] = LocalDate.parse(v) }
            req.status?.let { v -> it[status] = v }
        }
        LoansTable.select { LoansTable.id eq id }.single().toDto()
    }

    fun delete(id: UUID, userId: UUID) = transaction {
        val count = LoansTable.deleteWhere { (LoansTable.id eq id) and (LoansTable.userId eq userId) }
        if (count == 0) throw ApiException.notFound("Loan not found")
    }

    fun reduceBalance(loanId: UUID, amount: BigDecimal) = transaction {
        LoansTable.update({ LoansTable.id eq loanId }) {
            with(SqlExpressionBuilder) {
                it.update(remainingBalance, remainingBalance - amount)
            }
        }
        // Auto-archive if balance reaches zero
        val loan = LoansTable.select { LoansTable.id eq loanId }.single()
        if (loan[LoansTable.remainingBalance] <= BigDecimal.ZERO) {
            LoansTable.update({ LoansTable.id eq loanId }) {
                it[remainingBalance] = BigDecimal.ZERO
                it[status] = "archived"
            }
        }
    }

    fun findForSearch(userId: UUID, query: String): List<LoanDto> = transaction {
        LoansTable.select { (LoansTable.userId eq userId) and (LoansTable.name like "%$query%") }
            .map { it.toDto() }
    }

    fun upcomingPayments(userId: UUID): List<LoanDto> = transaction {
        val today = LocalDate.now()
        val nextWeek = today.plusDays(7)
        LoansTable.select {
            (LoansTable.userId eq userId) and
            (LoansTable.status eq "active") and
            (LoansTable.dueDay.isNotNull())
        }
            .map { it.toDto() }
            .filter { loan ->
                val dueDay = loan.dueDay ?: return@filter false
                val thisMonthDue = LocalDate.of(today.year, today.month, minOf(dueDay, today.month.length(today.isLeapYear)))
                thisMonthDue in today..nextWeek
            }
    }

    private fun ResultRow.toDto(): LoanDto {
        val original = this[LoansTable.originalAmount].toDouble()
        val remaining = this[LoansTable.remainingBalance].toDouble()
        val paid = original - remaining
        val pct = if (original > 0) (paid / original * 100).coerceIn(0.0, 100.0) else 0.0
        return LoanDto(
            id = this[LoansTable.id].toString(),
            name = this[LoansTable.name],
            lender = this[LoansTable.lender],
            originalAmount = original,
            remainingBalance = remaining,
            interestRate = this[LoansTable.interestRate]?.toDouble(),
            monthlyPayment = this[LoansTable.monthlyPayment]?.toDouble(),
            dueDay = this[LoansTable.dueDay],
            startDate = this[LoansTable.startDate].toString(),
            endDate = this[LoansTable.endDate]?.toString(),
            status = this[LoansTable.status],
            percentagePaid = pct,
            createdAt = this[LoansTable.createdAt].toString()
        )
    }
}

private val LoansTable = com.stor.loans.models.LoansTable
