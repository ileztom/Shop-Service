package com.shop.worker

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.shop.queue.RabbitMQProducer
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

object OrderEventWorker {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var job: Job? = null

    fun start() {
        if (!RabbitMQProducer.enabled) {
            logger.info("Worker disabled: RabbitMQ not available")
            return
        }

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val factory = ConnectionFactory().apply {
                    host = System.getenv("RABBITMQ_HOST") ?: "localhost"
                    port = (System.getenv("RABBITMQ_PORT") ?: "5672").toInt()
                    username = System.getenv("RABBITMQ_USER") ?: "guest"
                    password = System.getenv("RABBITMQ_PASSWORD") ?: "guest"
                }

                val connection = factory.newConnection()
                val channel = connection.createChannel()
                channel.queueDeclare("order_events", true, false, false, null)

                val deliverCallback = DeliverCallback { _, delivery ->
                    val message = String(delivery.body)
                    logger.info("[WORKER] Received event: $message")
                    sendFakeEmail(message)
                    channel.basicAck(delivery.envelope.deliveryTag, false)
                }

                val cancelCallback = CancelCallback { tag ->
                    logger.warn("[WORKER] Consumer cancelled: $tag")
                }

                channel.basicConsume("order_events", false, deliverCallback, cancelCallback)
                logger.info("[WORKER] Started consuming from order_events")

                while (isActive) {
                    delay(1000)
                }
            } catch (e: Exception) {
                logger.error("[WORKER] Error: ${e.message}")
            }
        }
    }

    private fun sendFakeEmail(eventData: String) {
        logger.info("[EMAIL-STUB] Sending email notification for event: $eventData")
        logger.info("[EMAIL-STUB] To: customer@example.com")
        logger.info("[EMAIL-STUB] Subject: Order Update")
        logger.info("[EMAIL-STUB] Body: Your order has been processed. Details: $eventData")
    }

    fun stop() {
        job?.cancel()
    }
}
