package com.shop.repository

import com.shop.domain.table.AuditLogs
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object AuditLogRepository {

    fun log(userId: Int?, action: String, entityType: String, entityId: Int?, details: String? = null) = transaction {
        AuditLogs.insert {
            it[AuditLogs.userId] = userId
            it[AuditLogs.action] = action
            it[AuditLogs.entityType] = entityType
            it[AuditLogs.entityId] = entityId
            it[AuditLogs.details] = details
        }
    }
}
