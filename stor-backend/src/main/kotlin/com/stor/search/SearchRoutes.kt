package com.stor.search

import com.stor.common.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val service = SearchService()

fun Route.searchRoutes() {
    get("/search") {
        val principal = call.principal<JWTPrincipal>()!!
        val query = call.request.queryParameters["q"] ?: ""
        val searchType = call.request.queryParameters["type"]
        call.respond(HttpStatusCode.OK, service.search(principal.userId(), query, searchType))
    }
}
