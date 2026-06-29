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

/**
 * Parses a Postgres connection URL (postgresql:// or postgres:// or jdbc:postgresql://)
 * and returns (jdbcUrl, username, password).
 *
 * The PostgreSQL JDBC driver does NOT support embedded user:password in the URL authority.
 * Credentials must be passed separately to HikariCP.
 */
private fun parsePostgresUrl(raw: String): Triple<String, String?, String?> {
    // Strip any jdbc: prefix and normalise scheme
    val noJdbc = raw.removePrefix("jdbc:")
    val withoutScheme = when {
        noJdbc.startsWith("postgresql://") -> noJdbc.removePrefix("postgresql://")
        noJdbc.startsWith("postgres://") -> noJdbc.removePrefix("postgres://")
        else -> noJdbc
    }

    // Split userinfo from host
    val atIdx = withoutScheme.indexOf('@')
    val (userInfo, hostAndRest) = if (atIdx >= 0) {
        withoutScheme.substring(0, atIdx) to withoutScheme.substring(atIdx + 1)
    } else {
        "" to withoutScheme
    }

    val (username, password) = when {
        userInfo.contains(':') -> {
            val ci = userInfo.indexOf(':')
            userInfo.substring(0, ci) to userInfo.substring(ci + 1)
        }
        userInfo.isNotEmpty() -> userInfo to null
        else -> null to null
    }

    // Remove channel_binding — not supported by pgjdbc
    val cleanRest = hostAndRest
        .replace("&channel_binding=require", "")
        .replace("channel_binding=require&", "")
        .replace("channel_binding=require", "")
        .trimEnd('?').trimEnd('&')

    return Triple("jdbc:postgresql://$cleanRest", username?.ifEmpty { null }, password?.ifEmpty { null })
}

fun Application.configureDatabase() {
    val rawUrl = environment.config.propertyOrNull("database.jdbcUrl")?.getString()
        ?: System.getenv("DATABASE_URL")
        ?: throw IllegalStateException("DATABASE_URL is not configured")

    val (jdbcUrl, username, password) = parsePostgresUrl(rawUrl)
    log.info("Connecting to database at ${jdbcUrl.substringBefore('?')}")

    val hikariConfig = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        driverClassName = "org.postgresql.Driver"
        if (username != null) this.username = username
        if (password != null) this.password = password
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
