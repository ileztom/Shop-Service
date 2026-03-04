package com.shop.unit

import com.shop.domain.model.CreateOrderRequest
import com.shop.service.OrderService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderServiceTest {

    @Test
    fun `createOrder should fail with empty items`() {
        val service = OrderService()
        assertThrows<IllegalArgumentException> {
            service.createOrder(1, CreateOrderRequest(emptyList()))
        }
    }
}
