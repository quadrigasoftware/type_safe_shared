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
        private val userCache = ConcurrentHashMap<String, CacheEntry<List<DirectoryUser>>>()

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

    override suspend fun searchUsers(query: String, fields: String?): List<DirectoryUser> {
        val allUsers = getAllUsersCached()
        
        val queryLower = query.lowercase().trim()
        if (queryLower.isEmpty()) return allUsers

        return allUsers.filter { 
            it.email.lowercase().contains(queryLower) ||
            it.fullName.lowercase().contains(queryLower)
        }
    }

    override suspend fun getUser(email: String): DirectoryUser? {
        val allUsers = getAllUsersCached()
        val emailLower = email.lowercase().trim()
        return allUsers.find { 
            it.email.lowercase().trim() == emailLower 
        }
    }

    override suspend fun getGroups(email: String): List<String> {
        // We could cache groups specifically, but for now we delegate
        // Note: If groups are already enriched in the user object, we could return them from cache
        return delegate.getGroups(email)
    }

    private suspend fun getAllUsersCached(): List<DirectoryUser> {
        val now = Instant.now()
        val entry = userCache[cacheKey]

        if (entry != null && entry.expiry.isAfter(now)) {
            logger.debug("Cache hit for key: {}", cacheKey)
            return entry.data
        }

        // Cache miss or expired
        logger.info("Cache miss or expired for key: {}. Fetching fresh data...", cacheKey)
        val freshUsers = delegate.searchUsers("")
        userCache[cacheKey] = CacheEntry(freshUsers, now.plusSeconds(ttlSeconds))
        
        return freshUsers
    }
}
