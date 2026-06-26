package com.stor.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_SECRET")
        ?: throw IllegalStateException("JWT_SECRET is not configured")
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "stor-api"
    val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "stor-app"
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "stor"

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or has expired"))
            }
        }
    }
}
