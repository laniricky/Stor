package com.stor.auth

import com.stor.auth.models.*
import com.stor.common.MessageResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val jwtService = JwtService(application)
    val authService = AuthService(jwtService, application)

    route("/auth") {
        rateLimit(RateLimitName("auth")) {
            post("/register") {
                val req = call.receive<RegisterRequest>()
                val response = authService.register(req)
                call.respond(HttpStatusCode.Created, response)
            }

            post("/login") {
                val req = call.receive<LoginRequest>()
                val response = authService.login(req)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val req = call.receive<RefreshTokenRequest>()
                val response = authService.refreshToken(req)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/forgot-password") {
                val req = call.receive<ForgotPasswordRequest>()
                authService.forgotPassword(req)
                call.respond(HttpStatusCode.OK, MessageResponse("If that email exists, a reset link has been sent"))
            }
        }
    }
}
