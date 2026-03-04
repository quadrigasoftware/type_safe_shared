package com.quadrigasoftware

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application.configureSecurity() {
    configureCoreSecurity()

    // Add digest auth if still needed for this specific runner
    authentication {
        digest("myDigestAuth") {
            digestProvider { userName, _ ->
                if (userName == "test") hex("fb12475e62dedc5c2744d98eb73b8877") else null
            }
        }
    }

    routing {
        configureCoreAuthRoutes(this@configureSecurity)

        authenticate("auth-session") {
            get("/protected/profile") {
                val session = call.principal<MySession>()!!
                call.respond(mapOf(
                    "message" to "This is a protected route",
                    "user" to session.userName,
                    "email" to session.email,
                    "provider" to session.provider
                ))
            }
        }

        authenticate("myDigestAuth") {
            get("/protected/route/digest") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }

        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. User: ${session.userName ?: "Anonymous"} (${session.email ?: "No Email"})")
        }
    }
}
