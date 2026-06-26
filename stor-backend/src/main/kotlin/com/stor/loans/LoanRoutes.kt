package com.stor.loans


import io.ktor.server.application.*
import com.stor.common.userId
import com.stor.loans.models.CreateLoanRequest
import com.stor.loans.models.UpdateLoanRequest
import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = LoanService()

fun Route.loanRoutes() {
    route("/loans") {
        get {
            val principal = call.principal<JWTPrincipal>()!!
            val status = call.request.queryParameters["status"]
            call.respond(HttpStatusCode.OK, service.list(principal.userId(), status))
        }
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val req = call.receive<CreateLoanRequest>()
            call.respond(HttpStatusCode.Created, service.create(principal.userId(), req))
        }
        route("/{id}") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                call.respond(HttpStatusCode.OK, service.get(principal.userId(), call.parameters["id"]!!))
            }
            put {
                val principal = call.principal<JWTPrincipal>()!!
                val req = call.receive<UpdateLoanRequest>()
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
