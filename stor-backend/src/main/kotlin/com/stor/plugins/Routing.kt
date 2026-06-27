package com.stor.plugins

import com.stor.auth.authRoutes
import com.stor.dashboard.dashboardRoutes
import com.stor.expenses.expenseRoutes
import com.stor.income.incomeRoutes
import com.stor.loans.loanRoutes
import com.stor.repayments.repaymentRoutes
import com.stor.reports.reportRoutes
import com.stor.search.searchRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf(
                "app" to "Stor API",
                "message" to "Welcome to the Stor backend!",
                "docs" to "Append /api/v1 to use endpoints"
            ))
        }

        get("/health") {
            call.respond(mapOf("status" to "ok", "version" to "1.0.0"))
        }

        route("/api/v1") {
            // Public routes
            authRoutes()

            // Protected routes
            authenticate("auth-jwt") {
                dashboardRoutes()
                expenseRoutes()
                incomeRoutes()
                loanRoutes()
                repaymentRoutes()
                reportRoutes()
                searchRoutes()
            }
        }
    }
}
