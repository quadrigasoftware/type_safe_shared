package com.quadrigasoftware

import io.ktor.client.*

/**
 * Orchestrates the selection, initialization, and caching of [DirectoryProvider] implementations.
 * 
 * This factory ensures that the correct data source (e.g., Google or Mock) is used based on 
 * the user's current session and that caching is consistently applied across the application.
 */
class DirectoryProviderFactory(
    private val httpClient: HttpClient,
    private val securityConfig: SecurityConfig
) {
    /**
     * Resolves and returns a [DirectoryProvider] appropriate for the provided [session].
     * 
     * - If [SecurityConfig.isMockEnabled] is true, returns a [MockDirectoryProvider].
     * - If the session provider is "google", returns a [GoogleDirectoryProvider].
     * - All real providers are automatically wrapped in a [CachingDirectoryProvider] 
     *   scoped to the user's organization domain.
     * 
     * @param session The current user session containing identity and tokens.
     * @return A ready-to-use [DirectoryProvider], or null if no provider can be resolved.
     */
    fun getProvider(session: MySession?): DirectoryProvider? {
        // 1. Check for Mock Mode
        if (securityConfig.isMockEnabled || session?.provider == "mock") {
            return CachingDirectoryProvider(MockDirectoryProvider(), "mock-org")
        }

        // 2. Validate Session
        val providerName = session?.provider ?: return null
        val token = session.accessToken ?: return null
        val email = session.email ?: return null

        // 3. Create real providers
        return when (providerName.lowercase()) {
            "google" -> {
                val domain = email.split("@").lastOrNull() ?: "unknown"
                CachingDirectoryProvider(
                    delegate = GoogleDirectoryProvider(httpClient, token),
                    cacheKey = domain
                )
            }
            // Future providers like "entra" or "okta" go here
            else -> null
        }
    }

    /**
     * Invalidates the directory cache.
     * 
     * @param cacheKey The specific domain/key to clear. If null, clears the entire cache.
     */
    fun clearCache(cacheKey: String? = null) {
        CachingDirectoryProvider.clearCache(cacheKey)
    }
}
