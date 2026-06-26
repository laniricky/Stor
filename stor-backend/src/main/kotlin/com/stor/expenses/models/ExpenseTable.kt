package com.stor.expenses.models

import com.stor.auth.models.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object ExpensesTable : Table("expenses") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val title = varchar("title", 255)
    val description = varchar("description", 1000).nullable()
    val amount = decimal("amount", 12, 2)
    val category = varchar("category", 100)
    val paymentMethod = varchar("payment_method", 100).default("Cash")
    val date = date("date")
    val notes = text("notes").nullable()
    val syncedAt = timestamp("synced_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
