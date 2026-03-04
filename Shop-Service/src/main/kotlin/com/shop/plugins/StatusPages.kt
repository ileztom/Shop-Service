package com.shop.plugins

import com.shop.domain.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Bad request: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("bad_request", cause.message ?: "Invalid request")
            )
        }
        exception<IllegalStateException> { call, cause ->
            logger.warn("Conflict: ${cause.message}")
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse("conflict", cause.message ?: "Conflict")
            )
        }
        exception<NoSuchElementException> { call, cause ->
            logger.warn("Not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("not_found", cause.message ?: "Resource not found")
            )
        }
        exception<SecurityException> { call, cause ->
            logger.warn("Unauthorized: ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse("unauthorized", cause.message ?: "Unauthorized")
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Internal error: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", "An unexpected error occurred")
            )
        }
    }
}
