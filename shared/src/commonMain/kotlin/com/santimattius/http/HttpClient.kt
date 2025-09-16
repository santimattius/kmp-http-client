package com.santimattius.http

import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.interceptor.Interceptor
import com.santimattius.http.internal.KtorClient

/**
 * Main entry point for creating and managing HTTP clients.
 *
 * This object provides factory methods to create and configure HTTP client instances.
 * It supports both a default singleton client and the creation of custom client instances.
 *
 * ## Basic Usage
 *
 * ### Initializing the default client (recommended for most use cases):
 * ```kotlin
 * val config = HttpClientConfig(
 *     baseUrl = "https://api.example.com",
 *     connectTimeout = 30_000,
 *     socketTimeout = 30_000,
 *     enableLogging = true
 * )
 * HttpClient.initialize(config)
 *
 * // Later in your code:
 * val client = HttpClient.defaultClient()
 * ```
 *
 * ### Creating a custom client:
 * ```kotlin
 * val customConfig = HttpClientConfig(
 *     baseUrl = "https://custom.api.example.com",
 *     connectTimeout = 60_000
 * )
 * val customClient = HttpClient.create(customConfig)
 * ```
 *
 * @see Client For executing HTTP requests
 * @see HttpClientConfig For client configuration options
 * @see Interceptor For request/response interception
 */
object HttpClient {

    /**
     * The default configuration used when creating new client instances.
     * This is set when [initialize] is called.
     */
    private var defaultConfig: HttpClientConfig? = null

    /**
     * The default client instance created by [initialize].
     */
    private var defaultClient: Client? = null

    /**
     * Global interceptors that will be added to all client instances.
     */
    private var interceptors: List<Interceptor> = emptyList()

    /**
     * Initializes the default HTTP client with the given configuration.
     * This should be called once during app startup, typically in your application's
     * initialization code.
     *
     * @param config The configuration to use for the default client
     * @param interceptors Optional list of interceptors to apply to all client instances
     * @throws IllegalStateException if called more than once
     *
     * @sample com.santimattius.http.samples.initializeSample
     */
    fun initialize(
        config: HttpClientConfig,
        interceptors: List<Interceptor> = emptyList()
    ) {
        require(defaultConfig == null) { "Default config already initialized. Call this method only once during app startup." }
        this.defaultConfig = config.copy()
        this.interceptors = interceptors
        this.defaultClient = create(config, interceptors)
    }

    /**
     * Gets the default HTTP client instance.
     *
     * @return The default [Client] instance
     * @throws IllegalStateException if [initialize] hasn't been called
     *
     * @sample com.santimattius.http.samples.defaultClientSample
     */
    fun defaultClient(): Client {
        return defaultClient ?: throw IllegalStateException(
            """
            Default client not initialized. Call HttpClient.initialize() first.
            Example:
            
            HttpClient.initialize(
                HttpClientConfig(
                    baseUrl = "https://api.example.com",
                    connectTimeout = 30_000,
                    socketTimeout = 30_000
                )
            )
            """.trimIndent()
        )
    }

    /**
     * Creates a new HTTP client with the given configuration.
     *
     * This method creates a new client instance independent of the default client.
     * Any global interceptors registered via [initialize] will be included.
     *
     * @param config The configuration for the new client
     * @return A new [Client] instance
     *
     * @sample com.santimattius.http.samples.createClientSample
     */
    fun create(config: HttpClientConfig): Client {
        return create(config, interceptors)
    }

    /**
     * Internal factory method that creates a new HTTP client with the given configuration
     * and interceptors.
     *
     * This method merges the provided configuration with any default configuration
     * that was set via [initialize].
     *
     * @param config The base configuration for the client
     * @param interceptors The interceptors to include in the client
     * @return A new [Client] instance
     */
    private fun create(
        config: HttpClientConfig,
        interceptors: List<Interceptor>
    ): Client {
        // Merge with default config if available, using the provided config as fallback
        return KtorClient.create(
            config.copy(
                baseUrl = defaultConfig?.baseUrl ?: config.baseUrl,
                connectTimeout = defaultConfig?.connectTimeout ?: config.connectTimeout,
                socketTimeout = defaultConfig?.socketTimeout ?: config.socketTimeout,
                enableLogging = defaultConfig?.enableLogging ?: config.enableLogging,
                logLevel = defaultConfig?.logLevel ?: config.logLevel
            ),
            interceptors
        )
    }
}