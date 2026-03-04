package com.shop.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    lateinit var realm: String
        private set
    private var expiration: Long = 3600000

    fun init(environment: ApplicationEnvironment) {
        secret = environment.config.property("jwt.secret").getString()
        issuer = environment.config.property("jwt.issuer").getString()
        audience = environment.config.property("jwt.audience").getString()
        realm = environment.config.property("jwt.realm").getString()
        expiration = environment.config.property("jwt.expiration").getString().toLong()
    }

    fun generateToken(userId: Int, role: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + expiration))
            .sign(Algorithm.HMAC256(secret))
    }

    fun getAlgorithm(): Algorithm = Algorithm.HMAC256(secret)
    fun getIssuer(): String = issuer
    fun getAudience(): String = audience

    fun verifyToken(token: String): DecodedJWT {
        val verifier = JWT.require(getAlgorithm())
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
        return verifier.verify(token)
    }
}
