package com.stor.repayments.models

import com.stor.loans.models.LoansTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object RepaymentsTable : Table("repayments") {
    val id = uuid("id").autoGenerate()
    val loanId = uuid("loan_id").references(LoansTable.id)
    val amountPaid = decimal("amount_paid", 12, 2)
    val date = date("date")
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
