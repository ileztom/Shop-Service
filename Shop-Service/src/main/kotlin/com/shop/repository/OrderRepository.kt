package com.shop.repository

import com.shop.domain.model.OrderDTO
import com.shop.domain.model.OrderItemDTO
import com.shop.domain.table.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object OrderRepository {

    fun create(userId: Int, totalPrice: Double): Int = transaction {
        Orders.insert {
            it[Orders.userId] = userId
            it[Orders.totalPrice] = BigDecimal.valueOf(totalPrice)
            it[Orders.status] = "CREATED"
        } get Orders.id
    }

    fun addItem(orderId: Int, productId: Int, quantity: Int, price: Double) = transaction {
        OrderItems.insert {
            it[OrderItems.orderId] = orderId
            it[OrderItems.productId] = productId
            it[OrderItems.quantity] = quantity
            it[OrderItems.priceAtOrder] = BigDecimal.valueOf(price)
        }
    }

    fun findByUserId(userId: Int): List<OrderDTO> = transaction {
        Orders.select { Orders.userId eq userId }
            .orderBy(Orders.createdAt, SortOrder.DESC)
            .map { row ->
                val orderId = row[Orders.id]
                val items = getOrderItems(orderId)
                OrderDTO(
                    id = orderId,
                    userId = row[Orders.userId],
                    totalPrice = row[Orders.totalPrice].toDouble(),
                    status = row[Orders.status],
                    items = items,
                    createdAt = row[Orders.createdAt].toString()
                )
            }
    }

    fun findById(id: Int): OrderDTO? = transaction {
        Orders.select { Orders.id eq id }
            .firstOrNull()
            ?.let { row ->
                val items = getOrderItems(id)
                OrderDTO(
                    id = row[Orders.id],
                    userId = row[Orders.userId],
                    totalPrice = row[Orders.totalPrice].toDouble(),
                    status = row[Orders.status],
                    items = items,
                    createdAt = row[Orders.createdAt].toString()
                )
            }
    }

    fun cancelOrder(orderId: Int): Boolean = transaction {
        val order = Orders.select { Orders.id eq orderId }.firstOrNull() ?: return@transaction false
        if (order[Orders.status] == "CANCELLED") return@transaction false

        Orders.update({ Orders.id eq orderId }) {
            it[Orders.status] = "CANCELLED"
        }
        true
    }

    fun getOrderItems(orderId: Int): List<OrderItemDTO> = transaction {
        (OrderItems innerJoin Products)
            .select { OrderItems.orderId eq orderId }
            .map {
                OrderItemDTO(
                    id = it[OrderItems.id],
                    productId = it[OrderItems.productId],
                    productName = it[Products.name],
                    quantity = it[OrderItems.quantity],
                    priceAtOrder = it[OrderItems.priceAtOrder].toDouble()
                )
            }
    }

    fun getOrderUserId(orderId: Int): Int? = transaction {
        Orders.select { Orders.id eq orderId }
            .firstOrNull()
            ?.get(Orders.userId)
    }

    fun getOrderStatus(orderId: Int): String? = transaction {
        Orders.select { Orders.id eq orderId }
            .firstOrNull()
            ?.get(Orders.status)
    }
}
