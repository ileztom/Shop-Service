package com.shop.service

import com.shop.config.JwtConfig
import com.shop.domain.model.*
import com.shop.repository.UserRepository

class AuthService {

    fun register(request: RegisterRequest): AuthResponse {
        if (request.username.isBlank() || request.email.isBlank() || request.password.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }
        if (request.password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters")
        }
        if (!request.email.contains("@")) {
            throw IllegalArgumentException("Invalid email format")
        }
        if (UserRepository.existsByEmail(request.email)) {
            throw IllegalStateException("Email already registered")
        }
        if (UserRepository.existsByUsername(request.username)) {
            throw IllegalStateException("Username already taken")
        }

        val user = UserRepository.create(request.username, request.email, request.password)
        val token = JwtConfig.generateToken(user.id, user.role)
        return AuthResponse(token = token, user = user)
    }

    fun login(request: LoginRequest): AuthResponse {
        if (request.email.isBlank() || request.password.isBlank()) {
            throw IllegalArgumentException("Email and password are required")
        }

        val result = UserRepository.findByEmail(request.email)
            ?: throw SecurityException("Invalid credentials")

        val (user, passwordHash) = result

        if (!UserRepository.verifyPassword(request.password, passwordHash)) {
            throw SecurityException("Invalid credentials")
        }

        val token = JwtConfig.generateToken(user.id, user.role)
        return AuthResponse(token = token, user = user)
    }
}
