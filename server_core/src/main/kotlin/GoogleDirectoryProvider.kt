package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class GoogleDirectoryProvider(
    private val httpClient: HttpClient,
    private val accessToken: String
) : DirectoryProvider {

    override suspend fun searchUsers(query: String, fields: String?): List<JsonObject> {
        val allUsers = fetchAllUsers()
        val queryLower = query.lowercase().trim()
        
        val filtered = if (queryLower.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { 
                it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true ||
                it["name"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true
            }
        }

        return filtered.map { enrichUser(it, allUsers) }
    }

    override suspend fun getUser(email: String): JsonObject? {
        val allUsers = fetchAllUsers()
        val emailLower = email.lowercase().trim()
        val user = allUsers.find { it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.trim() == emailLower }
        return user?.let { enrichUser(it, allUsers) }
    }

    private suspend fun fetchAllUsers(): List<JsonObject> {
        val response = httpClient.get("https://admin.googleapis.com/admin/directory/v1/users") {
            url {
                parameters.append("customer", "my_customer")
                parameters.append("projection", "full")
            }
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        return if (response.status == HttpStatusCode.OK) {
            response.body<JsonObject>()["users"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun enrichUser(user: JsonObject, allUsers: List<JsonObject>): JsonObject {
        val email = user["primaryEmail"]?.jsonPrimitive?.content ?: ""
        
        // Find manager
        val managerEmail = user["relations"]?.jsonArray?.firstOrNull { 
            it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
        }?.jsonObject?.get("value")?.jsonPrimitive?.content ?: ""

        // Find reports
        val reports = allUsers.filter { other ->
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
