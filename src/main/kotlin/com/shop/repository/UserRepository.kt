package com.shop.repository

import com.shop.domain.model.UserDTO
import com.shop.domain.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object UserRepository {

    fun findByEmail(email: String): Pair<UserDTO, String>? = transaction {
        Users.select { Users.email eq email }
            .firstOrNull()
            ?.let {
                Pair(
                    UserDTO(
                        id = it[Users.id],
                        username = it[Users.username],
                        email = it[Users.email],
                        role = it[Users.role]
                    ),
                    it[Users.passwordHash]
                )
            }
    }

    fun findById(id: Int): UserDTO? = transaction {
        Users.select { Users.id eq id }
            .firstOrNull()
            ?.let {
                UserDTO(
                    id = it[Users.id],
                    username = it[Users.username],
                    email = it[Users.email],
                    role = it[Users.role]
                )
            }
    }

    fun create(username: String, email: String, password: String, role: String = "USER"): UserDTO = transaction {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val id = Users.insert {
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = hash
            it[Users.role] = role
        } get Users.id

        UserDTO(id = id, username = username, email = email, role = role)
    }

    fun existsByEmail(email: String): Boolean = transaction {
        Users.select { Users.email eq email }.count() > 0
    }

    fun existsByUsername(username: String): Boolean = transaction {
        Users.select { Users.username eq username }.count() > 0
    }

    fun verifyPassword(raw: String, hashed: String): Boolean = BCrypt.checkpw(raw, hashed)
}
