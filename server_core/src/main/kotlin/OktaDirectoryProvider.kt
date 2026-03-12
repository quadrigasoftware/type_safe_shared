package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * Implementation of DirectoryProvider for Okta using the Okta Users and Groups APIs.
 */
internal class OktaDirectoryProvider(
    private val httpClient: HttpClient,
    private val accessToken: String,
    private val oktaDomain: String
) : DirectoryProvider {

    private val apiBaseUrl = "https://$oktaDomain/api/v1"

    override suspend fun searchUsers(query: String, fields: String?): DirectoryResult<List<DirectoryUser>> {
        logger.info("Searching users in Okta...")
        
        return try {
            val filter = if (query.isNotEmpty()) {
                // Okta search supports various filters. q is the most common for name/email.
                "?q=$query"
            } else ""

            val response = httpClient.get("$apiBaseUrl/users$filter") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Okta API Error: ${response.body<String>()}", response.status)
            }

            val users = response.body<JsonArray>().map { mapToDirectoryUser(it.jsonObject) }
            DirectoryResult.Success(users)
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to search Okta: ${e.message}")
        }
    }

    override suspend fun getUser(email: String): DirectoryResult<DirectoryUser> {
        logger.info("Fetching user from Okta: {}", email)
        
        return try {
            val response = httpClient.get("$apiBaseUrl/users/$email") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NotFound) return DirectoryResult.NotFound
            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Okta API Error: ${response.body<String>()}", response.status)
            }

            DirectoryResult.Success(mapToDirectoryUser(response.body<JsonObject>()))
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to fetch user from Okta: ${e.message}")
        }
    }

    override suspend fun getGroups(email: String): DirectoryResult<List<String>> {
        logger.info("Fetching group memberships from Okta for: {}", email)
        
        return try {
            // First get the user's Okta ID
            val userResult = getUser(email)
            if (userResult !is DirectoryResult.Success) return DirectoryResult.NotFound
            
            val userId = userResult.data.metadata["oktaId"] ?: return DirectoryResult.NotFound

            val response = httpClient.get("$apiBaseUrl/users/$userId/groups") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Okta API Error: ${response.body<String>()}", response.status)
            }

            val groups = response.body<JsonArray>().mapNotNull { 
                it.jsonObject["profile"]?.jsonObject?.get("name")?.jsonPrimitive?.content 
            }
            
            DirectoryResult.Success(groups)
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to fetch groups from Okta: ${e.message}")
        }
    }

    private fun mapToDirectoryUser(user: JsonObject): DirectoryUser {
        val profile = user["profile"]?.jsonObject
        val email = profile?.get("email")?.jsonPrimitive?.content ?: profile?.get("login")?.jsonPrimitive?.content ?: ""
        
        return DirectoryUser(
            email = email,
            firstName = profile?.get("firstName")?.jsonPrimitive?.content ?: "",
            lastName = profile?.get("lastName")?.jsonPrimitive?.content ?: "",
            fullName = profile?.get("displayName")?.jsonPrimitive?.content ?: "${profile?.get("firstName")?.jsonPrimitive?.content} ${profile?.get("lastName")?.jsonPrimitive?.content}",
            title = profile?.get("title")?.jsonPrimitive?.content,
            department = profile?.get("department")?.jsonPrimitive?.content,
            orgUnitPath = null, 
            managerEmail = profile?.get("manager")?.jsonPrimitive?.content,
            reports = emptyList(), // Okta typically doesn't provide reports list in the basic profile
            groups = emptyList(),
            floor = null,
            metadata = mapOf(
                "oktaId" to (user["id"]?.jsonPrimitive?.content ?: "")
            )
        )
    }
}
