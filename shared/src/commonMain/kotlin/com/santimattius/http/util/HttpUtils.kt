package com.santimattius.http.util

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.ParseException
import com.santimattius.http.internal.jsonConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Utility functions for working with HTTP responses.
 *
 * This package provides extension functions for parsing and transforming
 * HTTP responses in a type-safe manner using Kotlinx Serialization.
 */

/**
 * Parses the HTTP response body as the specified type using Kotlinx Serialization.
 *
 * This is a convenience extension function that infers the serializer from the reified type parameter.
 *
 * ## Example Usage
 * ```kotlin
 * // Parse response as a User object
 * val user: User = httpResponse.parseBody()
 *
 * // Parse response as a list of Users
 * val users: List<User> = httpResponse.parseBody()
 * ```
 *
 * @param T The type to parse the response into (must be serializable)
 * @return The parsed object of type [T]
 * @throws ParseException if the response body is null, not a string, or cannot be deserialized
 *                       into the specified type
 *
 * @see parseBody For the version that accepts an explicit serializer
 * @see KSerializer For more information about Kotlinx Serialization
 */
inline fun <reified T> HttpResponse.parseBody(): T {
    return parseBody(serializer())
}

/**
 * Parses the HTTP response body as the specified type using the provided serializer.
 *
 * This function is useful when you need to provide a custom serializer or when
 * working with generic types that can't be reified.
 *
 * ## Example Usage
 * ```kotlin
 * // With a custom serializer
 * val user = httpResponse.parseBody(User.serializer())
 *
 * // With a generic type
 * fun <T> parseResponse(response: HttpResponse, serializer: KSerializer<T>): T {
 *     return response.parseBody(serializer)
 * }
 * ```
 *
 * @param T The type to parse the response into
 * @param serializer The [KSerializer] to use for deserialization
 * @return The parsed object of type [T]
 * @throws ParseException if the response body is null, not a string, or cannot be deserialized
 *                       using the provided serializer
 *
 * @see parseBody For the version that infers the serializer from the type parameter
 * @see KSerializer For more information about Kotlinx Serialization
 */
fun <T> HttpResponse.parseBody(serializer: KSerializer<T>): T {
    val body = body ?: throw ParseException(
        "Response body is null or not a string. Status: $status, URL: $url"
    )
    
    return try {
        jsonConfig.decodeFromString(serializer, body)
    } catch (e: Exception) {
        throw ParseException(
            "Failed to parse response. Status: $status, URL: $url, Error: ${e.message}",
            e
        )
    }
}
