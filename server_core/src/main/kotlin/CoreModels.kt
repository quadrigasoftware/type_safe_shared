package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
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
    val reports: List<String> = emptyList(),
    val floor: String? = null,
    val metadata: Map<String, String> = emptyMap()
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
    val isMockEnabled: Boolean = System.getenv("MOCK_AUTH") == "true"
)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}
