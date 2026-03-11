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

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}
