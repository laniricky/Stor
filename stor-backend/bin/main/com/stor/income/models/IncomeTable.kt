package com.stor.income.models

import com.stor.auth.models.UsersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object IncomeTable : Table("income") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val incomeSource = varchar("source", 255)
    val amount = decimal("amount", 12, 2)
    val date = date("date")
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
