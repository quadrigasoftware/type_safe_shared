package com.quadrigasoftware

interface DirectoryProvider {
    /**
     * Search for users.
     */
    suspend fun searchUsers(query: String, fields: String? = null): DirectoryResult<List<DirectoryUser>>

    /**
     * Get a single user by email.
     */
    suspend fun getUser(email: String): DirectoryResult<DirectoryUser>

    /**
     * Get groups for a specific user.
     */
    suspend fun getGroups(email: String): DirectoryResult<List<String>>
}
