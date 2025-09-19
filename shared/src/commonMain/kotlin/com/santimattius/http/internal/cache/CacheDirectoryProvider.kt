package com.santimattius.http.internal.cache

import okio.Path

interface CacheDirectoryProvider {

    /**
     * Provides the cache directory path for the current platform
     */
    val cacheDirectory: Path
}

expect fun getCacheDirectoryProvider(): CacheDirectoryProvider