package com.santimattius.http.interceptor.authorization

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.UnauthorizedException
import com.santimattius.http.interceptor.Interceptor
import kotlin.jvm.JvmOverloads

/**
 * Interceptor that automatically handles 401 Unauthorized responses by attempting to refresh the authentication token.
 *
 * This interceptor is designed to work with token-based authentication systems where tokens can expire.
 * When a 401 response is received, it will:
 * 1. Attempt to refresh the authentication token
 * 2. If successful, retry the original request with the new token
 * 3. If unsuccessful, notify the application via the [onUnauthorized] callback
 *
 * ## Features
 * - Automatic token refresh on 401 responses
 * - Single retry attempt for failed requests
 * - Configurable unauthorized callback
 * - Thread-safe operation
 *
 * ## Example Usage
 * ```kotlin
 * val refreshToken = object : RefreshToken {
 *     private var currentToken: String? = "initial-token"
 *     private var refreshAttempted = false
 *
 *     override suspend fun isRefreshed(): Boolean {
 *         if (refreshAttempted) return false
 *         refreshAttempted = true
 *         // Implement your token refresh logic here
 *         currentToken = "new-token"
 *         return true
 *     }
 * }
 *
 * val interceptor = TokenRefreshInterceptor(
 *     refreshToken = refreshToken,
 *     onUnauthorized = {
 *         // Handle unauthorized state (e.g., navigate to login)
 *     }
 * )
 * ```
 *
 * @property refreshToken Handles the token refresh logic
 * @property onUnauthorized Callback invoked when authentication is required after a failed refresh attempt
 *
 * @see RefreshToken For implementing token refresh logic
 * @see AuthInterceptor For adding authentication tokens to requests
 */
class TokenRefreshInterceptor @JvmOverloads constructor(
    private val refreshToken: RefreshToken,
    private val onUnauthorized: UnauthorizedCallback
) : Interceptor {

    /**
     * Intercepts the HTTP request/response chain to handle 401 Unauthorized responses.
     *
     * This method:
     * 1. Proceeds with the original request
     * 2. If a 401 response is received, attempts to refresh the token
     * 3. If refresh is successful, retries the original request
     * 4. If refresh fails, invokes the unauthorized callback and throws an exception
     *
     * @param chain The interceptor chain
     * @return The HTTP response from the server or from the retried request
     * @throws UnauthorizedException If authentication is required and token refresh fails
     */
    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        // Proceed with the original request
        val response = chain.proceed(chain.request)

        // Check for 401 Unauthorized response
        if (response.status == 401) {
            // Attempt to refresh the authentication token
            val tokenRefreshed = refreshToken.isRefreshed()

            if (tokenRefreshed) {
                // If token was refreshed successfully, retry the original request
                // The AuthInterceptor will add the new token to the request
                return chain.proceed(chain.request)
            } else {
                // If token refresh failed, notify the application
                onUnauthorized.onUnauthorized()
                throw UnauthorizedException(
                    "Authentication required. Token refresh failed or not implemented."
                )
            }
        }

        return response
    }
}