package com.stor.plugins

import com.stor.auth.models.RefreshTokensTable
import com.stor.auth.models.UsersTable
import com.stor.expenses.models.ExpensesTable
import com.stor.income.models.IncomeTable
import com.stor.loans.models.LoansTable
import com.stor.repayments.models.RepaymentsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val rawUrl = environment.config.propertyOrNull("database.jdbcUrl")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: throw IllegalStateException("DATABASE_URL is not configured")

    // Neon/Postgres URLs may come as postgresql:// — JDBC driver requires jdbc:postgresql://
    val jdbcUrl = when {
        rawUrl.startsWith("jdbc:") -> rawUrl
        rawUrl.startsWith("postgresql://") -> "jdbc:${rawUrl}"
        rawUrl.startsWith("postgres://") -> "jdbc:postgresql://${rawUrl.removePrefix("postgres://")}"
        else -> rawUrl
    }

    val hikariConfig = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = environment.config.propertyOrNull("database.maximumPoolSize")
            ?.getString()?.toInt() ?: 10
        minimumIdle = 2
        connectionTimeout = 30_000
        idleTimeout = 600_000
        maxLifetime = 1_800_000
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    // Create tables if they don't exist
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            RefreshTokensTable,
            ExpensesTable,
            IncomeTable,
            LoansTable,
            RepaymentsTable
        )
    }

    log.info("Database connected and schema applied")
}
