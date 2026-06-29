package com.stor.common

import io.ktor.http.*

class ApiException(
    val statusCode: HttpStatusCode,
    override val message: String
) : Exception(message) {
    companion object {
        fun notFound(msg: String = "Resource not found") = ApiException(HttpStatusCode.NotFound, msg)
        fun badRequest(msg: String) = ApiException(HttpStatusCode.BadRequest, msg)
        fun unauthorized(msg: String = "Unauthorized") = ApiException(HttpStatusCode.Unauthorized, msg)
        fun forbidden(msg: String = "Forbidden") = ApiException(HttpStatusCode.Forbidden, msg)
        fun conflict(msg: String) = ApiException(HttpStatusCode.Conflict, msg)
    }
}

/** Extract authenticated user's UUID from JWT principal */
fun io.ktor.server.auth.jwt.JWTPrincipal.userId(): String =
    payload.getClaim("userId").asString()
        ?: throw ApiException.unauthorized("Invalid token payload")

/** Standard paginated list response */
@kotlinx.serialization.Serializable
data class PagedResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@kotlinx.serialization.Serializable
data class MessageResponse(val message: String)
