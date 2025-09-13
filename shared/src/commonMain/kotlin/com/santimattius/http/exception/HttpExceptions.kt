package com.santimattius.http.exception

/**
 * Base class for all HTTP-related exceptions.
 */
open class HttpException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when the request could not be executed due to a network error.
 */
class NetworkException(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Network error occurred", cause)

/**
 * Thrown when the server responds with an HTTP error code.
 * @property code The HTTP status code
 * @property errorBody The response body, if any
 */
open class HttpErrorException(
    val code: Int,
    val errorBody: String? = null,
    message: String? = null
) : HttpException(message ?: "HTTP $code Error")

/**
 * Thrown when the server responds with a 400 Bad Request error.
 */
class BadRequestException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(400, errorBody, message ?: "Bad Request")

/**
 * Thrown when the request requires user authentication (401 Unauthorized).
 */
class UnauthorizedException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(401, errorBody, message ?: "Unauthorized")

/**
 * Thrown when the server understood the request but refuses to authorize it (403 Forbidden).
 */
class ForbiddenException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(403, errorBody, message ?: "Forbidden")

/**
 * Thrown when the requested resource could not be found (404 Not Found).
 */
class NotFoundException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(404, errorBody, message ?: "Not Found")

/**
 * Thrown when the server encounters an internal error (500 Internal Server Error).
 */
class InternalServerErrorException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(500, errorBody, message ?: "Internal Server Error")

/**
 * Thrown when the server is temporarily unavailable (503 Service Unavailable).
 */
class ServiceUnavailableException(
    errorBody: String? = null,
    message: String? = null
) : HttpErrorException(503, errorBody, message ?: "Service Unavailable")

/**
 * Thrown when the request times out.
 */
class TimeoutException(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Request timed out", cause)

/**
 * Thrown when there's an error parsing the response.
 */
class ParseException(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Error parsing response", cause)

/**
 * Thrown when there's an unknown error.
 */
class UnknownHttpException(
    message: String? = null,
    cause: Throwable? = null
) : HttpException(message ?: "Unknown error occurred", cause)

/**
 * Converts an HTTP status code to its corresponding exception.
 */
fun statusCodeToException(
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
