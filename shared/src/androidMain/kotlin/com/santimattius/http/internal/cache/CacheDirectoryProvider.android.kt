package com.santimattius.http.internal.cache

import android.content.Context
import com.santimattius.http.startup.getApplicationContext
import okio.Path
import okio.Path.Companion.toPath

actual fun getCacheDirectoryProvider(): CacheDirectoryProvider {
    return AndroidCacheDirectoryProvider(getApplicationContext())
}

private class AndroidCacheDirectoryProvider(
    private val applicationContext: Context
) : CacheDirectoryProvider {
    override val cacheDirectory: Path
        get() {
            return applicationContext.cacheDir.absolutePath.toPath()
        }

}