package com.stor.auth

import com.stor.auth.models.*
import com.stor.common.ApiException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.Properties
import java.util.UUID
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.server.application.Application

class AuthService(
    private val jwtService: JwtService,
    private val application: Application
) {

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
        val email = request.email.lowercase().trim()
        val resetToken = UUID.randomUUID().toString().replace("-", "")
        
        transaction {
            val user = UsersTable.select { UsersTable.email eq email }
                .singleOrNull() ?: return@transaction // Silent fail to prevent user enumeration

            PasswordResetTokensTable.insert {
                it[userId] = user[UsersTable.id].value
                it[token] = resetToken
                it[expiresAt] = Instant.now().plusSeconds(3600)
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendResetEmail(email, resetToken)
            } catch (e: Exception) {
                application.environment.log.error("Failed to send reset email to $email", e)
            }
        }
    }

    private fun sendResetEmail(toEmail: String, token: String) {
        val config = application.environment.config
        val host = config.propertyOrNull("smtp.host")?.getString() ?: System.getenv("SMTP_HOST")
        val port = config.propertyOrNull("smtp.port")?.getString() ?: System.getenv("SMTP_PORT")
        val username = config.propertyOrNull("smtp.username")?.getString() ?: System.getenv("SMTP_USERNAME")
        val password = config.propertyOrNull("smtp.password")?.getString() ?: System.getenv("SMTP_PASSWORD")
        val fromEmail = config.propertyOrNull("smtp.from")?.getString() ?: System.getenv("SMTP_FROM")

        if (host == null || username == null || password == null || fromEmail == null) {
            application.environment.log.warn("SMTP credentials not configured. Skipping email.")
            return
        }

        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port ?: "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(fromEmail))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            subject = "Stor - Password Reset"
            val resetLink = "storapp://reset-password?token=$token"
            setText("You requested a password reset. Click the following link to reset your password:\n\n$resetLink\n\nIf you did not request this, please ignore this email.")
        }

        Transport.send(message)
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
