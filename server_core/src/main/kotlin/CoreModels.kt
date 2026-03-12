package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class MySession(
    val count: Int = 0,
    val userId: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val provider: String? = null,
    val accessToken: String? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * A type-safe representation of a user within the organizational directory.
 * 
 * This model abstracts away the specific schemas of external providers (like Google or Microsoft)
 * and provides a consistent set of fields for use across the application.
 */
@Serializable
data class DirectoryUser(
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val title: String? = null,
    val department: String? = null,
    val orgUnitPath: String? = null,
    val managerEmail: String? = null,
    /** Emails of users who report directly to this user. */
    val reports: List<String> = emptyList(),
    /** Emails of groups this user is a member of. */
    val groups: List<String> = emptyList(),
    val floor: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Represents the result of a directory operation.
 */
sealed class DirectoryResult<out T> {
    /** Indicates the operation was successful and returned [data]. */
    data class Success<T>(val data: T) : DirectoryResult<T>()
    
    /** Indicates the operation failed with a specific [message] and [status]. */
    data class Error(val message: String, val status: HttpStatusCode = HttpStatusCode.InternalServerError) : DirectoryResult<Nothing>()
    
    /** Indicates the requested resource (e.g., a user) could not be found. */
    object NotFound : DirectoryResult<Nothing>()

    /**
     * Unwraps the result, returning data on success or throwing a [DirectoryException] on failure.
     * 
     * Useful for thin routes where global error handling is configured.
     * @throws DirectoryException if the result is Error or NotFound.
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is NotFound -> throw DirectoryException("Resource not found", HttpStatusCode.NotFound)
            is Error -> throw DirectoryException(message, status)
        }
    }
}

/**
 * Togglable application features.
 */
@Serializable
data class AppFeatures(
    val leaderboard: Boolean = true,
    val orgChart: Boolean = true,
    val search: Boolean = true,
    val directoryCache: Boolean = true
)

data class AuthProviderConfig(
    val name: String,
    val clientId: String?,
    val clientSecret: String?,
    val authorizeUrl: String?,
    val accessTokenUrl: String?,
    val scopes: List<String> = emptyList(),
    val extraAuthParameters: List<Pair<String, String>> = emptyList()
)

data class SecurityConfig(
    val sessionSecret: String,
    val providers: Map<String, AuthProviderConfig>,
    val allowedEmails: Set<String>,
    val allowedDomains: Set<String>,
    val features: AppFeatures = AppFeatures(),
    val isMockEnabled: Boolean = System.getenv("MOCK_AUTH") == "true"
)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}
