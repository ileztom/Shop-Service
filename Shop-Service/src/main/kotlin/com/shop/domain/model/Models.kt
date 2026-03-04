package com.shop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDTO
)

@Serializable
data class ProductDTO(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int = 0
)

@Serializable
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val stock: Int? = null
)

@Serializable
data class OrderItemRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>
)

@Serializable
data class OrderItemDTO(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtOrder: Double
)

@Serializable
data class OrderDTO(
    val id: Int,
    val userId: Int,
    val totalPrice: Double,
    val status: String,
    val items: List<OrderItemDTO> = emptyList(),
    val createdAt: String
)

@Serializable
data class OrderStatsDTO(
    val totalOrders: Int,
    val totalRevenue: Double,
    val ordersByStatus: Map<String, Int>,
    val topProducts: List<TopProductDTO>
)

@Serializable
data class TopProductDTO(
    val productId: Int,
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

@Serializable
data class OrderEvent(
    val orderId: Int,
    val userId: Int,
    val action: String,
    val totalPrice: Double,
    val timestamp: String
)
