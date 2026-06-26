package com.stor

import com.stor.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureSecurity()
    configureStatusPages()
    configureCors()
    configureRateLimit()
    configureCallLogging()
    configureRouting()
}
