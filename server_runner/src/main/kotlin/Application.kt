package com.quadrigasoftware

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlin.random.Random

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configServerCore()
    configureSerialization()
    configureMonitoring()
    configureTemplating()
    configureRouting()
    configureCoreStatusPages()

    routing {
        get("/") {
            val session = call.sessions.get<MySession>()
            call.respondHtml {
                leaderboardPage(Random.Default, session)
            }
        }
        staticResources("/", "web")
    }
}
