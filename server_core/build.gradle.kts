val kotlin_version: String by project
val kotlinx_browser_version: String by project
val kotlinx_html_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

// Disable Ktor's fatJar/shadowJar since this is a library
tasks.named("shadowJar") {
    enabled = false
}

// Configure Dokka to generate Javadoc JAR
val dokkaJavadocJar by tasks.registering(Jar::class) {
    from(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
}

dependencies {
    // Shared dependencies that consumers also need (api)
    api("io.ktor:ktor-server-core")
    api("io.ktor:ktor-server-auth")
    api("io.ktor:ktor-client-core")
    api("io.ktor:ktor-serialization-kotlinx-json")
    
    // Internal dependencies (implementation)
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("io.ktor:ktor-server-sessions")
    implementation("com.google.cloud:google-cloud-firestore:3.30.0")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-webjars")
    implementation("org.webjars:jquery:3.2.1")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinx_html_version")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css-jvm:2025.6.4")
    implementation("io.ktor:ktor-server-htmx")
    implementation("io.ktor:ktor-htmx-html")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            
            groupId = "com.quadrigasoftware"
            artifactId = "server-core"
            version = project.version.toString()

            pom {
                name.set("Server Core Library")
                description.set("Shared authentication and directory services for Quadriga Software.")
                url.set("https://github.com/quadrigasoftware/type_safe_shared")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("quadrigasoftware")
                        name.set("Quadriga Software")
                    }
                }
            }
        }
    }
}
