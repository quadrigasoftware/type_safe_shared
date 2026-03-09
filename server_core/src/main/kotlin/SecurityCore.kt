package com.quadrigasoftware

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

@Serializable
data class MySession(
    val count: Int = 0,
    val userId: String? = null,
    val userName: String? = null,
    val email: String? = null,
    val provider: String? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap()
)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    }

    fun Application.configureCoreSecurity() {
    val sessionSecret = environment.config.propertyOrNull("auth.session.secret")?.getString() 
        ?: "00112233445566778899aabbccddeeff"

    // Convert the secret to a valid hex string if it isn't one already
    val hexSecret = try {
        hex(sessionSecret)
        sessionSecret
    } catch (e: Exception) {
        // If not valid hex, hash it to create a stable hex key
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(sessionSecret.toByteArray())
        hash.joinToString("") { "%02x".format(it) }
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"
            transform(SessionTransportTransformerMessageAuthentication(hex(hexSecret)))
        }
    }

    authentication {
        val authProviders = try {
            this@configureCoreSecurity.environment.config.config("auth.providers")
        } catch (e: Exception) {
            null
        }

        val providerNames = authProviders?.keys()?.map { it.split('.').first() }?.distinct() ?: emptyList()

        for (providerName in providerNames) {
            val config = try { authProviders?.config(providerName) } catch (e: Exception) { null }
            if (config == null) continue

            val clientId = config.propertyOrNull("clientId")?.getString()
            val clientSecret = config.propertyOrNull("clientSecret")?.getString()
            
            if (!clientId.isNullOrBlank() && !clientSecret.isNullOrBlank()) {
                val extraParamsConfig = try { config.config("extraAuthParameters") } catch (e: Exception) { null }
                val extraParams = extraParamsConfig?.keys()?.map { key ->
                    key to extraParamsConfig.property(key).getString()
                } ?: emptyList()

                oauth("auth-oauth-$providerName") {
                    urlProvider = { "http://localhost:8080/callback/$providerName" }
                    providerLookup = {
                        OAuthServerSettings.OAuth2ServerSettings(
                            name = providerName,
                            authorizeUrl = config.propertyOrNull("authorizeUrl")?.getString() ?: "",
                            accessTokenUrl = config.propertyOrNull("accessTokenUrl")?.getString() ?: "",
                            requestMethod = HttpMethod.Post,
                            clientId = clientId,
                            clientSecret = clientSecret,
                            defaultScopes = listOf("openid", "profile", "email"),
                            extraAuthParameters = extraParams
                        )
                    }
                    client = httpClient
                }
            }
        }

        session<MySession>("auth-session") {
            validate { session ->
                if (session.userId != null) session else null
            }
        }
    }
}

fun Routing.configureCoreAuthRoutes(application: Application) {
    val authProviders = try {
        application.environment.config.config("auth.providers")
    } catch (e: Exception) {
        null
    }
    val providerNames = authProviders?.keys()?.map { it.split('.').first() }?.distinct() ?: emptyList()
    
    configureCoreLoginRoutes(application)

    val userRepository: UserRepository = try {
        FirestoreUserRepository()
    } catch (e: Exception) {
        // Fallback for local development if GCP credentials are not available
        object : UserRepository {
            override suspend fun getUser(email: String): UserRecord? = null
            override suspend fun saveUser(user: UserRecord) {}
            override suspend fun deleteUser(email: String) {}
            override suspend fun getAllUsers(): List<UserRecord> = emptyList()
        }
    }

    for (providerName in providerNames) {
        val config = try { authProviders?.config(providerName) } catch (e: Exception) { null }
        val clientId = config?.propertyOrNull("clientId")?.getString()
        val clientSecret = config?.propertyOrNull("clientSecret")?.getString()

        if (!clientId.isNullOrBlank() && !clientSecret.isNullOrBlank()) {
            authenticate("auth-oauth-$providerName") {
                get("/login/$providerName") {
                }

                get("/callback/$providerName") {
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    if (principal != null) {
                        val idToken = principal.extraParameters["id_token"]
                        val payload = idToken?.let { decodeJwtPayload(it) }
                        
                        val email = payload?.get("email")?.jsonPrimitive?.content
                        val name = payload?.get("name")?.jsonPrimitive?.content ?: "OAuth User"

                        // Validate Allow List
                        if (application.isAllowed(email)) {
                            val userRecord = email?.let { userRepository.getUser(it) }
                            val session = call.sessions.get<MySession>() ?: MySession()
                            call.sessions.set(session.copy(
                                userId = payload?.get("sub")?.jsonPrimitive?.content ?: principal.accessToken.take(10),
                                provider = providerName,
                                userName = name,
                                email = email,
                                roles = userRecord?.roles ?: emptySet(),
                                permissions = userRecord?.permissions ?: emptySet()
                            ))
                            call.respondRedirect("/")
                        } else {
                            call.respondText("Access Denied: Your email ($email) is not on the allow list.", status = HttpStatusCode.Forbidden)
                        }
                    } else {
                        call.respondText("Login failed", status = HttpStatusCode.Unauthorized)
                    }
                }
            }
        }
    }

    get("/me") {
        val session = call.sessions.get<MySession>()
        if (session != null) {
            call.respond(session)
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Not logged in")
        }
    }

    get("/auth/list") {
        val authProvidersList = try {
            application.environment.config.config("auth.providers")
        } catch (e: Exception) {
            null
        }
        val available = authProvidersList?.keys()?.map { it.split('.').first() }?.distinct()?.filter {
            val config = authProvidersList.config(it)
            !config.propertyOrNull("clientId")?.getString().isNullOrBlank()
        } ?: emptyList()
        call.respond(available)
    }

    get("/logout") {
        call.sessions.clear<MySession>()
        call.respondRedirect("/")
    }
}

private fun Application.isAllowed(email: String?): Boolean {
    if (email == null) return false
    val config = try { environment.config.config("auth.allowList") } catch (e: Exception) { return true }
    
    val allowedEmails = config.propertyOrNull("emails")?.getList() ?: emptyList()
    val allowedDomains = config.propertyOrNull("domains")?.getList() ?: emptyList()
    
    if (email in allowedEmails) return true
    if (allowedDomains.any { domain -> email.endsWith("@$domain") }) return true
    
    return false
}

private fun decodeJwtPayload(token: String): JsonObject? {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return null
        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        Json.decodeFromString<JsonObject>(payload)
    } catch (e: Exception) {
        null
    }
}
