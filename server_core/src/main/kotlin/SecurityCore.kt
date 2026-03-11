package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.util.*

fun Application.configureCoreSecurity() {
    val securityConfig = loadSecurityConfig()

    // Convert the secret to a valid hex string if it isn't one already
    val hexSecret = try {
        hex(securityConfig.sessionSecret)
        securityConfig.sessionSecret
    } catch (e: Exception) {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(securityConfig.sessionSecret.toByteArray())
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
        for ((providerName, config) in securityConfig.providers) {
            val clientId = config.clientId
            val clientSecret = config.clientSecret

            if (!clientId.isNullOrBlank() && !clientSecret.isNullOrBlank()) {
                val configuredScopes = if (config.scopes.isNotEmpty()) config.scopes else listOf("openid", "profile", "email")

                oauth("auth-oauth-$providerName") {
                    urlProvider = { 
                        val host = request.host() + (if (request.port() != 80 && request.port() != 443) ":${request.port()}" else "")
                        val proto = request.header(HttpHeaders.XForwardedProto) ?: "http"
                        "$proto://$host/callback/$providerName"
                    }
                    providerLookup = {
                        OAuthServerSettings.OAuth2ServerSettings(
                            name = providerName,
                            authorizeUrl = config.authorizeUrl ?: "",
                            accessTokenUrl = config.accessTokenUrl ?: "",
                            requestMethod = HttpMethod.Post,
                            clientId = clientId,
                            clientSecret = clientSecret,
                            defaultScopes = configuredScopes,
                            extraAuthParameters = config.extraAuthParameters
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
    val securityConfig = application.loadSecurityConfig()
    val providerFactory = DirectoryProviderFactory(httpClient, securityConfig)
    
    configureCoreLoginRoutes(application)

    for ((providerName, config) in securityConfig.providers) {
        val clientId = config.clientId
        val clientSecret = config.clientSecret

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
                        if (securityConfig.isAllowed(email)) {
                            val session = call.sessions.get<MySession>() ?: MySession()
                            call.sessions.set(session.copy(
                                userId = payload?.get("sub")?.jsonPrimitive?.content ?: principal.accessToken.take(10),
                                provider = providerName,
                                userName = name,
                                email = email,
                                accessToken = principal.accessToken,
                                roles = emptySet(),
                                permissions = emptySet()
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

    // Prototype route to search Google Workspace users
    authenticate("auth-session") {
        get("/auth/google/users") {
            val session = call.sessions.get<MySession>()
            val provider = providerFactory.getProvider(session) ?: run {
                call.respond(HttpStatusCode.BadRequest, "No directory provider available")
                return@get
            }

            val query = call.request.queryParameters["q"] ?: ""
            val fields = call.request.queryParameters["fields"] ?: ""

            try {
                // Now returns a list of DirectoryUser objects directly
                val users = provider.searchUsers(query, fields)
                call.respond(buildJsonObject { 
                    put("users", Json.encodeToJsonElement(users)) 
                })
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to search users: ${e.message}")
            }
        }

        get("/auth/google/user") {
            val session = call.sessions.get<MySession>()
            val email = call.request.queryParameters["email"] ?: session?.email
            
            val provider = providerFactory.getProvider(session) ?: run {
                call.respond(HttpStatusCode.BadRequest, "No directory provider available")
                return@get
            }

            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, "Requires email")
                return@get
            }

            try {
                val user = provider.getUser(email)
                if (user != null) {
                    call.respond(user) // Ktor automatically serializes the data class
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching user: ${e.message}")
            }
        }

        post("/auth/directory/clear-cache") {
            val session = call.sessions.get<MySession>()
            val domain = session?.email?.split("@")?.lastOrNull()
            
            if (domain != null) {
                CachingDirectoryProvider.clearCache(domain)
                call.respondText("Cache cleared for domain: $domain")
            } else if (securityConfig.isMockEnabled) {
                CachingDirectoryProvider.clearCache("mock-org")
                call.respondText("Mock cache cleared")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Could not determine domain to clear")
            }
        }
    }
    get("/auth/list") {
        val securityConfig = call.application.loadSecurityConfig()
        val available = securityConfig.providers.filter { (name, config) ->
            !config.clientId.isNullOrBlank() || name == "mock"
        }.keys.toList()
        call.respond(available)
    }

    get("/login/mock") {
        // Simple HTML picker for mock users
        val usersHtml = MockUserStore.users.joinToString("") { user ->
            val email = user["primaryEmail"]?.jsonPrimitive?.content ?: ""
            val name = user["name"]?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: email
            "<li><a href='/mock/login?email=$email'>$name ($email)</a></li>"
        }
        
        call.respondText(
            "<html><body><h2>Select Mock User</h2><ul>$usersHtml</ul></body></html>",
            ContentType.Text.Html
        )
    }

    get("/logout") {
        call.sessions.clear<MySession>()
        call.respondRedirect("/")
    }

    get("/mock/login") {
        val email = call.request.queryParameters["email"] ?: "amina.el-amin@quadrigasoftware.com"
        val provider = call.request.queryParameters["provider"] ?: "google"
        
        val user = MockUserStore.users.find { it["primaryEmail"]?.jsonPrimitive?.content == email }
        val name = user?.get("name")?.jsonObject?.get("fullName")?.jsonPrimitive?.content ?: "Mock User"
        
        call.sessions.set(MySession(
            userId = email,
            userName = name,
            email = email,
            provider = provider,
            accessToken = "mock-token"
        ))
        // Since this is mock, we can just redirect to home
        call.respondRedirect("/")
    }
}

fun SecurityConfig.isAllowed(email: String?): Boolean {
    if (email == null) return false
    if (allowedEmails.isEmpty() && allowedDomains.isEmpty()) return true
    
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
