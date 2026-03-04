package com.shop.unit

import com.shop.domain.model.CreateProductRequest
import com.shop.service.ProductService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceTest {

    @Test
    fun `createProduct should fail with blank name`() {
        val service = ProductService()
        assertThrows<IllegalArgumentException> {
            service.createProduct(CreateProductRequest("", null, 10.0, 5), 1)
        }
    }

    @Test
    fun `createProduct should fail with negative price`() {
        val service = ProductService()
        assertThrows<IllegalArgumentException> {
            service.createProduct(CreateProductRequest("Test", null, -5.0, 5), 1)
        }
    }

    @Test
    fun `createProduct should fail with negative stock`() {
        val service = ProductService()
        assertThrows<IllegalArgumentException> {
            service.createProduct(CreateProductRequest("Test", null, 10.0, -1), 1)
        }
    }
}
