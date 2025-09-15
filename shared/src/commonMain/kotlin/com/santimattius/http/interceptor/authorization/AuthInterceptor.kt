package com.santimattius.http.interceptor.authorization

import com.santimattius.http.HttpResponse
import com.santimattius.http.interceptor.Interceptor

/**
 * Interceptor that adds authentication tokens to requests.
 *
 * @property tokenProvider A suspending function that provides the current authentication token
 * @property headerName The name of the authentication header (default: "Authorization")
 * @property tokenPrefix The prefix for the token (default: "Bearer ")
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val headerName: String = "Authorization",
    private val tokenPrefix: String = "Bearer "
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val request = chain.request

        // Skip if already has authorization header
        if (request.headers.any { it.key.equals(headerName, ignoreCase = true) }) {
            return chain.proceed(request)
        }

        // Get token
        val token = tokenProvider.getToken() ?: return chain.proceed(request)

        // Add authorization header
        val headers = request.headers.toMutableMap()
        headers[headerName] = "$tokenPrefix$token"

        // Create new request with new headers
        val newRequest = request.copy(headers = headers)

        return chain.proceed(newRequest)
    }
}

