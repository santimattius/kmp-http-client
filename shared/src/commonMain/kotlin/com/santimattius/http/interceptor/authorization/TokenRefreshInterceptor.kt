package com.santimattius.http.interceptor.authorization

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.UnauthorizedException
import com.santimattius.http.interceptor.Interceptor

/**
 * Interceptor that handles 401 Unauthorized responses by refreshing the token and retrying.
 *
 * @property refreshToken A suspending function that refreshes the token
 * @property onUnauthorized Callback when a 401 response is received (after refresh attempt)
 */
class TokenRefreshInterceptor(
    private val refreshToken: RefreshToken,
    private val onUnauthorized: UnauthorizedCallback
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
        val response = chain.proceed(chain.request)

        // Check for 401 Unauthorized
        if (response.status == 401) {
            // Try to refresh token
            val tokenRefreshed = refreshToken.isRefreshed()

            if (tokenRefreshed) {
                // Retry the original request with new token
                return chain.proceed(chain.request)
            } else {
                // Notify about unauthorized state
                onUnauthorized.onUnauthorized()
                throw UnauthorizedException("Authentication required")
            }
        }

        return response
    }
}