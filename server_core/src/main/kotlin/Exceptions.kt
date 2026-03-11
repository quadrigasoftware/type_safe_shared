package com.quadrigasoftware

import io.ktor.http.*

/**
 * Base exception for all directory-related errors.
 */
open class DirectoryException(
    override val message: String,
    val status: HttpStatusCode = HttpStatusCode.InternalServerError
) : RuntimeException(message)

/**
 * Thrown when a specific user cannot be found in the directory.
 */
class UserNotFoundException(email: String) : 
    DirectoryException("User not found: $email", HttpStatusCode.NotFound)

/**
 * Thrown when a directory provider is not configured correctly or is missing required data.
 */
class ProviderConfigurationException(message: String) : 
    DirectoryException(message, HttpStatusCode.BadRequest)

/**
 * Thrown when an external provider (like Google) returns an error.
 */
class ExternalProviderException(provider: String, details: String) : 
    DirectoryException("Error from $provider: $details", HttpStatusCode.BadGateway)
