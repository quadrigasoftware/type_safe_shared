package com.quadrigasoftware

import io.ktor.client.*

/**
 * Factory responsible for creating and caching DirectoryProviders based on the user's session
 * and the application's security configuration.
 */
class DirectoryProviderFactory(
    private val httpClient: HttpClient,
    private val securityConfig: SecurityConfig
) {
    /**
     * Returns the appropriate DirectoryProvider for the given session.
     * Handles caching and mock-mode transitions automatically.
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
}
