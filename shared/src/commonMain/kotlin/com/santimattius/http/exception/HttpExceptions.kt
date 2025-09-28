package com.santimattius.http.exception

import kotlin.jvm.JvmOverloads

/**
 * Package containing all HTTP-related exceptions used throughout the HTTP client.
 *
 * This package defines a hierarchy of exceptions for handling various HTTP error
 * conditions in a type-safe manner. All exceptions extend from [HttpException].
 */

/**
 * Base class for all HTTP-related exceptions in the client.
 *
 * This serves as the root exception for all HTTP-specific error conditions.
 * It extends the standard [Exception] class and provides additional context
 * through its message and cause.
 *
 * @property message The detail message (which is saved for later retrieval by the [message] property)
 * @property cause The cause (which is saved for later retrieval by the [cause] property)
 *
 * @see NetworkException For network-related errors
 * @see HttpErrorException For HTTP error responses (4xx, 5xx)
 * @see TimeoutException For request timeouts
 * @see ParseException For response parsing errors
 */
open class HttpException(
    message: String? = null,
    cause: Throwable? = null
) : Throwable(message, cause)

/**
 * Thrown when the request could not be executed due to a network error.
 *
 * This typically indicates that the client was unable to reach the server
 * or the connection was lost during the request.
 *
 * ## Common Causes
 * - No internet connection
 * - DNS resolution failure
 * - Connection refused
 * - Network timeout
 *
 * @param message The detail message (which is saved for later retrieval by the [message] property)
 * @param cause The cause (which is saved for later retrieval by the [cause] property)
 */
class NetworkException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Network error occurred", cause)

/**
 * Base class for all HTTP error responses (status codes 4xx and 5xx).
 *
 * This exception is thrown when the server responds with an error status code.
 * It provides access to the status code and the response body for error handling.
 *
 * @property code The HTTP status code (e.g., 404)
 * @property errorBody The raw response body, if any (may contain error details)
 * @param message The detail message (defaults to a generic message with the status code)
 *
 * @see BadRequestException For 400 errors
 * @see UnauthorizedException For 401 errors
 * @see ForbiddenException For 403 errors
 * @see NotFoundException For 404 errors
 * @see InternalServerErrorException For 500 errors
 * @see ServiceUnavailableException For 503 errors
 */
open class HttpErrorException @JvmOverloads constructor(
    val code: Int,
    val errorBody: String? = null,
    message: String? = null
) : HttpException(message ?: "HTTP $code Error")

/**
 * Thrown when the server cannot process the request due to a client error (400 Bad Request).
 *
 * This typically indicates that the request was malformed or contained invalid parameters.
 *
 * ## Common Causes
 * - Invalid request syntax
 * - Invalid request message framing
 * - Deceptive request routing
 * - Size too large
 *
 * @param errorBody The response body, which may contain details about the error
 * @param message A custom error message (defaults to "Bad Request")
 */
class BadRequestException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(400, errorBody, message ?: "Bad Request")

/**
 * Thrown when authentication is required and has failed or has not yet been provided (401 Unauthorized).
 *
 * This indicates that the request lacks valid authentication credentials.
 *
 * ## Common Causes
 * - No authentication credentials provided
 * - Invalid or expired authentication token
 * - Invalid API key
 *
 * @param errorBody The response body, which may contain details about the authentication failure
 * @param message A custom error message (defaults to "Unauthorized")
 */
class UnauthorizedException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(401, errorBody, message ?: "Unauthorized")

/**
 * Thrown when the server understood the request but refuses to authorize it (403 Forbidden).
 *
 * Unlike [UnauthorizedException], the server knows who is making the request
 * but their account doesn't have permission to access the resource.
 *
 * ## Common Causes
 * - Insufficient permissions
 * - Account disabled
 * - IP-based restrictions
 *
 * @param errorBody The response body, which may contain details about the authorization failure
 * @param message A custom error message (defaults to "Forbidden")
 */
class ForbiddenException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(403, errorBody, message ?: "Forbidden")

