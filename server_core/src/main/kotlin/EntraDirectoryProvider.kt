package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * Implementation of DirectoryProvider for Microsoft Entra ID (formerly Azure AD)
 * using the Microsoft Graph API.
 */
internal class EntraDirectoryProvider(
    private val httpClient: HttpClient,
    private val accessToken: String
) : DirectoryProvider {

    private val graphBaseUrl = "https://graph.microsoft.com/v1.0"

    override suspend fun searchUsers(query: String, fields: String?): DirectoryResult<List<DirectoryUser>> {
        logger.info("Searching users in Microsoft Graph...")
        
        return try {
            // Entra search requires $search or $filter. For simple names, we use startsWith filter.
            val filter = if (query.isNotEmpty()) {
                "\$filter=startsWith(displayName,'$query') or startsWith(mail,'$query')"
            } else ""

            val response = httpClient.get("$graphBaseUrl/users?$filter&\$expand=manager,directReports") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header("ConsistencyLevel", "eventual") // Required for some Graph filters
            }

            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Graph API Error: ${response.body<String>()}", response.status)
            }

            val body = response.body<JsonObject>()
            val users = body["value"]?.jsonArray?.map { mapToDirectoryUser(it.jsonObject) } ?: emptyList()
            DirectoryResult.Success(users)
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to search Microsoft Graph: ${e.message}")
        }
    }

    override suspend fun getUser(email: String): DirectoryResult<DirectoryUser> {
        logger.info("Fetching user from Microsoft Graph: {}", email)
        
        return try {
            val response = httpClient.get("$graphBaseUrl/users/$email?\$expand=manager,directReports") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status == HttpStatusCode.NotFound) return DirectoryResult.NotFound
            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Graph API Error: ${response.body<String>()}", response.status)
            }

            DirectoryResult.Success(mapToDirectoryUser(response.body<JsonObject>()))
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to fetch user from Microsoft Graph: ${e.message}")
        }
    }

    override suspend fun getGroups(email: String): DirectoryResult<List<String>> {
        logger.info("Fetching group memberships from Microsoft Graph for: {}", email)
        
        return try {
            val response = httpClient.get("$graphBaseUrl/users/$email/memberOf") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }

            if (response.status != HttpStatusCode.OK) {
                return DirectoryResult.Error("Graph API Error: ${response.body<String>()}", response.status)
            }

            val body = response.body<JsonObject>()
            val groups = body["value"]?.jsonArray?.mapNotNull { 
                it.jsonObject["mail"]?.jsonPrimitive?.content 
            } ?: emptyList()
            
            DirectoryResult.Success(groups)
        } catch (e: Exception) {
            DirectoryResult.Error("Failed to fetch groups from Microsoft Graph: ${e.message}")
        }
    }

    private fun mapToDirectoryUser(user: JsonObject): DirectoryUser {
        val email = user["mail"]?.jsonPrimitive?.content ?: user["userPrincipalName"]?.jsonPrimitive?.content ?: ""
        
        val manager = user["manager"]?.jsonObject
        val managerEmail = manager?.get("mail")?.jsonPrimitive?.content ?: manager?.get("userPrincipalName")?.jsonPrimitive?.content

        val reports = user["directReports"]?.jsonArray?.mapNotNull { 
            it.jsonObject["mail"]?.jsonPrimitive?.content ?: it.jsonObject["userPrincipalName"]?.jsonPrimitive?.content
        } ?: emptyList()

        return DirectoryUser(
            email = email,
            firstName = user["givenName"]?.jsonPrimitive?.content ?: "",
            lastName = user["surname"]?.jsonPrimitive?.content ?: "",
            fullName = user["displayName"]?.jsonPrimitive?.content ?: email,
            title = user["jobTitle"]?.jsonPrimitive?.content,
            department = user["department"]?.jsonPrimitive?.content,
            orgUnitPath = user["officeLocation"]?.jsonPrimitive?.content, // Microsoft's closest equivalent
            managerEmail = managerEmail,
            reports = reports,
            groups = emptyList(), // Typically enriched on-demand
            floor = null,
            metadata = emptyMap()
        )
    }
}
