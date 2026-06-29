package com.stor.auth.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object PasswordResetTokensTable : Table("password_reset_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val token = varchar("token", 128).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val used = bool("used").default(false)
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
