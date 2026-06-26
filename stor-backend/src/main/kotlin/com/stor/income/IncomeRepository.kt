package com.stor.income

import com.stor.common.ApiException
import com.stor.income.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class IncomeRepository {

    fun findAll(userId: UUID, month: Int? = null, year: Int? = null): Pair<List<IncomeDto>, Int> = transaction {
        var query = IncomeTable.selectAll().where { IncomeTable.userId eq userId }
        if (month != null && year != null) {
            val start = LocalDate.of(year, month, 1)
            val end = start.plusMonths(1).minusDays(1)
            query = query.andWhere { IncomeTable.date.between(start, end) }
        }
        val total = query.count().toInt()
        val list = query.orderBy(IncomeTable.date, SortOrder.DESC).map { it.toDto() }
        Pair(list, total)
    }

    fun monthlyTotal(userId: UUID, month: Int, year: Int): Double = transaction {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        IncomeTable.select(IncomeTable.amount.sum())
            .where { (IncomeTable.userId eq userId) and IncomeTable.date.between(start, end) }
            .singleOrNull()?.get(IncomeTable.amount.sum())?.toDouble() ?: 0.0
    }

    fun annualTotal(userId: UUID, year: Int): Double = transaction {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        IncomeTable.select(IncomeTable.amount.sum())
            .where { (IncomeTable.userId eq userId) and IncomeTable.date.between(start, end) }
            .singleOrNull()?.get(IncomeTable.amount.sum())?.toDouble() ?: 0.0
    }

    fun findById(id: UUID, userId: UUID): IncomeDto = transaction {
        IncomeTable.selectAll()
            .where { (IncomeTable.id eq id) and (IncomeTable.userId eq userId) }
            .singleOrNull()?.toDto() ?: throw ApiException.notFound("Income record not found")
    }

    fun create(userId: UUID, req: CreateIncomeRequest): IncomeDto = transaction {
        val insertStatement = IncomeTable.insert {
            it[this.userId] = userId
            it[source] = req.source.trim()
            it[amount] = BigDecimal.valueOf(req.amount)
            it[date] = LocalDate.parse(req.date)
            it[notes] = req.notes?.trim()
        }
        val newId = insertStatement[IncomeTable.id]
        IncomeTable.selectAll().where { IncomeTable.id eq newId }.single().toDto()
    }

    fun update(id: UUID, userId: UUID, req: UpdateIncomeRequest): IncomeDto = transaction {
        IncomeTable.selectAll()
            .where { (IncomeTable.id eq id) and (IncomeTable.userId eq userId) }
            .singleOrNull() ?: throw ApiException.notFound("Income record not found")
        IncomeTable.update({ (IncomeTable.id eq id) and (IncomeTable.userId eq userId) }) {
            req.source?.let { v -> it[source] = v.trim() }
            req.amount?.let { v -> it[amount] = BigDecimal.valueOf(v) }
            req.date?.let { v -> it[date] = LocalDate.parse(v) }
            req.notes?.let { v -> it[notes] = v }
        }
        IncomeTable.selectAll().where { IncomeTable.id eq id }.single().toDto()
    }

    fun delete(id: UUID, userId: UUID) = transaction {
        val count = IncomeTable.deleteWhere { (IncomeTable.id eq id) and (IncomeTable.userId eq userId) }
        if (count == 0) throw ApiException.notFound("Income record not found")
    }

    fun findForSearch(userId: UUID, query: String): List<IncomeDto> = transaction {
        IncomeTable.selectAll()
            .where { (IncomeTable.userId eq userId) and (IncomeTable.source like "%$query%") }
            .orderBy(IncomeTable.date, SortOrder.DESC).limit(20).map { it.toDto() }
    }

    private fun ResultRow.toDto() = IncomeDto(
        id = this[IncomeTable.id].toString(),
        source = this[IncomeTable.source],
        amount = this[IncomeTable.amount].toDouble(),
        date = this[IncomeTable.date].toString(),
        notes = this[IncomeTable.notes],
        createdAt = this[IncomeTable.createdAt].toString()
    )
}

private val IncomeTable = com.stor.income.models.IncomeTable
