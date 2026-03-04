package com.shop.service

import com.shop.cache.RedisCache
import com.shop.domain.model.*
import com.shop.repository.AuditLogRepository
import com.shop.repository.ProductRepository
import kotlinx.serialization.encodeToString

class ProductService {

    fun getAllProducts(): List<ProductDTO> {
        return RedisCache.getOrSet("products:all") {
            ProductRepository.findAll()
        }
    }

    fun getProductById(id: Int): ProductDTO {
        return RedisCache.getOrSet("product:$id") {
            ProductRepository.findById(id) ?: throw NoSuchElementException("Product not found: $id")
        }
    }

    fun createProduct(request: CreateProductRequest, adminId: Int): ProductDTO {
        validateProductRequest(request.name, request.price, request.stock)

        val product = ProductRepository.create(request.name, request.description, request.price, request.stock)

        AuditLogRepository.log(adminId, "CREATE", "PRODUCT", product.id, "Created product: ${product.name}")
        invalidateProductCache()

        return product
    }

    fun updateProduct(id: Int, request: UpdateProductRequest, adminId: Int): ProductDTO {
        if (request.price != null && request.price < 0) throw IllegalArgumentException("Price cannot be negative")
        if (request.stock != null && request.stock < 0) throw IllegalArgumentException("Stock cannot be negative")

        val updated = ProductRepository.update(id, request.name, request.description, request.price, request.stock)
            ?: throw NoSuchElementException("Product not found: $id")

        AuditLogRepository.log(adminId, "UPDATE", "PRODUCT", id, "Updated product: ${updated.name}")
        invalidateProductCache()
        RedisCache.delete("product:$id")

        return updated
    }

    fun deleteProduct(id: Int, adminId: Int) {
        ProductRepository.findById(id) ?: throw NoSuchElementException("Product not found: $id")

        try {
            if (!ProductRepository.delete(id)) {
                throw IllegalStateException("Failed to delete product")
            }
        } catch (e: org.jetbrains.exposed.exceptions.ExposedSQLException) {
            throw IllegalStateException("Cannot delete product: it is referenced by existing orders")
        }

        AuditLogRepository.log(adminId, "DELETE", "PRODUCT", id, "Deleted product $id")
        invalidateProductCache()
        RedisCache.delete("product:$id")
    }

    private fun validateProductRequest(name: String, price: Double, stock: Int) {
        if (name.isBlank()) throw IllegalArgumentException("Product name is required")
        if (price < 0) throw IllegalArgumentException("Price cannot be negative")
        if (stock < 0) throw IllegalArgumentException("Stock cannot be negative")
    }

    private fun invalidateProductCache() {
        RedisCache.delete("products:all")
    }
}
