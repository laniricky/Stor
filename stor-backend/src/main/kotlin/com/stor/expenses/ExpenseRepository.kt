package com.stor.expenses

import com.stor.common.ApiException
import com.stor.expenses.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

class ExpenseRepository {

    fun findAll(
        userId: UUID,
        category: String? = null,
        month: Int? = null,
        year: Int? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 50
    ): Pair<List<ExpenseDto>, Int> = transaction {
        var query = ExpensesTable.selectAll().where { ExpensesTable.userId eq userId }

        if (!category.isNullOrBlank()) {
            query = query.andWhere { ExpensesTable.category eq category }
        }
        if (month != null && year != null) {
            val start = LocalDate.of(year, month, 1)
            val end = start.plusMonths(1).minusDays(1)
            query = query.andWhere {
                ExpensesTable.date.between(start, end)
            }
        }
        if (!search.isNullOrBlank()) {
            query = query.andWhere {
                (ExpensesTable.title like "%$search%") or (ExpensesTable.notes like "%$search%")
            }
        }

        val total = query.count().toInt()
        val expenses = query
            .orderBy(ExpensesTable.date, SortOrder.DESC)
            .limit(pageSize).offset(((page - 1) * pageSize).toLong())
            .map { it.toExpenseDto() }

        Pair(expenses, total)
    }

    fun findById(id: UUID, userId: UUID): ExpenseDto = transaction {
        ExpensesTable.selectAll()
            .where { (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) }
            .singleOrNull()
            ?.toExpenseDto()
            ?: throw ApiException.notFound("Expense not found")
    }

    fun monthlyTotal(userId: UUID, month: Int, year: Int): Double = transaction {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        ExpensesTable
            .select(ExpensesTable.amount.sum())
            .where {
                (ExpensesTable.userId eq userId) and
                ExpensesTable.date.between(start, end)
            }
            .singleOrNull()
            ?.get(ExpensesTable.amount.sum())
            ?.toDouble() ?: 0.0
    }

    fun create(userId: UUID, req: CreateExpenseRequest): ExpenseDto = transaction {
        val date = parseDate(req.date)
        val insertStatement = ExpensesTable.insert {
            it[this.userId] = userId
            it[title] = req.title.trim()
            it[description] = req.description?.trim()
            it[amount] = BigDecimal.valueOf(req.amount)
            it[category] = req.category
            it[paymentMethod] = req.paymentMethod
            it[this.date] = date
            it[notes] = req.notes?.trim()
        }
        val newId = insertStatement[ExpensesTable.id]
        ExpensesTable.selectAll()
            .where { ExpensesTable.id eq newId }
            .single().toExpenseDto()
    }

    fun update(id: UUID, userId: UUID, req: UpdateExpenseRequest): ExpenseDto = transaction {
        ExpensesTable.selectAll()
            .where { (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) }
            .singleOrNull() ?: throw ApiException.notFound("Expense not found")

        ExpensesTable.update({ (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId) }) {
            req.title?.let { v -> it[title] = v.trim() }
            req.description?.let { v -> it[description] = v.trim() }
            req.amount?.let { v -> it[amount] = BigDecimal.valueOf(v) }
            req.category?.let { v -> it[category] = v }
            req.paymentMethod?.let { v -> it[paymentMethod] = v }
            req.date?.let { v -> it[date] = parseDate(v) }
            req.notes?.let { v -> it[notes] = v.trim() }
        }

        ExpensesTable.selectAll().where { ExpensesTable.id eq id }.single().toExpenseDto()
    }

    fun delete(id: UUID, userId: UUID) = transaction {
        val count = ExpensesTable.deleteWhere {
            (ExpensesTable.id eq id) and (ExpensesTable.userId eq userId)
        }
        if (count == 0) throw ApiException.notFound("Expense not found")
    }

    fun sumByCategory(userId: UUID, month: Int, year: Int): Map<String, Double> = transaction {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        ExpensesTable
            .select(ExpensesTable.category, ExpensesTable.amount.sum())
            .where {
                (ExpensesTable.userId eq userId) and
                ExpensesTable.date.between(start, end)
            }
            .groupBy(ExpensesTable.category)
            .associate {
                it[ExpensesTable.category] to (it[ExpensesTable.amount.sum()]?.toDouble() ?: 0.0)
            }
    }

    fun findForSearch(userId: UUID, query: String): List<ExpenseDto> = transaction {
        ExpensesTable.selectAll()
            .where {
                (ExpensesTable.userId eq userId) and
                ((ExpensesTable.title like "%$query%") or (ExpensesTable.category like "%$query%"))
            }
            .orderBy(ExpensesTable.date, SortOrder.DESC)
            .limit(20)
            .map { it.toExpenseDto() }
    }

    private fun ResultRow.toExpenseDto() = ExpenseDto(
        id = this[ExpensesTable.id].toString(),
        title = this[ExpensesTable.title],
        description = this[ExpensesTable.description],
        amount = this[ExpensesTable.amount].toDouble(),
        category = this[ExpensesTable.category],
        paymentMethod = this[ExpensesTable.paymentMethod],
        date = this[ExpensesTable.date].toString(),
        notes = this[ExpensesTable.notes],
        createdAt = this[ExpensesTable.createdAt].toString()
    )

    private fun parseDate(dateStr: String): LocalDate {
        return try {
            LocalDate.parse(dateStr)
        } catch (e: DateTimeParseException) {
            throw ApiException.badRequest("Invalid date format. Use YYYY-MM-DD")
        }
    }
}

private val ExpensesTable = com.stor.expenses.models.ExpensesTable
