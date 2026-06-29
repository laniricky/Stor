package com.stor.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimit() {
    install(RateLimit) {
        global {
            rateLimiter(limit = 200, refillPeriod = 60.seconds)
        }
        register(RateLimitName("auth")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }
    }
}
