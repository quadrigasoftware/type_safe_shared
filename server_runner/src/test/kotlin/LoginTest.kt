package com.quadrigasoftware

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginTest {
    @Test
    fun testLoginRoute() = testApplication {
        application {
            module()
        }
        client.get("/login").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("Sign In"))
            assertTrue(bodyAsText().contains("Sign in with"))
        }
    }

    @Test
    fun testProtectedResourceRedirect() = testApplication {
        application {
            module()
        }
        // Assuming /protected/profile is a protected route
        client.get("/protected/profile").apply {
            // Depending on your auth configuration, this might be 401 or 302
            // In Security.kt, it uses 'authenticate("auth-session")', which usually returns 401 if no session
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testLogout() = testApplication {
        application {
            module()
        }
        client.get("/logout").apply {
            assertEquals(HttpStatusCode.Found, status)
            assertEquals("/", headers[HttpHeaders.Location])
        }
    }
    
    @Test
    fun testStatusPages() = testApplication {
        application {
            module()
        }
        client.get("/non-existent-route").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertTrue(bodyAsText().contains("404 - Page Not Found"))
            assertTrue(bodyAsText().contains("Go Home"))
        }
    }
}
