package com.santimattius.http.interceptor.authorization

import com.santimattius.http.HttpResponse
import com.santimattius.http.interceptor.Interceptor
import kotlin.jvm.JvmOverloads

/**
 * Interceptor that adds authentication tokens to outgoing HTTP requests.
 *
 * This interceptor should be added to the client's interceptor chain to automatically
 * include authentication tokens in requests. It's designed to be flexible enough
 * to work with various authentication schemes by allowing customization of the
 * header name and token prefix.
 *
 * @constructor Creates a new instance of the authentication interceptor.
 * @param tokenProvider The provider that supplies the authentication token
 * @param headerName The name of the HTTP header to use for authentication (default: "Authorization")
 * @param tokenPrefix The prefix to prepend to the token (default: "Bearer ")
 */
class AuthInterceptor @JvmOverloads constructor(
    private val tokenProvider: TokenProvider,
    private val headerName: String = "Authorization",
    private val tokenPrefix: String = "Bearer "
) : Interceptor {

    /**
     * Intercepts an outgoing HTTP request to add an authentication token.
     *
     * This method:
     * 1. Checks if the request already has an authorization header
     * 2. If not, fetches a token from the token provider
     * 3. Adds the token to the request headers with the configured prefix
     * 4. Proceeds with the modified request
     *
     * @param chain The interceptor chain
     * @return The HTTP response from the server
     *
     * @sample com.santimattius.http.interceptor.samples.authInterceptorUsageSample
     */
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val request = chain.request

        // Skip if the request already has an authorization header
        if (request.headers.any { it.key.equals(headerName, ignoreCase = true) }) {
            return chain.proceed(request)
        }

        // Get the current authentication token
        val token = tokenProvider.getToken() ?: return chain.proceed(request)

        // Create a new headers map with the authorization header added
        val headers = request.headers.toMutableMap()
        headers[headerName] = "$tokenPrefix$token"

        // Create a new request with the updated headers
        val newRequest = request.copy(headers = headers)

        return chain.proceed(newRequest)
    }
}

