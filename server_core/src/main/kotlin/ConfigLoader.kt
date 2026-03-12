package com.quadrigasoftware

import io.ktor.server.application.*
import io.ktor.server.config.*

/**
 * Loads the type-safe SecurityConfig from Ktor's environment configuration.
 */
fun Application.loadSecurityConfig(): SecurityConfig {
    val authConfig = environment.config.config("auth")
    
    val sessionSecret = authConfig.propertyOrNull("session.secret")?.getString() 
        ?: "00112233445566778899aabbccddeeff"

    val allowListConfig = try { authConfig.config("allowList") } catch (e: Exception) { null }
    val allowedEmails = allowListConfig?.propertyOrNull("emails")?.getList()?.toSet() ?: emptySet()
    val allowedDomains = allowListConfig?.propertyOrNull("domains")?.getList()?.toSet() ?: emptySet()

    val providersConfig = try { authConfig.config("providers") } catch (e: Exception) { null }
    val providerNames = providersConfig?.keys()?.map { it.split('.').first() }?.distinct() ?: emptyList()

    val providers = providerNames.associateWith { name ->
        val config = providersConfig!!.config(name)
        
        val extraParamsConfig = try { config.config("extraAuthParameters") } catch (e: Exception) { null }
        val extraParams = extraParamsConfig?.keys()?.map { key ->
            key to extraParamsConfig.property(key).getString()
        } ?: emptyList()

        AuthProviderConfig(
            name = name,
            clientId = config.propertyOrNull("clientId")?.getString(),
            clientSecret = config.propertyOrNull("clientSecret")?.getString(),
            authorizeUrl = config.propertyOrNull("authorizeUrl")?.getString(),
            accessTokenUrl = config.propertyOrNull("accessTokenUrl")?.getString(),
            scopes = config.propertyOrNull("scopes")?.getList() ?: emptyList(),
            extraAuthParameters = extraParams
        )
    }

    val isMockActive = System.getenv("MOCK_AUTH") == "true" || (providers["mock"]?.name != null)

    val featuresConfig = try { authConfig.config("features") } catch (e: Exception) { null }
    val features = AppFeatures(
        leaderboard = featuresConfig?.propertyOrNull("leaderboard")?.getString()?.toBoolean() ?: true,
        orgChart = featuresConfig?.propertyOrNull("orgChart")?.getString()?.toBoolean() ?: true,
        search = featuresConfig?.propertyOrNull("search")?.getString()?.toBoolean() ?: true,
        directoryCache = featuresConfig?.propertyOrNull("directoryCache")?.getString()?.toBoolean() ?: true
    )

    return SecurityConfig(
        sessionSecret = sessionSecret,
        providers = providers,
        allowedEmails = allowedEmails,
        allowedDomains = allowedDomains,
        features = features,
        isMockEnabled = isMockActive
    )
}
