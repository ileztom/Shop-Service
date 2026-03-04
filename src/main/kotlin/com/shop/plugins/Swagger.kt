package com.shop.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSwagger() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger"
            forwardRoot = false
        }
        info {
            title = "Shop Service API"
            version = "1.0.0"
            description = """
                REST API for an online shop service.
                
                Features:
                - User registration and authentication (JWT)
                - Product catalog management
                - Order management
                - Admin statistics
                - Redis caching
                - RabbitMQ event queue
                
                Authentication:
                Use Bearer token obtained from /auth/login or /auth/register endpoints.
            """.trimIndent()
            contact {
                name = "Shop Service Team"
                email = "admin@shop-service.com"
            }
        }
        server {
            url = "/"
            description = "Shop Service API"
        }
        pathFilter = { _, url -> url.firstOrNull() != "swagger" }
    }

    routing {
        get("/api-docs") {
            val specStream = this::class.java.classLoader.getResourceAsStream("openapi/documentation.yaml")
            if (specStream != null) {
                call.respondText(specStream.bufferedReader().readText(), ContentType.parse("application/yaml"))
            } else {
                call.respond(HttpStatusCode.NotFound, "OpenAPI spec not found")
            }
        }
    }
}
