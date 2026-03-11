package com.quadrigasoftware

import kotlinx.serialization.json.*

internal class MockDirectoryProvider : DirectoryProvider {

    override suspend fun searchUsers(query: String, fields: String?): DirectoryResult<List<DirectoryUser>> {
        val queryLower = query.lowercase().trim()
        val filtered = if (queryLower.isEmpty()) {
            MockUserStore.users
        } else {
            MockUserStore.users.filter { 
                it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true ||
                it["name"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content?.lowercase()?.contains(queryLower) == true
            }
        }
        
        return DirectoryResult.Success(filtered.map { mapToDirectoryUser(it) })
    }

    override suspend fun getUser(email: String): DirectoryResult<DirectoryUser> {
        val emailLower = email.lowercase().trim()
        val user = MockUserStore.users.find { it["primaryEmail"]?.jsonPrimitive?.content?.lowercase()?.trim() == emailLower }
        return if (user != null) {
            DirectoryResult.Success(mapToDirectoryUser(user))
        } else {
            DirectoryResult.NotFound
        }
    }

    override suspend fun getGroups(email: String): DirectoryResult<List<String>> {
        val userResult = getUser(email)
        return when (userResult) {
            is DirectoryResult.Success -> DirectoryResult.Success(userResult.data.groups)
            is DirectoryResult.NotFound -> DirectoryResult.NotFound
            is DirectoryResult.Error -> userResult
        }
    }

    private fun mapToDirectoryUser(user: JsonObject): DirectoryUser {
        val email = user["primaryEmail"]?.jsonPrimitive?.content ?: ""
        val nameObj = user["name"]?.jsonObject
        
        // Find manager
        val managerEmail = user["relations"]?.jsonArray?.firstOrNull { 
            it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
        }?.jsonObject?.get("value")?.jsonPrimitive?.content

        // Find reports
        val reports = MockUserStore.users.filter { other ->
            val otherManager = other["relations"]?.jsonArray?.firstOrNull { 
                it.jsonObject["type"]?.jsonPrimitive?.content?.equals("manager", ignoreCase = true) == true
            }?.jsonObject?.get("value")?.jsonPrimitive?.content
            otherManager?.lowercase()?.trim() == email.lowercase().trim()
        }.map { it["primaryEmail"]?.jsonPrimitive?.content ?: "" }

        val groups = user["groups"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

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
            groups = groups,
            floor = user["locations"]?.jsonObject?.get("floor")?.jsonPrimitive?.content
        )
    }
}
