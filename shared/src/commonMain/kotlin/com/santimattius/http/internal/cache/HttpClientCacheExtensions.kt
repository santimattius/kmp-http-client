package com.santimattius.http.internal.cache

import com.santimattius.http.config.CacheConfig
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage

/**
 * Configures the HTTP client with caching support based on the provided configuration.
 *
 * @param config The cache configuration
 * @param cacheDirectoryProvider Optional custom cache directory provider
 */
internal fun HttpClientConfig<*>.configureCache(
    config: CacheConfig,
    cacheDirectoryProvider: CacheDirectoryProvider
) {
    if (!config.enabled) return

    install(HttpCache) {
        privateStorage(
            OkioFileCacheStorage(
                OkioFileCacheConfig(
                    fileName = config.cacheDirectory,
                    maxSize = config.maxCacheSize,
                    ttl = config.cacheTtl,
                    cacheDirectoryProvider = cacheDirectoryProvider
                )
            )
        )
    }
}

/**
 * Disables caching for this client.
 */
internal fun HttpClientConfig<*>.disableCaching() {
    install(HttpCache) {
        privateStorage(CacheStorage.Disabled)
    }
}
