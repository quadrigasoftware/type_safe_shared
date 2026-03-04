package com.quadrigasoftware

import kotlinx.css.*
import kotlinx.css.properties.BoxShadow
import kotlinx.css.properties.TextDecoration
import kotlinx.css.properties.Transition
import kotlinx.css.properties.s
import kotlinx.html.*

// Helper extension to use CSS DSL inside style tag
fun STYLE.cssRules(block: CssBuilder.() -> Unit) {
    unsafe {
        +CssBuilder().apply(block).toString()
    }
}

fun HEAD.headerStyles() {
    style {
        cssRules {
            rule(".ts-header") {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                alignItems = Align.center
                paddingTop = 1.rem
                paddingBottom = 1.rem
                paddingLeft = 2.rem
                paddingRight = 2.rem
                backgroundColor = Color("#333")
                color = Color.white
                boxShadow += BoxShadow(Color("rgba(0,0,0,0.2)"), 0.px, 2.px, 5.px)
                position = Position.relative
                zIndex = 100
                boxSizing = BoxSizing.borderBox
            }
            rule(".ts-header .left-section") {
                display = Display.flex
                alignItems = Align.center
                gap = 1.rem
            }
            rule(".ts-header .brand h3") {
                margin = Margin(0.px)
                fontFamily = "sans-serif"
            }
            
            rule(".ts-btn-nav") {
                paddingTop = 0.5.rem
                paddingBottom = 0.5.rem
                paddingLeft = 1.rem
                paddingRight = 1.rem
                borderRadius = 4.px
                textDecoration = TextDecoration.none
                fontWeight = FontWeight.bold
                color = Color.white
                backgroundColor = Color("#555")
                border = Border.none
                fontSize = 0.9.rem
                fontFamily = "sans-serif"
                transition += Transition("background", 0.2.s)
                display = Display.inlineBlock
            }
            rule(".ts-btn-nav:hover") {
                backgroundColor = Color("#666")
            }
            rule(".ts-btn-login") {
                backgroundColor = Color("#28a745")
            }
            rule(".ts-btn-login:hover") {
                backgroundColor = Color("#218838")
            }

            // Hamburger Menu Styles
            rule(".ts-hamburger-btn") {
                fontSize = 1.5.rem
                backgroundColor = Color.transparent
                border = Border.none
                color = Color.white
                cursor = Cursor.pointer
                paddingTop = 0.px
                paddingBottom = 0.px
                paddingLeft = 0.px
                paddingRight = 0.px
                display = Display.flex
                alignItems = Align.center
            }

            rule("#ts-nav-menu") {
                boxSizing = BoxSizing.borderBox
                position = Position.fixed
                top = 0.px
                left = (-100).pct // Hidden by default
                width = 300.px
                height = 100.pct
                backgroundColor = Color("#222")
                transition += Transition("left", 0.3.s)
                zIndex = 1000
                paddingTop = 2.rem
                paddingBottom = 2.rem
                paddingLeft = 2.rem
                paddingRight = 2.rem
                display = Display.flex
                flexDirection = FlexDirection.column
                gap = 1.5.rem
                boxShadow += BoxShadow(Color("rgba(0,0,0,0.5)"), 2.px, 0.px, 5.px)
            }

            rule("#ts-nav-menu.open") {
                left = 0.px
            }

            rule(".ts-menu-close-btn") {
                alignSelf = Align.flexEnd
                fontSize = 1.5.rem
                backgroundColor = Color.transparent
                border = Border.none
                color = Color.white
                cursor = Cursor.pointer
            }

            rule(".ts-menu-item") {
                color = Color.white
                fontSize = 1.2.rem
                fontFamily = "sans-serif"
                paddingTop = 0.5.rem
                paddingBottom = 0.5.rem
                paddingLeft = 0.5.rem
                paddingRight = 0.5.rem
                borderBottom = Border(1.px, BorderStyle.solid, Color("#444"))
                textDecoration = TextDecoration.none
            }
            rule(".ts-menu-item:hover") {
                backgroundColor = Color("#333")
            }
            
            rule(".ts-user-info") {
                fontSize = 0.9.rem
                color = Color("#ccc")
                fontFamily = "sans-serif"
                marginBottom = 1.rem
            }

            media("screen and (max-width: 768px)") {
                rule("#ts-nav-menu") {
                    width = 100.pct
                    left = (-100).pct
                }
                rule("#ts-nav-menu.open") {
                    left = 0.px
                }
            }
        }
    }
}

fun FlowContent.loginStatusHeader(
    session: MySession? = null,
    brandName: String = "My App",
    loginUrl: String = "/login",
    logoutUrl: String = "/logout",
    navContent: DIV.() -> Unit = {}
) {
    // Navigation Menu (Hidden by default)
    div {
        id = "ts-nav-menu"
        button(classes = "ts-menu-close-btn") {
            attributes["onclick"] = "document.getElementById('ts-nav-menu').classList.remove('open')"
            +"✕"
        }
        
        // User Login State
        div {
            if (session?.userId != null) {
                val providerLabel = session.provider?.replaceFirstChar { it.uppercase() } ?: "Unknown"
                val username = session.email ?: session.userName ?: session.userId
                val displayUser = "$username ($providerLabel Login)"
                div("ts-user-info") { +displayUser }
                a(href = logoutUrl, classes = "ts-btn-nav ts-btn-login") { +"Logout" }
            } else {
                a(href = loginUrl, classes = "ts-btn-nav ts-btn-login") { +"Login" }
            }
        }

        // Custom Navigation Content
        div {
            navContent()
        }
    }

    header("ts-header") {
        div("left-section") {
            button(classes = "ts-hamburger-btn") {
                attributes["onclick"] = "document.getElementById('ts-nav-menu').classList.add('open')"
                +"☰"
            }
            div("brand") {
                h3 { +brandName }
            }
        }
    }
}
