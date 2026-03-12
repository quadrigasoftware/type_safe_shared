# Project Mandates & Notes

## Security & Permissions
- **Status**: `POST /auth/directory/clear-cache` is currently accessible to any logged-in user.
- **Mandate**: Before going to production, this route MUST be restricted to users with an `ADMIN` role or a specific allow-list of emails to prevent potential Denial of Service (DoS) by repeatedly triggering expensive directory fetches.
- **Mandate (Session Continuity)**: In production, a stable `SESSION_SECRET` must be provided via environment variables. If this secret is changed or left to the default fallback, all active user sessions will be invalidated upon server restart or Cloud Run redeployment.

## Caching Strategy (Scalability)
- **Status**: Current implementation uses a simple in-memory `ConcurrentHashMap` with a 5-minute TTL in `CachingDirectoryProvider`.
- **Note**: When scaling beyond 5,000 users or increasing multi-tenancy (many domains), the caching strategy MUST be improved to prevent excessive memory usage.
- **Recommendations**:
    - Implement field filtering to store only essential user data (SimplifiedUser).
    - Switch to a size-bounded cache like Caffeine to enforce memory limits.
    - Consider an external cache like Redis for stateless scaling and persistence.
