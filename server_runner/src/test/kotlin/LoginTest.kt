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
            val body = bodyAsText()
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(body.contains("Sign In"), "Body should contain 'Sign In'")
            // Note: If no auth providers are configured in the test environment, 
            // the 'Sign in with' buttons won't appear.
        }
    }

    @Test
    fun testProtectedResourceRedirect() = testApplication {
        application {
            module()
        }
        client.get("/protected/profile").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testLogout() = testApplication {
        application {
            module()
        }
        // Disable follow redirects to test the 302 redirect to home
        val noRedirectClient = createClient {
            followRedirects = false
        }
        noRedirectClient.get("/logout").apply {
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
