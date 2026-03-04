package com.shop.unit

import com.shop.config.JwtConfig
import com.shop.domain.model.RegisterRequest
import com.shop.domain.model.LoginRequest
import com.shop.service.AuthService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthServiceTest {

    @Test
    fun `register should fail with blank username`() {
        val service = AuthService()
        assertThrows<IllegalArgumentException> {
            service.register(RegisterRequest("", "test@mail.com", "password123"))
        }
    }

    @Test
    fun `register should fail with short password`() {
        val service = AuthService()
        assertThrows<IllegalArgumentException> {
            service.register(RegisterRequest("user", "test@mail.com", "12"))
        }
    }

    @Test
    fun `register should fail with invalid email`() {
        val service = AuthService()
        assertThrows<IllegalArgumentException> {
            service.register(RegisterRequest("user", "invalid-email", "password123"))
        }
    }

    @Test
    fun `login should fail with blank email`() {
        val service = AuthService()
        assertThrows<IllegalArgumentException> {
            service.login(LoginRequest("", "password"))
        }
    }
}
