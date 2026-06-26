package com.stor.income

import com.stor.common.userId
import com.stor.income.models.CreateIncomeRequest
import com.stor.income.models.UpdateIncomeRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = IncomeService()

fun Route.incomeRoutes() {
    route("/income") {
        get {
            val principal = call.principal<JWTPrincipal>()!!
            val month = call.request.queryParameters["month"]?.toIntOrNull()
            val year = call.request.queryParameters["year"]?.toIntOrNull()
            call.respond(HttpStatusCode.OK, service.list(principal.userId(), month, year))
        }
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val req = call.receive<CreateIncomeRequest>()
            call.respond(HttpStatusCode.Created, service.create(principal.userId(), req))
        }
        route("/{id}") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                call.respond(HttpStatusCode.OK, service.get(principal.userId(), call.parameters["id"]!!))
            }
            put {
                val principal = call.principal<JWTPrincipal>()!!
                val req = call.receive<UpdateIncomeRequest>()
                call.respond(HttpStatusCode.OK, service.update(principal.userId(), call.parameters["id"]!!, req))
            }
            delete {
                val principal = call.principal<JWTPrincipal>()!!
                service.delete(principal.userId(), call.parameters["id"]!!)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
