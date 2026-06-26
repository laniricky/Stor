package com.stor.auth

import com.stor.auth.models.*
import com.stor.common.ApiException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.UUID

class AuthService(private val jwtService: JwtService) {

    fun register(request: RegisterRequest): AuthResponse {
        if (request.name.isBlank()) throw ApiException.badRequest("Name is required")
        if (!request.email.contains('@')) throw ApiException.badRequest("Invalid email")
        if (request.password.length < 8) throw ApiException.badRequest("Password must be at least 8 characters")

        return transaction {
            val exists = UsersTable.select { UsersTable.email eq request.email.lowercase() }.count() > 0
            if (exists) throw ApiException.conflict("Email already registered")

            val userId = UsersTable.insertAndGetId {
                it[name] = request.name.trim()
                it[email] = request.email.lowercase().trim()
                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt(12))
            }

            buildAuthResponse(userId.value, request.name.trim(), request.email.lowercase())
        }
    }

    fun login(request: LoginRequest): AuthResponse {
        return transaction {
            val user = UsersTable.select { UsersTable.email eq request.email.lowercase() }
                .singleOrNull() ?: throw ApiException.unauthorized("Invalid email or password")

            if (!BCrypt.checkpw(request.password, user[UsersTable.passwordHash])) {
                throw ApiException.unauthorized("Invalid email or password")
            }

            buildAuthResponse(user[UsersTable.id].value, user[UsersTable.name], user[UsersTable.email])
        }
    }

    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        return transaction {
            val now = Instant.now()
            val tokenRow = RefreshTokensTable.select { RefreshTokensTable.token eq request.refreshToken }
                .singleOrNull() ?: throw ApiException.unauthorized("Invalid refresh token")

            if (tokenRow[RefreshTokensTable.expiresAt].isBefore(now)) {
                RefreshTokensTable.deleteWhere { token eq request.refreshToken }
                throw ApiException.unauthorized("Refresh token expired")
            }

            val userId = tokenRow[RefreshTokensTable.userId].value
            val user = UsersTable.select { UsersTable.id eq userId }
                .singleOrNull() ?: throw ApiException.unauthorized("User not found")

            // Rotate refresh token
            RefreshTokensTable.deleteWhere { token eq request.refreshToken }
            val newRefresh = storeRefreshToken(userId, jwtService.refreshTokenExpiryMs())

            TokenResponse(
                accessToken = jwtService.generateAccessToken(userId.toString(), user[UsersTable.email]),
                refreshToken = newRefresh
            )
        }
    }

    fun forgotPassword(request: ForgotPasswordRequest) {
        // In production: generate reset token, store it, send email
        // Implementation uses placeholder email config
        transaction {
            val user = UsersTable.select { UsersTable.email eq request.email.lowercase() }
                .singleOrNull() ?: return@transaction // Silent fail to prevent user enumeration

            PasswordResetTokensTable.insert {
                it[userId] = user[UsersTable.id].value
                it[token] = UUID.randomUUID().toString().replace("-", "")
                it[expiresAt] = Instant.now().plusSeconds(3600)
            }
            // TODO: Send email with reset link
        }
    }

    private fun buildAuthResponse(userId: UUID, name: String, email: String): AuthResponse {
        val accessToken = jwtService.generateAccessToken(userId.toString(), email)
        val refreshToken = storeRefreshToken(userId, jwtService.refreshTokenExpiryMs())
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserDto(id = userId.toString(), name = name, email = email)
        )
    }

    private fun storeRefreshToken(userId: UUID, expiryMs: Long): String {
        val token = jwtService.generateRefreshToken()
        RefreshTokensTable.insert {
            it[this.userId] = userId
            it[this.token] = token
            it[expiresAt] = Instant.now().plusMillis(expiryMs)
        }
        return token
    }
}

// Use the table directly since we're in the same package
private val UsersTable = com.stor.auth.models.UsersTable
private val RefreshTokensTable = com.stor.auth.models.RefreshTokensTable
private val PasswordResetTokensTable = com.stor.auth.models.PasswordResetTokensTable
