package com.quadrigasoftware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.withRole(role: String, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })
    authorizedRoute.intercept(ApplicationCallPipeline.Plugins) {
        val session = call.sessions.get<MySession>()
        if (session == null || role !in session.roles) {
            call.respond(HttpStatusCode.Forbidden, "Missing required role: $role")
            finish()
        }
    }
    authorizedRoute.build()
    return authorizedRoute
}

fun Route.withPermission(permission: String, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })
    authorizedRoute.intercept(ApplicationCallPipeline.Plugins) {
        val session = call.sessions.get<MySession>()
        if (session == null || permission !in session.permissions) {
            call.respond(HttpStatusCode.Forbidden, "Missing required permission: $permission")
            finish()
        }
    }
    authorizedRoute.build()
    return authorizedRoute
}
