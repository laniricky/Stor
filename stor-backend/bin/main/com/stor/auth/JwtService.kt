package com.stor.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import java.util.*

class JwtService(private val application: Application) {

    private val secret = application.environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_SECRET")
        ?: throw IllegalStateException("JWT_SECRET not configured")

    private val issuer = application.environment.config.propertyOrNull("jwt.issuer")?.getString()
        ?: "stor-api"

    private val audience = application.environment.config.propertyOrNull("jwt.audience")?.getString()
        ?: "stor-app"

    private val accessTokenExpiryMs = application.environment.config
        .propertyOrNull("jwt.accessTokenExpiryMs")?.getString()?.toLong() ?: 900_000L // 15 min

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: String, email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiryMs))
            .sign(algorithm)
    }

    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    fun refreshTokenExpiryMs(): Long = application.environment.config
        .propertyOrNull("jwt.refreshTokenExpiryMs")?.getString()?.toLong() ?: 2_592_000_000L // 30 days
}
