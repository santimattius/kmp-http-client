package com.santimattius.http.interceptor

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.UnauthorizedException

/**
 * Interceptor that adds authentication tokens to requests.
 *
 * @property tokenProvider A suspending function that provides the current authentication token
 * @property headerName The name of the authentication header (default: "Authorization")
 * @property tokenPrefix The prefix for the token (default: "Bearer ")
 */
class AuthInterceptor(
    private val tokenProvider: suspend () -> String?,
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
        val token = tokenProvider() ?: return chain.proceed(request)

        // Add authorization header
        val headers = request.headers.toMutableMap()
        headers[headerName] = "$tokenPrefix$token"

        // Create new request with new headers
        val newRequest = request.copy(headers = headers)

        return chain.proceed(newRequest)
    }
}

/**
 * Interceptor that handles 401 Unauthorized responses by refreshing the token and retrying.
 *
 * @property refreshToken A suspending function that refreshes the token
 * @property onUnauthorized Callback when a 401 response is received (after refresh attempt)
 */
class TokenRefreshInterceptor(
    private val refreshToken: suspend () -> Boolean,
    private val onUnauthorized: suspend () -> Unit = {}
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val response = chain.proceed(chain.request)

        // Check for 401 Unauthorized
        if (response.status == 401) {
            // Try to refresh token
            val tokenRefreshed = refreshToken()

            if (tokenRefreshed) {
                // Retry the original request with new token
                return chain.proceed(chain.request)
            } else {
                // Notify about unauthorized state
                onUnauthorized()
                throw UnauthorizedException("Authentication required")
            }
        }

        return response
    }
}

/**
 * Creates an authentication interceptor with the given token provider.
 */
fun authInterceptor(
    headerName: String = "Authorization",
    tokenPrefix: String = "Bearer ",
    tokenProvider: suspend () -> String?
): Interceptor = AuthInterceptor(tokenProvider, headerName, tokenPrefix)

/**
 * Creates a token refresh interceptor.
 */
fun tokenRefreshInterceptor(
    refreshToken: suspend () -> Boolean,
    onUnauthorized: suspend () -> Unit = {}
): Interceptor = TokenRefreshInterceptor(refreshToken, onUnauthorized)
