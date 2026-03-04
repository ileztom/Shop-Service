package com.shop.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.net.URI

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(environment: ApplicationEnvironment) {
        val dbUrl = environment.config.property("database.url").getString()
        val dbUser = environment.config.property("database.user").getString()
        val dbPassword = environment.config.property("database.password").getString()

        val jdbcUrl: String
        val finalUser: String
        val finalPassword: String

        if (dbUrl.startsWith("jdbc:")) {
            jdbcUrl = dbUrl
            finalUser = dbUser
            finalPassword = dbPassword
        } else {
            val uri = URI(dbUrl)
            val userInfo = uri.userInfo?.split(":") ?: listOf(dbUser, dbPassword)
            finalUser = userInfo.getOrElse(0) { dbUser }
            finalPassword = userInfo.getOrElse(1) { dbPassword }
            val queryString = if (uri.query != null) "?${uri.query}" else ""
            jdbcUrl = "jdbc:postgresql://${uri.host}:${if (uri.port > 0) uri.port else 5432}${uri.path}$queryString"
        }

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = finalUser
            this.password = finalPassword
            this.driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 30000
            connectionTimeout = 30000
            maxLifetime = 600000
            isAutoCommit = false
        }

        val dataSource = HikariDataSource(hikariConfig)

        runMigrations(jdbcUrl, finalUser, finalPassword)

        Database.connect(dataSource)
        logger.info("Database connected successfully")
    }

    private fun runMigrations(url: String, user: String, password: String) {
        try {
            val flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
            flyway.migrate()
            logger.info("Database migrations completed")
        } catch (e: Exception) {
            logger.error("Flyway migration failed: ${e.message}", e)
            throw e
        }
    }
}
