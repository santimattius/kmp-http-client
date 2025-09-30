package com.santimattius.http

import android.app.Application
import com.santimattius.http.configuration.CacheConfig
import com.santimattius.http.configuration.HttpClientConfig

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = createConfiguration()

        //initialize the http client
        HttpClient.initialize(config)
    }

    private fun createConfiguration(): HttpClientConfig {
        return HttpClientConfig("https://api-picture.onrender.com/")
            .cache(
                cacheConfig = CacheConfig(
                    enabled = true,
                    cacheDirectory = "http_cache",
                    maxCacheSize = 10L * 1024 * 1024, // 10 MB
                    cacheTtl = 60 * 60 * 1000 // 1 hour
                )
            )
    }
}