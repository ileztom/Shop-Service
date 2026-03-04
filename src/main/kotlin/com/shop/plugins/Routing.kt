package com.shop.plugins

import com.shop.controller.*
import com.shop.service.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val authService = AuthService()
    val productService = ProductService()
    val orderService = OrderService()
    val statsService = StatsService()

    routing {
        authRoutes(authService)
        productRoutes(productService)

        authenticate("auth-jwt") {
            adminProductRoutes(productService)
            orderRoutes(orderService)
            statsRoutes(statsService)
        }
    }
}
