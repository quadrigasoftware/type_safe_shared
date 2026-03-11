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

    override suspend fun searchUsers(query: String, fields: String?): DirectoryResult<List<DirectoryUser>> {
        val allUsersResult = getAllUsersCached()
        return when (allUsersResult) {
            is DirectoryResult.Success -> {
                val queryLower = query.lowercase().trim()
                val filtered = if (queryLower.isEmpty()) {
                    allUsersResult.data
                } else {
                    allUsersResult.data.filter { 
                        it.email.lowercase().contains(queryLower) ||
                        it.fullName.lowercase().contains(queryLower)
                    }
                }
                DirectoryResult.Success(filtered)
            }
            is DirectoryResult.NotFound -> DirectoryResult.NotFound
            is DirectoryResult.Error -> allUsersResult
        }
    }

    override suspend fun getUser(email: String): DirectoryResult<DirectoryUser> {
        val allUsersResult = getAllUsersCached()
        return when (allUsersResult) {
            is DirectoryResult.Success -> {
                val emailLower = email.lowercase().trim()
                val user = allUsersResult.data.find { it.email.lowercase().trim() == emailLower }
                if (user != null) DirectoryResult.Success(user) else DirectoryResult.NotFound
            }
            is DirectoryResult.NotFound -> DirectoryResult.NotFound
            is DirectoryResult.Error -> allUsersResult
        }
    }

    override suspend fun getGroups(email: String): DirectoryResult<List<String>> {
        return delegate.getGroups(email)
    }

    private suspend fun getAllUsersCached(): DirectoryResult<List<DirectoryUser>> {
        val now = Instant.now()
        val entry = userCache[cacheKey]

        if (entry != null && entry.expiry.isAfter(now)) {
            logger.debug("Cache hit for key: {}", cacheKey)
            return DirectoryResult.Success(entry.data)
        }

        // Cache miss or expired
        logger.info("Cache miss or expired for key: {}. Fetching fresh data...", cacheKey)
        return when (val freshResult = delegate.searchUsers("")) {
            is DirectoryResult.Success -> {
                userCache[cacheKey] = CacheEntry(freshResult.data, now.plusSeconds(ttlSeconds))
                freshResult
            }
            else -> freshResult
        }
    }
}
