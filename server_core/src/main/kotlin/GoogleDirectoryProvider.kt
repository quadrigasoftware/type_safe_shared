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

    override suspend fun searchUsers(query: String, fields: String?): DirectoryResult<List<DirectoryUser>> {
        val allUsers = try {
            fetchAllUsers()
        } catch (e: ExternalProviderException) {
            return DirectoryResult.Error(e.message, e.status)
        }

        val queryLower = query.lowercase().trim()
        
        val filtered = if (queryLower.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { 
                it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true ||
                it["name"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true
            }
        }

        return DirectoryResult.Success(filtered.map { mapToDirectoryUser(it, allUsers) })
    }

    override suspend fun getUser(email: String): DirectoryResult<DirectoryUser> {
        val allUsers = try {
            fetchAllUsers()
        } catch (e: ExternalProviderException) {
            return DirectoryResult.Error(e.message, e.status)
        }

        val emailLower = email.lowercase().trim()
        val user = allUsers.find { it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.trim() == emailLower }
        
        return if (user != null) {
            DirectoryResult.Success(mapToDirectoryUser(user, allUsers))
        } else {
            DirectoryResult.NotFound
        }
    }

    override suspend fun getGroups(email: String): DirectoryResult<List<String>> {
        logger.info("Fetching groups for user: {}", email)
        val response = httpClient.get("https://admin.googleapis.com/admin/directory/v1/groups") {
            url {
                parameters.append("userKey", email)
            }
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (response.status == HttpStatusCode.NotFound) return DirectoryResult.NotFound
        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.body<String>()
            logger.error("Google Groups API error: Status={}, Body={}", response.status, errorBody)
            return DirectoryResult.Error("Google Groups API error: $errorBody", response.status)
        }

        val body = response.body<JsonObject>()
        val groups = body["groups"]?.jsonArray?.map { 
            it.jsonObject["email"]?.jsonPrimitive?.content ?: "" 
        } ?: emptyList()
        
        return DirectoryResult.Success(groups)
    }

    private suspend fun fetchAllUsers(): List<JsonObject> {
        logger.info("Fetching all users from Google Admin SDK...")
        val response = httpClient.get("https://admin.googleapis.com/admin/directory/v1/users") {
            url {
                parameters.append("customer", "my_customer")
                parameters.append("projection", "full")
            }
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.body<String>()
            logger.error("Google API error: Status={}, Body={}", response.status, errorBody)
            throw ExternalProviderException("Google", "Failed to fetch users: $errorBody")
        }

        val body = response.body<JsonObject>()
        val users = body["users"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
        logger.info("Successfully fetched {} users from Google.", users.size)
        return users
    }

    private fun mapToDirectoryUser(user: JsonObject, allUsers: List<JsonObject>): DirectoryUser {
        val email = user["primaryEmail"]?.jsonPrimitive?.content ?: ""
        val nameObj = user["name"]?.jsonObject
        
        // Find manager
        val managerEmail = user["relations"]?.jsonArray?.firstOrNull { 
            it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
        }?.jsonObject?.get("value")?.jsonPrimitive?.content

        // Find reports
        val reports = allUsers.filter { other ->
            val otherManager = other["relations"]?.jsonArray?.firstOrNull { 
                it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
            }?.jsonObject?.get("value")?.jsonPrimitive?.content
            otherManager?.lowercase()?.trim() == email.lowercase().trim()
        }.map { it["primaryEmail"]?.jsonPrimitive?.content ?: "" }

        return DirectoryUser(
            email = email,
            firstName = nameObj?.get("givenName")?.jsonPrimitive?.content ?: "",
            lastName = nameObj?.get("familyName")?.jsonPrimitive?.content ?: "",
            fullName = nameObj?.get("fullName")?.jsonPrimitive?.content ?: email,
            title = user["employeeTitle"]?.jsonPrimitive?.content,
            department = user["department"]?.jsonPrimitive?.content,
            orgUnitPath = user["orgUnitPath"]?.jsonPrimitive?.content,
            managerEmail = managerEmail,
            reports = reports,
            groups = emptyList(), // Groups are fetched on-demand or via specific enrichment
            floor = user["locations"]?.jsonObject?.get("floor")?.jsonPrimitive?.content
        )
    }
}
