package com.quadrigasoftware

/**
 * Common interface for retrieving organizational directory information.
 * Supports different backend implementations such as Google Workspace or Mock data.
 */
interface DirectoryProvider {
    /**
     * Search for users in the organization based on a query string.
     * 
     * @param query The search string (matched against name or email).
     * @param fields Optional filter for specific fields (API implementation specific).
     * @return [DirectoryResult] containing a list of matching [DirectoryUser] objects.
     */
    suspend fun searchUsers(query: String, fields: String? = null): DirectoryResult<List<DirectoryUser>>

    /**
     * Retrieve a detailed profile for a single user.
     * 
     * @param email The primary email address of the user.
     * @return [DirectoryResult] containing the [DirectoryUser], [DirectoryResult.NotFound], or [DirectoryResult.Error].
     */
    suspend fun getUser(email: String): DirectoryResult<DirectoryUser>

    /**
     * Retrieve the list of group emails that a specific user belongs to.
     * 
     * @param email The primary email address of the user.
     * @return [DirectoryResult] containing a list of group email strings.
     */
    suspend fun getGroups(email: String): DirectoryResult<List<String>>
}
