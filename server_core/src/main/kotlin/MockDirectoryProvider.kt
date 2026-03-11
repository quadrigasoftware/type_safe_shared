package com.quadrigasoftware

import kotlinx.serialization.json.*

class MockDirectoryProvider : DirectoryProvider {

    override suspend fun searchUsers(query: String, fields: String?): List<JsonObject> {
        val queryLower = query.lowercase().trim()
        val filtered = if (queryLower.isEmpty()) {
            MockUserStore.users
        } else {
            MockUserStore.users.filter { 
                it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true ||
                it["name"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true
            }
        }
        
        return filtered.map { enrichUser(it) }
    }

    override suspend fun getUser(email: String): JsonObject? {
        val emailLower = email.lowercase().trim()
        val user = MockUserStore.users.find { it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.trim() == emailLower }
        return user?.let { enrichUser(it) }
    }

    private fun enrichUser(user: JsonObject): JsonObject {
        val email = user["primaryEmail"]?.jsonPrimitive?.content ?: ""
        
        // Find manager
        val managerEmail = user["relations"]?.jsonArray?.firstOrNull { 
            it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
        }?.jsonObject?.get("value")?.jsonPrimitive?.content ?: ""

        // Find reports
        val reports = MockUserStore.users.filter { other ->
            val otherManager = other["relations"]?.jsonArray?.firstOrNull { 
                it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
            }?.jsonObject?.get("value")?.jsonPrimitive?.content
            otherManager?.lowercase()?.trim() == email.lowercase().trim()
        }.map { it["primaryEmail"]?.jsonPrimitive?.content ?: "" }

        return buildJsonObject {
            user.forEach { (key, value) -> put(key, value) }
            put("managerEmail", managerEmail)
            put("reports", JsonArray(reports.map { JsonPrimitive(it) }))
        }
    }
}
