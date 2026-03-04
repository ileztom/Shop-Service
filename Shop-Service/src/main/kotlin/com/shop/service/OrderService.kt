package com.shop.service

import com.shop.cache.RedisCache
import com.shop.domain.model.*
import com.shop.queue.RabbitMQProducer
import com.shop.repository.AuditLogRepository
import com.shop.repository.OrderRepository
import com.shop.repository.ProductRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class OrderService {
    private val json = Json { encodeDefaults = true }

    fun createOrder(userId: Int, request: CreateOrderRequest): OrderDTO {
        if (request.items.isEmpty()) {
            throw IllegalArgumentException("Order must contain at least one item")
        }

        return transaction {
            var totalPrice = 0.0

            for (item in request.items) {
                if (item.quantity <= 0) throw IllegalArgumentException("Quantity must be positive")
                val product = ProductRepository.findById(item.productId)
                    ?: throw NoSuchElementException("Product not found: ${item.productId}")
                if (product.stock < item.quantity) {
                    throw IllegalStateException("Insufficient stock for product '${product.name}': available ${product.stock}, requested ${item.quantity}")
                }
                totalPrice += product.price * item.quantity
            }

            val orderId = OrderRepository.create(userId, totalPrice)

            for (item in request.items) {
                val product = ProductRepository.findById(item.productId)!!
                OrderRepository.addItem(orderId, item.productId, item.quantity, product.price)
                if (!ProductRepository.decreaseStock(item.productId, item.quantity)) {
                    throw IllegalStateException("Insufficient stock for product '${product.name}' (concurrent modification)")
                }
            }

            AuditLogRepository.log(userId, "CREATE", "ORDER", orderId, "Order created, total: $totalPrice")

            val order = OrderRepository.findById(orderId)!!

            RedisCache.set("order:$orderId", json.encodeToString(order))

            val event = OrderEvent(
                orderId = orderId,
                userId = userId,
                action = "ORDER_CREATED",
                totalPrice = totalPrice,
                timestamp = Instant.now().toString()
            )
            RabbitMQProducer.publish(json.encodeToString(event))

            order
        }
    }

    fun getUserOrders(userId: Int): List<OrderDTO> {
        return OrderRepository.findByUserId(userId)
    }

    fun cancelOrder(userId: Int, orderId: Int): OrderDTO {
        val orderUserId = OrderRepository.getOrderUserId(orderId)
            ?: throw NoSuchElementException("Order not found: $orderId")

        if (orderUserId != userId) {
            throw SecurityException("You don't have access to this order")
        }

        val status = OrderRepository.getOrderStatus(orderId)
        if (status == "CANCELLED") {
            throw IllegalStateException("Order is already cancelled")
        }

        return transaction {
            val items = OrderRepository.getOrderItems(orderId)
            for (item in items) {
                ProductRepository.restoreStock(item.productId, item.quantity)
            }

            OrderRepository.cancelOrder(orderId)
            AuditLogRepository.log(userId, "CANCEL", "ORDER", orderId, "Order cancelled")

            val order = OrderRepository.findById(orderId)!!

            RedisCache.delete("order:$orderId")
            RedisCache.delete("products:all")

            val event = OrderEvent(
                orderId = orderId,
                userId = userId,
                action = "ORDER_CANCELLED",
                totalPrice = order.totalPrice,
                timestamp = Instant.now().toString()
            )
            RabbitMQProducer.publish(Json.encodeToString(event))

            order
        }
    }
}
