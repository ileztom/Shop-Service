package com.shop

import com.shop.cache.RedisCache
import com.shop.config.DatabaseConfig
import com.shop.config.JwtConfig
import com.shop.plugins.*
import com.shop.queue.RabbitMQProducer
import com.shop.worker.OrderEventWorker
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import io.ktor.server.config.*
import org.slf4j.LoggerFactory

fun main() {
    val env = applicationEngineEnvironment {
        config = HoconApplicationConfig(com.typesafe.config.ConfigFactory.load())
        connector {
            port = System.getenv("PORT")?.toIntOrNull() ?: 5000
            host = "0.0.0.0"
        }
        module { module() }
    }
    embeddedServer(Netty, env).start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    JwtConfig.init(environment)
    DatabaseConfig.init(environment)

    try {
        RedisCache.init(environment)
    } catch (e: Exception) {
        logger.warn("Redis initialization failed, running without cache: ${e.message}")
    }

    try {
        RabbitMQProducer.init(environment)
    } catch (e: Exception) {
        logger.warn("RabbitMQ initialization failed, running without queue: ${e.message}")
    }

    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureSwagger()
    configureRouting()

    OrderEventWorker.start()

    environment.monitor.subscribe(ApplicationStopped) {
        OrderEventWorker.stop()
        RabbitMQProducer.close()
    }

    logger.info("Shop Service started on port ${System.getenv("PORT") ?: "5000"}")
}
