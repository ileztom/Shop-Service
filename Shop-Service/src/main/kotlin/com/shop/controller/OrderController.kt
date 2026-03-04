package com.shop.controller

import com.shop.domain.model.*
import com.shop.service.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(orderService: OrderService) {

    route("/orders") {
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.getClaim("userId").asInt()
            val request = call.receive<CreateOrderRequest>()
            val order = orderService.createOrder(userId, request)
            call.respond(HttpStatusCode.Created, order)
        }

        get {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.getClaim("userId").asInt()
            val orders = orderService.getUserOrders(userId)
            call.respond(HttpStatusCode.OK, orders)
        }

        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.getClaim("userId").asInt()
            val orderId = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid order ID"))
            val order = orderService.cancelOrder(userId, orderId)
            call.respond(HttpStatusCode.OK, order)
        }
    }
}
