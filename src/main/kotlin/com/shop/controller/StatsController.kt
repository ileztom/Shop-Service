package com.shop.controller

import com.shop.domain.model.ErrorResponse
import com.shop.service.StatsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statsRoutes(statsService: StatsService) {

    route("/stats") {
        get("/orders") {
            val principal = call.principal<JWTPrincipal>()!!
            val role = principal.payload.getClaim("role").asString()
            if (role != "ADMIN") {
                return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse("forbidden", "Admin access required"))
            }
            val stats = statsService.getOrderStats()
            call.respond(HttpStatusCode.OK, stats)
        }
    }
}
