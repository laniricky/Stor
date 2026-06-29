package com.stor.repayments

import com.stor.common.userId
import com.stor.repayments.models.CreateRepaymentRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = RepaymentService()

fun Route.repaymentRoutes() {
    route("/loans/{loanId}/repayments") {
        get {
            val principal = call.principal<JWTPrincipal>()!!
            val loanId = call.parameters["loanId"]!!
            call.respond(HttpStatusCode.OK, service.list(principal.userId(), loanId))
        }
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val loanId = call.parameters["loanId"]!!
            val req = call.receive<CreateRepaymentRequest>()
            call.respond(HttpStatusCode.Created, service.create(principal.userId(), loanId, req))
        }
    }
}
