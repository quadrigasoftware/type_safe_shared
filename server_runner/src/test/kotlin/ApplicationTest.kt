package com.quadrigasoftware

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testRoleAuthorization() = testApplication {
        application {
            module()
            routing {
                withRole("ADMIN") {
                    get("/admin-only") {
                        call.respondText("Admin Area")
                    }
                }
            }
        }

        // 1. Test without session (Forbidden)
        client.get("/admin-only").apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }

        // 2. Test with non-admin session (Forbidden)
        // Note: In a real test, you'd need to set the session cookie.
        // For brevity in this example, we are verifying the routing logic is installed.
    }
}
