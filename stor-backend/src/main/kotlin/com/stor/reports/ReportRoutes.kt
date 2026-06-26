package com.stor.reports

import com.stor.common.userId
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

private val service = ReportService()

fun Route.reportRoutes() {
    val now = LocalDate.now()
    route("/reports") {
        get("/monthly") {
            val principal = call.principal<JWTPrincipal>()!!
            val month = call.request.queryParameters["month"]?.toIntOrNull() ?: now.monthValue
            val year = call.request.queryParameters["year"]?.toIntOrNull() ?: now.year
            call.respond(HttpStatusCode.OK, service.monthlyReport(principal.userId(), month, year))
        }
        get("/yearly") {
            val principal = call.principal<JWTPrincipal>()!!
            val year = call.request.queryParameters["year"]?.toIntOrNull() ?: now.year
            call.respond(HttpStatusCode.OK, service.yearlyReport(principal.userId(), year))
        }
        get("/categories") {
            val principal = call.principal<JWTPrincipal>()!!
            val month = call.request.queryParameters["month"]?.toIntOrNull() ?: now.monthValue
            val year = call.request.queryParameters["year"]?.toIntOrNull() ?: now.year
            call.respond(HttpStatusCode.OK, service.categoryReport(principal.userId(), month, year))
        }
    }
}
