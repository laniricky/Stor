package com.stor.repayments

import com.stor.common.ApiException
import com.stor.loans.LoanRepository
import com.stor.loans.models.LoansTable
import com.stor.repayments.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class RepaymentRepository(private val loanRepo: LoanRepository = LoanRepository()) {

    fun findByLoan(loanId: UUID, userId: UUID): RepaymentListResponse = transaction {
        // Verify loan belongs to user
        val loan = LoansTable.selectAll().where { (LoansTable.id eq loanId) and (LoansTable.userId eq userId) }
            .singleOrNull() ?: throw ApiException.notFound("Loan not found")

        val repayments = RepaymentsTable.selectAll().where { RepaymentsTable.loanId eq loanId }
            .orderBy(RepaymentsTable.date, SortOrder.DESC)
            .map { it.toDto() }

        val totalPaid = repayments.sumOf { it.amountPaid }
        val originalAmount = loan[LoansTable.originalAmount].toDouble()
        val totalRemaining = loan[LoansTable.remainingBalance].toDouble()

        RepaymentListResponse(repayments = repayments, totalPaid = totalPaid, totalRemaining = totalRemaining)
    }

    fun create(loanId: UUID, userId: UUID, req: CreateRepaymentRequest): RepaymentDto = transaction {
        // Verify loan belongs to user and is active
        val loan = LoansTable.selectAll().where { (LoansTable.id eq loanId) and (LoansTable.userId eq userId) }
            .singleOrNull() ?: throw ApiException.notFound("Loan not found")

        if (loan[LoansTable.status] == "archived") {
            throw ApiException.badRequest("Cannot add repayment to archived loan")
        }

        val payAmount = BigDecimal.valueOf(req.amountPaid)
        val insertStatement = RepaymentsTable.insert {
            it[this.loanId] = loanId
            it[amountPaid] = payAmount
            it[date] = LocalDate.parse(req.date)
            it[notes] = req.notes?.trim()
        }
        val newId = insertStatement[RepaymentsTable.id]

        // Reduce loan balance
        loanRepo.reduceBalance(loanId, payAmount)

        RepaymentsTable.selectAll().where { RepaymentsTable.id eq newId }.single().toDto()
    }

    fun findForSearch(userId: UUID, query: String): List<RepaymentDto> = transaction {
        // Join repayments with loans to scope by userId
        (RepaymentsTable innerJoin LoansTable)
            .selectAll().where {
                (LoansTable.userId eq userId) and
                (RepaymentsTable.notes like "%$query%")
            }
            .orderBy(RepaymentsTable.date, SortOrder.DESC)
            .limit(20)
            .map { it.toDto() }
    }

    private fun ResultRow.toDto() = RepaymentDto(
        id = this[RepaymentsTable.id].toString(),
        loanId = this[RepaymentsTable.loanId].toString(),
        amountPaid = this[RepaymentsTable.amountPaid].toDouble(),
        date = this[RepaymentsTable.date].toString(),
        notes = this[RepaymentsTable.notes],
        createdAt = this[RepaymentsTable.createdAt].toString()
    )
}

private val RepaymentsTable = com.stor.repayments.models.RepaymentsTable
private val LoansTable = com.stor.loans.models.LoansTable

