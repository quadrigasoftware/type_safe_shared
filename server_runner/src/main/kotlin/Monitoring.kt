package com.quadrigasoftware

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureMonitoring() {
    routing {
        get("/hello") {
            call.respondText("Hello World!")
        }

        post("/post") {
            val postData = call.receiveText()
            call.respondText("Received: $postData")
        }
    }
}
