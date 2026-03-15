# System Architecture

This document describes the architectural patterns and component relationships used in the core directory and authentication system.

## Design Patterns

1.  **Strategy Pattern (Provider)**: The `DirectoryProvider` interface allows the system to switch between different data sources (Google Workspace vs. Mock data) without changing the business logic.
2.  **Decorator Pattern**: `CachingDirectoryProvider` wraps a base provider to add in-memory caching logic transparently.
3.  **Factory Pattern**: `DirectoryProviderFactory` encapsulates the logic for selecting, initializing, and caching the correct provider implementation based on the user session.
4.  **Value Object Pattern**: `DirectoryUser` provides a stable, type-safe representation of a user, decoupling the application from external API schemas (like Google's JSON).

## Class Diagram

```mermaid
classDiagram
    class DirectoryProvider {
        <<interface>>
        +searchUsers(query, fields) List~DirectoryUser~
        +getUser(email) DirectoryUser?
    }

    class GoogleDirectoryProvider {
        -httpClient: HttpClient
        -accessToken: String
        +searchUsers(query, fields)
        +getUser(email)
        -fetchAllUsers()
        -mapToDirectoryUser(json)
    }

    class MockDirectoryProvider {
        +searchUsers(query, fields)
        +getUser(email)
        -mapToDirectoryUser(json)
    }

    class CachingDirectoryProvider {
        -delegate: DirectoryProvider
        -cacheKey: String
        -ttl: Long
        +searchUsers(query, fields)
        +getUser(email)
        +clearCache(key)
    }

    class DirectoryProviderFactory {
        -httpClient: HttpClient
        -securityConfig: SecurityConfig
        +getProvider(session) DirectoryProvider?
    }

    class DirectoryUser {
        <<Serializable>>
        +email: String
        +fullName: String
        +title: String?
        +managerEmail: String?
        +reports: List~String~
    }

    DirectoryProvider <|.. GoogleDirectoryProvider : implements
    DirectoryProvider <|.. MockDirectoryProvider : implements
    DirectoryProvider <|.. CachingDirectoryProvider : implements (Decorator)
    CachingDirectoryProvider o-- DirectoryProvider : delegates to
    
    DirectoryProviderFactory ..> DirectoryProvider : creates
    DirectoryProviderFactory ..> DirectoryUser : returns via provider
```

## Data Flow

```mermaid
graph TD
    A[Ktor Route: /auth/google/users] --> B[SecurityCore.kt]
    B --> C{DirectoryProviderFactory}
    
    subgraph "Core Logic (server_core)"
    C -->|If Mock| D[MockDirectoryProvider]
    C -->|If Google| E[GoogleDirectoryProvider]
    
    D --> F[CachingDirectoryProvider]
    E --> F
    
    F -->|Cache Hit| G[Return Cached DirectoryUser List]
    F -->|Cache Miss| H[Fetch fresh data from Google/MockStore]
    H --> I[Map JSON to DirectoryUser]
    I --> G
    end
    
    subgraph "Data Sources"
    H -.-> J[Google Admin SDK API]
    H -.-> K[MockUserStore.kt]
    end

    G --> L[Ktor Response: JSON]
```

## Core Components

### SecurityConfig
A type-safe configuration object loaded at startup. It validates all authentication settings and allow-lists, ensuring the application fails fast if configuration is missing.

### MySession
The primary session object stored in a signed cookie. It contains the user identity, roles, and the OAuth access token required for directory operations.

### StatusPages (Error Handling)
Centralized error handling that maps domain exceptions (e.g., `UserNotFoundException`) to appropriate HTTP status codes and JSON error responses.
