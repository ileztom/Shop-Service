package com.shop.integration

import com.shop.domain.table.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class DatabaseIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            postgres.start()
            Database.connect(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )
            transaction {
                SchemaUtils.create(Users, Products, Orders, OrderItems, AuditLogs)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            postgres.stop()
        }
    }

    @Test
    fun `should create and query users`() {
        transaction {
            Users.insert {
                it[username] = "testuser"
                it[email] = "test@example.com"
                it[passwordHash] = "hashed"
                it[role] = "USER"
            }

            val count = Users.selectAll().count()
            assertTrue(count > 0)
        }
    }

    @Test
    fun `should create and query products`() {
        transaction {
            Products.insert {
                it[name] = "Test Product"
                it[description] = "A test product"
                it[price] = BigDecimal.valueOf(29.99)
                it[stock] = 100
            }

            val products = Products.selectAll().toList()
            assertTrue(products.isNotEmpty())
            assertEquals("Test Product", products.last()[Products.name])
        }
    }
}
