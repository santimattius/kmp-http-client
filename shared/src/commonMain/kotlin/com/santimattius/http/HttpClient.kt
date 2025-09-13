package com.santimattius.http

import com.santimattius.http.HttpClient.initialize
import com.santimattius.http.config.HttpClientConfig
import com.santimattius.http.interceptor.Interceptor
import com.santimattius.http.internal.KtorClient

/**
 * Main entry point for creating HTTP clients.
 */
object HttpClient {

    private var defaultConfig: HttpClientConfig? = null
    private var defaultClient: Client? = null
    private var interceptors: List<Interceptor> = emptyList()

    /**
     * Initializes the default HTTP client with the given configuration.
     * This should be called once during app startup.
     *
     * @param config Configuration for the default HTTP client
     */
    fun initialize(
        config: HttpClientConfig,
        interceptors: List<Interceptor> = emptyList()
    ) {
        require(defaultConfig == null) { "Default config already initialized" }
        this.defaultConfig = config.copy()
        this.interceptors = interceptors
        this.defaultClient = create(config, interceptors)
    }

    /**
     * Gets the default HTTP client instance.
     * @throws IllegalStateException if [initialize] hasn't been called
     */
    fun defaultClient(): Client {
        return defaultClient ?: throw IllegalStateException(
            "Default client not initialized. Call HttpClient.initialize() first."
        )
    }

    fun create(config: HttpClientConfig): Client {
        return create(config, interceptors)
    }

    /**
     * Creates a new HTTP client with the given configuration.
     *
     * @param config Configuration for the HTTP client
     * @param interceptors Optional list of interceptors to add to this client
     * @return A new [Client] instance
     */
    private fun create(
        config: HttpClientConfig,
        interceptors: List<Interceptor>
    ): Client {
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