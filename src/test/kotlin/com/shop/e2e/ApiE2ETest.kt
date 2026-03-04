package com.shop.e2e

import com.shop.domain.table.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import com.shop.config.JwtConfig
import com.shop.plugins.*
import com.shop.controller.*
import com.shop.service.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ApiE2ETest {

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

    private fun ApplicationTestBuilder.configureTestApp() {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true; encodeDefaults = true })
            }
            install(StatusPages) {
                exception<IllegalArgumentException> { call, cause ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_request", "message" to (cause.message ?: "")))
                }
                exception<IllegalStateException> { call, cause ->
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "conflict", "message" to (cause.message ?: "")))
                }
                exception<NoSuchElementException> { call, cause ->
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found", "message" to (cause.message ?: "")))
                }
                exception<SecurityException> { call, cause ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized", "message" to (cause.message ?: "")))
                }
            }

            val authService = AuthService()
            val productService = ProductService()
            val orderService = OrderService()

            routing {
                authRoutes(authService)
                productRoutes(productService)
            }
        }
    }

    @Test
    @Order(1)
    fun `should register user and get token`() = testApplication {
        configureTestApp()
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"e2euser","email":"e2e@test.com","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("token"))
        assertTrue(body.contains("e2euser"))
    }

    @Test
    @Order(2)
    fun `should login with registered user`() = testApplication {
        configureTestApp()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"e2e@test.com","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("token"))
    }

    @Test
    @Order(3)
    fun `should get empty products list`() = testApplication {
        configureTestApp()
        val response = client.get("/products")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
