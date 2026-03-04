package com.shop.repository

import com.shop.domain.model.OrderStatsDTO
import com.shop.domain.model.TopProductDTO
import com.shop.domain.table.OrderItems
import com.shop.domain.table.Orders
import com.shop.domain.table.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object StatsRepository {

    fun getOrderStats(): OrderStatsDTO = transaction {
        val totalOrders = Orders.selectAll().count().toInt()

        val totalRevenue = Orders
            .select { Orders.status neq "CANCELLED" }
            .sumOf { it[Orders.totalPrice].toDouble() }

        val ordersByStatus = Orders.slice(Orders.status, Orders.id.count())
            .selectAll()
            .groupBy(Orders.status)
            .associate { it[Orders.status] to it[Orders.id.count()].toInt() }

        val quantitySum = OrderItems.quantity.sum()
        val revenueAlias = OrderItems.priceAtOrder.sum()

        val topProducts = (OrderItems innerJoin Products)
            .slice(
                OrderItems.productId,
                Products.name,
                quantitySum,
                revenueAlias
            )
            .selectAll()
            .groupBy(OrderItems.productId, Products.name)
            .orderBy(quantitySum, SortOrder.DESC)
            .limit(10)
            .map {
                TopProductDTO(
                    productId = it[OrderItems.productId],
                    productName = it[Products.name],
                    totalQuantity = it[quantitySum]?.toInt() ?: 0,
                    totalRevenue = it[revenueAlias]?.toDouble() ?: 0.0
                )
            }

        OrderStatsDTO(
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            ordersByStatus = ordersByStatus,
            topProducts = topProducts
        )
    }
}
