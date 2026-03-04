package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Routing.configureCoreLoginRoutes(application: Application) {
    route("/login") {
        get {
            val session = call.sessions.get<MySession>()
            
            if (session?.userId != null) {
                call.respondHtml {
                    head {
                        title("Login - Already Signed In")
                        style {
                            unsafe {
                                +"""
                                body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f2f5; }
                                .login-card { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; min-width: 300px; }
                                .user-info { margin-bottom: 1.5rem; text-align: left; background: #f8f9fa; padding: 1rem; border-radius: 4px; border: 1px solid #eee; }
                                .user-info div { margin: 0.5rem 0; }
                                .label { font-weight: bold; color: #666; font-size: 0.8rem; text-transform: uppercase; }
                                .value { color: #333; font-family: monospace; }
                                .btn { display: block; width: 100%; padding: 0.75rem; margin: 0.5rem 0; border-radius: 4px; text-decoration: none; font-weight: bold; text-align: center; }
                                .btn-logout { background-color: #dc3545; color: white; border: none; cursor: pointer; }
                                .btn-logout:hover { background-color: #c82333; }
                                .btn-home { background-color: #f8f9fa; color: #333; border: 1px solid #ddd; }
                                """.trimIndent()
                            }
                        }
                    }
                    body {
                        div("login-card") {
                            h2 { +"Signed In" }
                            div("user-info") {
                                val providerLabel = session.provider?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                                val username = session.email ?: session.userName ?: session.userId ?: "User"
                                val displayUser = "$username ($providerLabel Login)"

                                div {
                                    div("label") { +"Account" }
                                    div("value") { +displayUser }
                                }
                                div {
                                    div("label") { +"User ID" }
                                    div("value") { +(session.userId) }
                                }
                            }
                            a(href = "/", classes = "btn btn-home") { +"Go to Leaderboard" }
                            form(action = "/logout", method = FormMethod.get) {
                                button(classes = "btn btn-logout") { +"Logout" }
                            }
                        }
                    }

                }
                return@get
            }

            val authProvidersList = try {
                application.environment.config.config("auth.providers")
            } catch (e: Exception) {
                null
            }

            val availableProviders = authProvidersList?.keys()?.map { it.split('.').first() }?.distinct()?.filter {
                val config = authProvidersList.config(it)
                !config.propertyOrNull("clientId")?.getString().isNullOrBlank()
            } ?: emptyList()

            // If only one provider, redirect immediately
            if (availableProviders.size == 1) {

                call.respondRedirect("/login/${availableProviders.first()}")
                return@get
            }

            call.respondHtml {
                head {
                    title("Login")
                    style {
                        unsafe {
                            +"""
                            body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f2f5; }
                            .login-card { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; min-width: 300px; }
                            .login-button { display: block; width: 100%; padding: 0.75rem; margin: 0.5rem 0; border: 1px solid #ddd; border-radius: 4px; text-decoration: none; color: #333; font-weight: bold; transition: background-color 0.2s; }
                            .login-button:hover { background-color: #f8f9fa; }
                            .login-button.google { background-color: #fff; border-color: #4285f4; color: #4285f4; }
                            .login-button.entra { background-color: #00a4ef; border-color: #00a4ef; color: white; }
                            .login-button.okta { background-color: #007dc1; border-color: #007dc1; color: white; }
                        """.trimIndent()
                        }
                    }
                }
                body {
                    div("login-card") {
                        h2 { +"Sign In" }
                        if (availableProviders.isEmpty()) {
                            p { +"No authentication methods configured." }
                        } else {
                            availableProviders.forEach { provider ->
                                val providerClass = provider.lowercase()
                                val displayName = provider.replaceFirstChar { it.uppercase() }
                                a(href = "/login/$provider", classes = "login-button $providerClass") {
                                    +"Sign in with $displayName"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
