package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.webjars.*
import kotlinx.serialization.Serializable
import kotlin.random.Random

fun Application.configureRouting() {
    install(Resources)
    // StatusPages is now configured in configureCoreStatusPages()
    install(Webjars) {
        path = "/webjars" //defaults to /webjars
    }
    routing {
        get("/header-example") {
            call.respondHeaderExample()
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        staticResources("/static", "static")
        get("/webjars") {
            call.respondText("<script src='/webjars/jquery/jquery.js'></script>", ContentType.Text.Html)
        }
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
