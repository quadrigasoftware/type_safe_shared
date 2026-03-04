package com.quadrigasoftware

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.sessions.*
import kotlinx.html.*

suspend fun ApplicationCall.respondHeaderExample() {
    val session = sessions.get<MySession>()
    
    respondHtml {
        head {
            title("Header Component Example")
            headerStyles()
            style {
                unsafe {
                    +"""
                        body {
                            font-family: sans-serif;
                            margin: 0;
                            background-color: #f0f2f5;
                        }
                        .content {
                            padding: 2rem;
                            max-width: 800px;
                            margin: 0 auto;
                            background: white;
                            margin-top: 2rem;
                            border-radius: 8px;
                            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        }
                    """.trimIndent()
                }
            }
        }
        body {
            loginStatusHeader(session, "Example Project")
            
            div("content") {
                h1 { +"Welcome to the Example Page" }
                p {
                    +"This page demonstrates the reusable header component from "
                    code { +"server_core" }
                    +"."
                }
                if (session == null) {
                    p { +"You are not logged in. Click the Login button in the header to sign in." }
                } else {
                    p { +"Hello, ${session.email ?: session.userId}! You are currently logged in." }
                }
            }
        }
    }
}
