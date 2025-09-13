package com.santimattius.http.util

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.ParseException
import com.santimattius.http.internal.jsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Parses the response body as the specified type using Kotlinx Serialization.
 *
 * @param T The type to parse the response into
 * @return The parsed object
 * @throws ParseException if the response body cannot be parsed
 */
inline fun <reified T> HttpResponse.parseBody(): T {
    return parseBody(serializer())
}

/**
 * Parses the response body as the specified type using the provided serializer.
 *
 * @param T The type to parse the response into
 * @param serializer The serializer to use for parsing
 * @return The parsed object
 * @throws ParseException if the response body cannot be parsed
 */
fun <T> HttpResponse.parseBody(serializer: KSerializer<T>): T {
    val body = body as? String ?: throw ParseException("Response body is null or not a string")
    
    return try {
        jsonConfig.decodeFromString(serializer, body)
    } catch (e: Exception) {
        throw ParseException("Failed to parse response: ${e.message}", e)
    }
}
