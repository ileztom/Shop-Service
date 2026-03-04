package com.shop.queue

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

object RabbitMQProducer {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var connection: Connection? = null
    private var channel: Channel? = null
    private var queueName: String = "order_events"
    var enabled: Boolean = false
        private set

    fun init(environment: ApplicationEnvironment) {
        val host = environment.config.property("rabbitmq.host").getString()
        val port = environment.config.property("rabbitmq.port").getString().toInt()
        val username = environment.config.property("rabbitmq.username").getString()
        val password = environment.config.property("rabbitmq.password").getString()
        queueName = environment.config.property("rabbitmq.queue").getString()
        enabled = environment.config.property("rabbitmq.enabled").getString().toBoolean()

        if (!enabled) {
            logger.info("RabbitMQ is disabled, messages will be logged only")
            return
        }

        try {
            val factory = ConnectionFactory().apply {
                this.host = host
                this.port = port
                this.username = username
                this.password = password
                connectionTimeout = 5000
            }
            connection = factory.newConnection()
            channel = connection?.createChannel()
            channel?.queueDeclare(queueName, true, false, false, null)
            logger.info("RabbitMQ connected, queue: $queueName")
        } catch (e: Exception) {
            logger.warn("RabbitMQ not available: ${e.message}")
            enabled = false
        }
    }

    fun publish(message: String) {
        if (enabled) {
            try {
                channel?.basicPublish("", queueName, null, message.toByteArray())
                logger.info("Message published to $queueName")
            } catch (e: Exception) {
                logger.error("Failed to publish message: ${e.message}")
            }
        } else {
            logger.info("[MQ-MOCK] Message: $message")
        }
    }

    fun close() {
        try {
            channel?.close()
            connection?.close()
        } catch (e: Exception) {
            logger.warn("Error closing RabbitMQ: ${e.message}")
        }
    }
}
