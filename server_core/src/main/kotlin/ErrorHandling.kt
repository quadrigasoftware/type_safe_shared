package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String, val status: Int)

/**
 * Configures global error handling for the application.
 * Maps domain exceptions to appropriate HTTP responses.
 */
fun Application.configureCoreErrorHandling() {
    install(StatusPages) {
        // Handle Directory-specific exceptions
        exception<DirectoryException> { call, cause ->
            call.respond(
                cause.status,
                ErrorResponse(cause.message ?: "Unknown directory error", cause.status.value)
            )
        }

        // Generic fallback for unhandled exceptions
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message ?: "An unexpected error occurred", HttpStatusCode.InternalServerError.value)
            )
        }
    }
}
