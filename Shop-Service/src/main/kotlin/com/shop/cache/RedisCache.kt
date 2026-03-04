package com.shop.cache

import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisCache {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var jedisPool: JedisPool? = null
    var ttlSeconds: Int = 300
        private set
    private var enabled: Boolean = false
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun init(environment: ApplicationEnvironment) {
        val host = environment.config.property("redis.host").getString()
        val port = environment.config.property("redis.port").getString().toInt()
        ttlSeconds = environment.config.property("redis.ttl").getString().toInt()

        try {
            val poolConfig = JedisPoolConfig().apply {
                maxTotal = 10
                maxIdle = 5
                testOnBorrow = true
            }
            jedisPool = JedisPool(poolConfig, host, port, 2000)
            jedisPool?.resource?.use { it.ping() }
            enabled = true
            logger.info("Redis connected at $host:$port")
        } catch (e: Exception) {
            logger.warn("Redis not available, caching disabled: ${e.message}")
            enabled = false
        }
    }

    fun set(key: String, value: String, ttl: Int = ttlSeconds) {
        if (!enabled) return
        try {
            jedisPool?.resource?.use { jedis ->
                jedis.setex(key, ttl.toLong(), value)
            }
        } catch (e: Exception) {
            logger.warn("Redis SET failed for key $key: ${e.message}")
        }
    }

    fun get(key: String): String? {
        if (!enabled) return null
        return try {
            jedisPool?.resource?.use { jedis ->
                jedis.get(key)
            }
        } catch (e: Exception) {
            logger.warn("Redis GET failed for key $key: ${e.message}")
            null
        }
    }

    fun delete(key: String) {
        if (!enabled) return
        try {
            jedisPool?.resource?.use { jedis ->
                jedis.del(key)
            }
        } catch (e: Exception) {
            logger.warn("Redis DELETE failed for key $key: ${e.message}")
        }
    }

    fun deleteByPattern(pattern: String) {
        if (!enabled) return
        try {
            jedisPool?.resource?.use { jedis ->
                val keys = jedis.keys(pattern)
                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                }
            }
        } catch (e: Exception) {
            logger.warn("Redis DELETE pattern failed: ${e.message}")
        }
    }

    inline fun <reified T> getOrSet(key: String, ttl: Int = ttlSeconds, loader: () -> T): T {
        val cached = get(key)
        if (cached != null) {
            return json.decodeFromString<T>(cached)
        }
        val value = loader()
        set(key, json.encodeToString(value), ttl)
        return value
    }
}
