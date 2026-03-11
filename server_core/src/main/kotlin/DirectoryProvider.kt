package com.quadrigasoftware

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

interface DirectoryProvider {
    /**
     * Search for users. The returned JsonObjects should include 'managerEmail' 
     * and 'reports' (list of emails) fields.
     */
    suspend fun searchUsers(query: String, fields: String? = null): List<JsonObject>

    /**
     * Get a single user by email.
     */
    suspend fun getUser(email: String): JsonObject?
}
