package com.shop.repository

import com.shop.domain.model.ProductDTO
import com.shop.domain.table.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

object ProductRepository {

    fun findAll(): List<ProductDTO> = transaction {
        Products.selectAll()
            .orderBy(Products.id)
            .map { it.toProductDTO() }
    }

    fun findById(id: Int): ProductDTO? = transaction {
        Products.select { Products.id eq id }
            .firstOrNull()
            ?.toProductDTO()
    }

    fun create(name: String, description: String?, price: Double, stock: Int): ProductDTO = transaction {
        val id = Products.insert {
            it[Products.name] = name
            it[Products.description] = description
            it[Products.price] = BigDecimal.valueOf(price)
            it[Products.stock] = stock
        } get Products.id

        ProductDTO(id = id, name = name, description = description, price = price, stock = stock)
    }

    fun update(id: Int, name: String?, description: String?, price: Double?, stock: Int?): ProductDTO? = transaction {
        Products.select { Products.id eq id }.firstOrNull() ?: return@transaction null

        Products.update({ Products.id eq id }) {
            if (name != null) it[Products.name] = name
            if (description != null) it[Products.description] = description
            if (price != null) it[Products.price] = BigDecimal.valueOf(price)
            if (stock != null) it[Products.stock] = stock
            it[Products.updatedAt] = Instant.now()
        }

        findById(id)
    }

    fun delete(productId: Int): Boolean = transaction {
        Products.deleteWhere { id eq productId } > 0
    }

    fun decreaseStock(id: Int, quantity: Int): Boolean = transaction {
        val updated = Products.update({ (Products.id eq id) and (Products.stock greaterEq quantity) }) {
            with(SqlExpressionBuilder) {
                it[Products.stock] = Products.stock - quantity
            }
            it[Products.updatedAt] = Instant.now()
        }
        updated > 0
    }

    fun restoreStock(id: Int, quantity: Int) = transaction {
        Products.update({ Products.id eq id }) {
            with(SqlExpressionBuilder) {
                it[Products.stock] = Products.stock + quantity
            }
            it[Products.updatedAt] = Instant.now()
        }
    }

    private fun ResultRow.toProductDTO() = ProductDTO(
        id = this[Products.id],
        name = this[Products.name],
        description = this[Products.description],
        price = this[Products.price].toDouble(),
        stock = this[Products.stock]
    )
}
