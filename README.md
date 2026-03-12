# type_safe_shared

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                        | Description                                                                                             |
|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                                  | Provides a structured routing DSL                                                                       |
| [Authentication](https://start.ktor.io/p/auth)                              | Provides extension point for handling the Authorization header                                          |
| [Authentication Digest](https://start.ktor.io/p/auth-digest)                | Handles 'Digest' authentication scheme                                                                  |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)          | Provides automatic content conversion according to Content-Type and Accept headers                      |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization)      | Handles JSON serialization using kotlinx.serialization library                                          |
| [Sessions](https://start.ktor.io/p/ktor-sessions)                           | Adds support for persistent sessions through cookies or headers                                         |
| [Resources](https://start.ktor.io/p/resources)                              | Provides type-safe routing                                                                              |
| [Static Content](https://start.ktor.io/p/static-content)                    | Serves static files from defined locations                                                              |
| [Status Pages](https://start.ktor.io/p/status-pages)                        | Provides exception handling for routes                                                                  |
| [Webjars](https://start.ktor.io/p/webjars)                                  | Bundles static assets into your built JAR file                                                          |
| [HTML DSL](https://start.ktor.io/p/html-dsl)                                | Generates HTML from Kotlin DSL                                                                          |
| [CSS DSL](https://start.ktor.io/p/css-dsl)                                  | Generates CSS from Kotlin DSL                                                                           |
| [HTMX](https://start.ktor.io/p/htmx)                                        | Includes HTMX for front-end scripting                                                                   |

## Structure

This project includes the following modules:

| Path             | Description                                             |
|------------------|---------------------------------------------------------|
| [server](server) | A runnable Ktor server implementation                   |
| [core](core)     | Domain objects and interfaces                           |
| [client](client) | Extensions for making requests to the server using Ktor |
| [web](web)       | Front-end Kotlin scripts for the browser                |

## Building

To build the project, use one of the following tasks:

| Task                                            | Description                                                          |
|-------------------------------------------------|----------------------------------------------------------------------|
| `./gradlew build`                               | Build everything                                                     |
| `./gradlew :server:buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew :server:buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew :server:publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew -t :web:build`                       | Build WASM scripts continuously                                      |

## Running

To run the project, use one of the following tasks:

| Task                                 | Description                            |
|--------------------------------------|----------------------------------------|
| `./gradlew :server:run`              | Run the server                         |
| `./gradlew :server:runDocker`        | Run using the local docker image       |
| `./gradlew -t :web:wasmJsBrowserRun` | Run scripts in a browser, without Ktor |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Mock Auth Mode

For development and testing without real OAuth providers, you can enable **Mock Auth Mode**.

### 1. Enable Mock Mode
Set the following environment variable before running the server:
```bash
export MOCK_AUTH=true
```

### 2. Login as a Mock User
- Visit `http://localhost:8080/login/mock` in your browser.
- Select one of the 31 predefined mock users (e.g., Amina El-Amin, CEO).
- You will be "logged in" with a mock session and redirected to the home page.

### 3. Mock Features
- **User Search**: `/auth/google/users?q=...` searches the internal `MockUserStore`.
- **Hierarchy**: `/auth/google/hierarchy?email=...` builds the organizational tree using the mock data.
- **Reference Files**:
    - `users-mock.csv`: CSV formatted for Google Workspace import.
    - `org-chart-mock.txt`: ASCII representation of the mock hierarchy.

## Enterprise Identity Support

This library is built for large-scale enterprise deployment, providing native support for the "Big Three" Identity Providers (IdP) that power over 80% of the modern business market.

| Provider | Market Segment | Capabilities |
| :--- | :--- | :--- |
| **Microsoft Entra ID** | Global 2000 / Fortune 500 | Full functional hierarchy, Group discovery via Graph API. |
| **Google Workspace** | Startups / Mid-Market / Tech | Organizational search, Manager relations via Admin SDK. |
| **Okta** | Best-of-Breed / Multi-Cloud | User profiles, Group memberships, Dashboard "Tile" support. |
| **Mock Provider** | Local Development | 31-user functional organizational slice for rapid prototyping. |

### Key Enterprise Features:
- **Type-Safe Models**: Standardized `DirectoryUser` model regardless of the underlying IdP.
- **Domain-Isolated Caching**: Automatic, performant in-memory caching with organization-level isolation.
- **Unified Error Handling**: Consistent JSON error reporting for all directory operations.
- **Modular Design**: Swappable provider architecture via `DirectoryProviderFactory`.

This project is licensed under the MIT License - see the LICENSE file for details.

