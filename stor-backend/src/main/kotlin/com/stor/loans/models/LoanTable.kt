package com.stor.loans.models

import com.stor.auth.models.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object LoansTable : Table("loans") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val name = varchar("name", 255)
    val lender = varchar("lender", 255)
    val originalAmount = decimal("original_amount", 12, 2)
    val remainingBalance = decimal("remaining_balance", 12, 2)
    val interestRate = decimal("interest_rate", 5, 2).nullable()
    val monthlyPayment = decimal("monthly_payment", 12, 2).nullable()
    val dueDay = integer("due_day").nullable()
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val status = varchar("status", 20).default("active") // active | archived
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
