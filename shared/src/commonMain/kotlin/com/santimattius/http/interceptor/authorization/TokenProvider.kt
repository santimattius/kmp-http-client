package com.santimattius.http.interceptor.authorization

/**
 * A provider interface for retrieving authentication tokens.
 *
 * Implement this interface to provide authentication tokens to the [AuthInterceptor].
 * The token will be automatically added to outgoing requests as a Bearer token.
 *
 * ## Example Implementation
 * ```kotlin
 * class MyTokenProvider : TokenProvider {
 *     private val authRepository: AuthRepository // Your auth repository
 *
 *     override suspend fun getToken(): String? {
 *         return authRepository.getCurrentToken()
 *     }
 * }
 * ```
 *
 * ## Usage with AuthInterceptor
 * ```kotlin
 * val client = HttpClient {
 *     install(AuthInterceptor(MyTokenProvider()))
 * }
 * ```
 *
 * @see AuthInterceptor The interceptor that uses this provider
 */
interface TokenProvider {
    /**
     * Retrieves the current authentication token.
     *
     * This function should return the current valid authentication token,
     * or `null` if no token is available (in which case no Authorization header will be added).
     *
     * ## Implementation Notes
     * - This is a suspending function, so you can perform asynchronous operations
     *   (e.g., fetching from a database or secure storage)
     * - The function should return quickly and not perform long-running operations
     * - If token refresh is needed, it should be handled by the implementation
     *
     * @return The current authentication token as a String, or `null` if no token is available
     */
    suspend fun getToken(): String?
}