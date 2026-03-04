package com.shop.controller

import com.shop.domain.model.*
import com.shop.service.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productService: ProductService) {

    route("/products") {
        get {
            val products = productService.getAllProducts()
            call.respond(HttpStatusCode.OK, products)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid product ID"))
            val product = productService.getProductById(id)
            call.respond(HttpStatusCode.OK, product)
        }
    }
}

fun Route.adminProductRoutes(productService: ProductService) {

    route("/products") {
        post {
            val principal = call.principal<JWTPrincipal>()!!
            val role = principal.payload.getClaim("role").asString()
            if (role != "ADMIN") {
                return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse("forbidden", "Admin access required"))
            }
            val adminId = principal.payload.getClaim("userId").asInt()
            val request = call.receive<CreateProductRequest>()
            val product = productService.createProduct(request, adminId)
            call.respond(HttpStatusCode.Created, product)
        }

        put("/{id}") {
            val principal = call.principal<JWTPrincipal>()!!
            val role = principal.payload.getClaim("role").asString()
            if (role != "ADMIN") {
                return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse("forbidden", "Admin access required"))
            }
            val adminId = principal.payload.getClaim("userId").asInt()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid product ID"))
            val request = call.receive<UpdateProductRequest>()
            val product = productService.updateProduct(id, request, adminId)
            call.respond(HttpStatusCode.OK, product)
        }

        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()!!
            val role = principal.payload.getClaim("role").asString()
            if (role != "ADMIN") {
                return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponse("forbidden", "Admin access required"))
            }
            val adminId = principal.payload.getClaim("userId").asInt()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", "Invalid product ID"))
            productService.deleteProduct(id, adminId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