/**
 * Thrown when the requested resource could not be found (404 Not Found).
 *
 * This indicates that the server can't find the requested resource.
 *
 * ## Common Causes
 * - Invalid URL
 * - Resource was deleted
 * - Resource was moved without a forwarding address
 *
 * @param errorBody The response body, which may contain details about the missing resource
 * @param message A custom error message (defaults to "Not Found")
 */
class NotFoundException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(404, errorBody, message ?: "Not Found")

/**
 * Thrown when the server encounters an unexpected condition (500 Internal Server Error).
 *
 * This is a generic server-side error that indicates the server encountered
 * an unexpected condition that prevented it from fulfilling the request.
 *
 * ## Common Causes
 * - Server misconfiguration
 * - Runtime exception on the server
 * - Database connection issues
 *
 * @param errorBody The response body, which may contain details about the server error
 * @param message A custom error message (defaults to "Internal Server Error")
 */
class InternalServerErrorException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(500, errorBody, message ?: "Internal Server Error")

/**
 * Thrown when the server is currently unable to handle the request due to temporary overloading
 * or maintenance (503 Service Unavailable).
 *
 * This is a temporary condition which will be alleviated after some delay.
 *
 * ## Common Causes
 * - Server under maintenance
 * - Server overloaded
 * - Temporary service disruption
 *
 * @param errorBody The response body, which may contain a Retry-After header or similar
 * @param message A custom error message (defaults to "Service Unavailable")
 */
class ServiceUnavailableException @JvmOverloads constructor(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(503, errorBody, message ?: "Service Unavailable")

/**
 * Thrown when a request times out before a response is received.
 *
 * This can occur at different levels:
 * - Connection timeout: When the client cannot establish a connection
 * - Read timeout: When the server takes too long to respond
 * - Write timeout: When the client takes too long to send the request
 *
 * @param message A custom error message (defaults to "Request timed out")
 * @param cause The underlying cause of the timeout, if any
 */
class TimeoutException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Request timed out", cause)

/**
 * Thrown when there's an error parsing the response from the server.
 *
 * This typically occurs when the response body doesn't match the expected format.
 *
 * ## Common Causes
 * - Invalid JSON/XML in the response
 * - Mismatch between expected and actual response format
 * - Malformed response headers
 *
 * @param message A custom error message (defaults to "Error parsing response")
 * @param cause The underlying exception that caused the parse error
 */
class ParseException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Error parsing response", cause)

/**
 * A catch-all exception for any HTTP-related error that doesn't fit into other categories.
 *
 * This should be used sparingly - prefer more specific exceptions when possible.
 *
 * @param message A custom error message (defaults to "Unknown error occurred")
 * @param cause The underlying cause of the error, if known
 */
class UnknownHttpException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Unknown error occurred", cause)

/**
 * Converts an HTTP status code to its corresponding exception.
 *
 * This utility function maps standard HTTP status codes to their corresponding
 * exception types. It's typically used internally by the HTTP client to
 * throw appropriate exceptions based on the response status code.
 *
 * ## Example Usage
 * ```kotlin
 * try {
 *     val response = client.execute(request)
 *     if (!response.isSuccessful) {
 *         throw statusCodeToException(response.status, response.body?.toString())
 *     }
 *     // Process successful response
 * } catch (e: HttpErrorException) {
 *     // Handle specific HTTP errors
 * }
 * ```
 *
 * @param code The HTTP status code (e.g., 404)
 * @param errorBody The response body, which may contain error details
 * @param message A custom error message (optional)
 * @return An appropriate [HttpErrorException] subclass based on the status code
 *
 * @see HttpErrorException For the base exception type
 */
internal fun statusCodeToException(
    code: Int,
    errorBody: String? = null,
    message: String? = null
): HttpErrorException {
    return when (code) {
        400 -> BadRequestException(errorBody, message)
        401 -> UnauthorizedException(errorBody, message)
        403 -> ForbiddenException(errorBody, message)
        404 -> NotFoundException(errorBody, message)
        500 -> InternalServerErrorException(errorBody, message)
        503 -> ServiceUnavailableException(errorBody, message)
        else -> HttpErrorException(code, errorBody, message)
    }
}
