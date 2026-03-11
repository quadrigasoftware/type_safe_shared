package com.quadrigasoftware

import kotlinx.serialization.json.JsonObject
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

/**
 * A wrapper that adds simple in-memory caching to any DirectoryProvider.
 */
class CachingDirectoryProvider(
    private val delegate: DirectoryProvider,
    private val cacheKey: String, // Usually the organization domain or access token hash
    private val ttlSeconds: Long = 300 // Default 5 minutes
) : DirectoryProvider {

    companion object {
        private val userCache = ConcurrentHashMap<String, CacheEntry<List<JsonObject>>>()

        fun clearCache(key: String? = null) {
            if (key != null) {
                userCache.remove(key)
            } else {
                userCache.clear()
            }
        }
    }

    private data class CacheEntry<T>(
        val data: T,
        val expiry: Instant
    )

    override suspend fun searchUsers(query: String, fields: String?): List<JsonObject> {
        val allUsers = getAllUsersCached()
        
        val queryLower = query.lowercase().trim()
        if (queryLower.isEmpty()) return allUsers

        return allUsers.filter { 
            it["primaryEmail"]?.toString()?.lowercase()?.contains(queryLower) == true ||
            it["name"]?.toString()?.lowercase()?.contains(queryLower) == true
        }
    }

    override suspend fun getUser(email: String): JsonObject? {
        val allUsers = getAllUsersCached()
        val emailLower = email.lowercase().trim()
        return allUsers.find { 
            it["primaryEmail"]?.toString()?.lowercase()?.trim()?.contains(emailLower) == true 
        }
    }

    private suspend fun getAllUsersCached(): List<JsonObject> {
        val now = Instant.now()
        val entry = userCache[cacheKey]

        if (entry != null && entry.expiry.isAfter(now)) {
            return entry.data
        }

        // Cache miss or expired
        val freshUsers = delegate.searchUsers("")
        userCache[cacheKey] = CacheEntry(freshUsers, now.plusSeconds(ttlSeconds))
        
        return freshUsers
    }
}
