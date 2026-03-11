package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.sessions.*
import kotlinx.html.*

import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String, val status: Int)

fun Application.configureCoreStatusPages() {
    install(StatusPages) {
        // 1. Handle Directory-specific exceptions (API focused)
        exception<DirectoryException> { call, cause ->
            call.respond(
                cause.status,
                ErrorResponse(cause.message, cause.status.value)
            )
        }

        // 2. Standard Global Exception Handler
        exception<Throwable> { call, cause ->
            val isApiRequest = call.request.path().startsWith("/auth/") || 
                               call.request.headers[HttpHeaders.Accept]?.contains("application/json") == true

            if (isApiRequest) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(cause.message ?: "An unexpected error occurred", HttpStatusCode.InternalServerError.value)
                )
            } else {
                val session = call.sessions.get<MySession>()
                call.respondHtml(HttpStatusCode.InternalServerError) {
                    head {
                        title("Error - 500")
                        headerStyles()
                        style {
                            unsafe {
                                +"""
                                    body { font-family: sans-serif; margin: 0; background-color: #f8f9fa; }
                                    .error-container { text-align: center; padding: 4rem 2rem; }
                                    h1 { font-size: 3rem; color: #dc3545; margin-bottom: 1rem; }
                                    p { font-size: 1.2rem; color: #666; margin-bottom: 2rem; }
                                    .btn-home { display: inline-block; padding: 0.75rem 1.5rem; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; transition: background 0.2s; }
                                    .btn-home:hover { background-color: #0056b3; }
                                    pre { text-align: left; background: #eee; padding: 1rem; border-radius: 4px; overflow-x: auto; max-width: 800px; margin: 2rem auto; }
                                """.trimIndent()
                            }
                        }
                    }
                    body {
                        loginStatusHeader(session, "Error")
                        div("error-container") {
                            h1 { +"500 - Internal Server Error" }
                            p { +"Oops! Something went wrong on our end." }
                            a(href = "/", classes = "btn-home") { +"Go Home" }
                        }
                    }
                }
            }
        }

        status(HttpStatusCode.NotFound) { call, status ->
            val session = call.sessions.get<MySession>()
            call.respondHtml(status) {
                head {
                    title("Page Not Found - 404")
                    headerStyles()
                    style {
                        unsafe {
                            +"""
                                body { font-family: sans-serif; margin: 0; background-color: #f8f9fa; }
                                .error-container { text-align: center; padding: 4rem 2rem; }
                                h1 { font-size: 3rem; color: #6c757d; margin-bottom: 1rem; }
                                p { font-size: 1.2rem; color: #666; margin-bottom: 2rem; }
                                .btn-home { display: inline-block; padding: 0.75rem 1.5rem; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; transition: background 0.2s; }
                                .btn-home:hover { background-color: #0056b3; }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    loginStatusHeader(session, "Not Found")
                    div("error-container") {
                        h1 { +"404 - Page Not Found" }
                        p { +"The page you are looking for does not exist." }
                        a(href = "/", classes = "btn-home") { +"Go Home" }
                    }
                }
            }
        }
        
        status(HttpStatusCode.Forbidden) { call, status ->
            val session = call.sessions.get<MySession>()
            call.respondHtml(status) {
                head {
                    title("Access Denied - 403")
                    headerStyles()
                    style {
                        unsafe {
                            +"""
                                body { font-family: sans-serif; margin: 0; background-color: #f8f9fa; }
                                .error-container { text-align: center; padding: 4rem 2rem; }
                                h1 { font-size: 3rem; color: #dc3545; margin-bottom: 1rem; }
                                p { font-size: 1.2rem; color: #666; margin-bottom: 2rem; }
                                .btn-home { display: inline-block; padding: 0.75rem 1.5rem; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; transition: background 0.2s; }
                                .btn-home:hover { background-color: #0056b3; }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    loginStatusHeader(session, "Access Denied")
                    div("error-container") {
                        h1 { +"403 - Access Denied" }
                        p { +"You do not have permission to access this resource." }
                        a(href = "/", classes = "btn-home") { +"Go Home" }
                    }
                }
            }
        }
    }
}
