package com.stor.expenses

import com.stor.common.userId
import com.stor.expenses.models.CreateExpenseRequest
import com.stor.expenses.models.UpdateExpenseRequest
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = ExpenseService()

fun Route.expenseRoutes() {
    route("/expenses") {
        get {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.userId()
            val category = call.request.queryParameters["category"]
            val month = call.request.queryParameters["month"]?.toIntOrNull()
            val year = call.request.queryParameters["year"]?.toIntOrNull()
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 50

            val result = service.listExpenses(userId, category, month, year, search, page, pageSize)
            call.respond(HttpStatusCode.OK, result)
        }

        post {
            val principal = call.principal<JWTPrincipal>()!!
            val req = call.receive<CreateExpenseRequest>()
            val expense = service.createExpense(principal.userId(), req)
            call.respond(HttpStatusCode.Created, expense)
        }

        route("/{id}") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val id = call.parameters["id"]!!
                val expense = service.getExpense(principal.userId(), id)
                call.respond(HttpStatusCode.OK, expense)
            }

            put {
                val principal = call.principal<JWTPrincipal>()!!
                val id = call.parameters["id"]!!
                val req = call.receive<UpdateExpenseRequest>()
                val expense = service.updateExpense(principal.userId(), id, req)
                call.respond(HttpStatusCode.OK, expense)
            }

            delete {
                val principal = call.principal<JWTPrincipal>()!!
                val id = call.parameters["id"]!!
                service.deleteExpense(principal.userId(), id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
