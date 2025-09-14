package com.santimattius.http.interceptor

import com.santimattius.http.HttpResponse
import com.santimattius.http.exception.UnauthorizedException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

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
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun authInterceptor(
    headerName: String = "Authorization",
    tokenPrefix: String = "Bearer ",
    tokenProvider: TokenProvider
): Interceptor = AuthInterceptor(tokenProvider, headerName, tokenPrefix)

interface TokenProvider {

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    suspend fun getToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            fetchToken { token ->
                continuation.resume(token)
            }
        }
    }

    fun fetchToken(onTokenFetched: (String?) -> Unit)
}

/**
 * Creates a token refresh interceptor.
 */

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun tokenRefreshInterceptor(
    refreshToken: suspend () -> Boolean,
    onUnauthorized: suspend () -> Unit = {}
): Interceptor = TokenRefreshInterceptor(refreshToken, onUnauthorized)
