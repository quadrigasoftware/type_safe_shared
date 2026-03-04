package com.quadrigasoftware

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureCoreSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
