package com.santimattius.http.internal.cache

import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

fun CachedResponseData.makeCopy(): CachedResponseDataCopy {
    val httpStatusCodeCopy = HttpStatusCodeCopy(
        value = statusCode.value,
        description = statusCode.description
    )
    val httpProtocolVersionCopy = HttpProtocolVersionCopy(
        name = version.name,
        major = version.major,
        minor = version.minor
    )
    return CachedResponseDataCopy(
        url = url,
        statusCode = httpStatusCodeCopy,
        requestTime = requestTime,
        responseTime = responseTime,
        headers = headers.entries().associate { it.key to it.value },
        varyKeys = varyKeys,
        body = body,
        version = httpProtocolVersionCopy,
        expires = expires
    )
}

fun CachedResponseDataCopy.restore(): CachedResponseData {
    val httpStatusCode = HttpStatusCode(
        value = statusCode.value,
        description = statusCode.description
    )
    val httpProtocolVersion = HttpProtocolVersion(
        name = version.name,
        major = version.major,
        minor = version.minor
    )
    return CachedResponseData(
        url = url,
        statusCode = httpStatusCode,
        requestTime = requestTime,
        responseTime = responseTime,
        headers = headersOf(*headers.map { it.key to it.value }.toTypedArray()),
        varyKeys = varyKeys,
        body = body,
        version = httpProtocolVersion,
        expires = expires
    )
}