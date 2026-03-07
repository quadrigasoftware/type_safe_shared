package com.quadrigasoftware

import kotlinx.serialization.Serializable

@Serializable
data class UserRecord(
    val email: String,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap()
)

interface UserRepository {
    suspend fun getUser(email: String): UserRecord?
    suspend fun saveUser(user: UserRecord)
    suspend fun deleteUser(email: String)
    suspend fun getAllUsers(): List<UserRecord>
}
