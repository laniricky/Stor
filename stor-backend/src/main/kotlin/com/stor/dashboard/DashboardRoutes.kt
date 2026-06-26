package com.stor.dashboard

import com.stor.common.userId
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = DashboardService()

fun Route.dashboardRoutes() {
    get("/dashboard") {
        val principal = call.principal<JWTPrincipal>()!!
        call.respond(HttpStatusCode.OK, service.getDashboard(principal.userId()))
    }
}
