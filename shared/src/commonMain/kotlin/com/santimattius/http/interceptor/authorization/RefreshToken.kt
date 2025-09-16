package com.santimattius.http.interceptor.authorization

/**
 * Interface defining the contract for token refresh operations.
 *
 * Implement this interface to provide custom token refresh logic for the [TokenRefreshInterceptor].
 * The [isRefreshed] method should contain the logic to refresh the authentication token.
 *
 * ## Example Implementation
 * ```kotlin
 * class MyRefreshToken : RefreshToken {
 *     private val authRepository: AuthRepository // Your auth repository
 *     private var refreshAttempted = false
 *
 *     override suspend fun isRefreshed(): Boolean {
 *         if (refreshAttempted) return false
 *         refreshAttempted = true
 *         
 *         return try {
 *             val newToken = authRepository.refreshToken()
 *             // Store the new token in your secure storage
 *             true
 *         } catch (e: Exception) {
 *             // Handle refresh failure
 *             false
 *         }
 *     }
 * }
 * ```
 *
 * @see TokenRefreshInterceptor The interceptor that uses this interface
 */
interface RefreshToken {
    /**
     * Attempts to refresh the authentication token.
     *
     * This method should implement the logic to refresh the authentication token.
     * It will be called by [TokenRefreshInterceptor] when a 401 Unauthorized response is received.
     *
     * ## Implementation Guidelines
     * - This method should be idempotent (calling it multiple times has the same effect as calling it once)
     * - It should return `true` only if the token was successfully refreshed
     * - It should return `false` if the token could not be refreshed (e.g., network error, invalid refresh token)
     * - Any exceptions thrown will be caught and treated as a refresh failure
     *
     * @return `true` if the token was successfully refreshed, `false` otherwise
     */
    suspend fun isRefreshed(): Boolean
}

/**
 * Callback interface for handling unauthorized states after a failed token refresh.
 *
 * Implement this interface to be notified when authentication is required and token refresh has failed.
 * This typically happens when the refresh token is invalid or expired.
 *
 * ## Example Usage
 * ```kotlin
 * val callback = object : UnauthorizedCallback {
 *     override suspend fun onUnauthorized() {
 *         // Navigate to login screen
 *         // Clear user session
 *     }
 * }
 * ```
 *
 * @see TokenRefreshInterceptor The interceptor that uses this callback
 */
interface UnauthorizedCallback {
    /**
     * Called when authentication is required and token refresh has failed.
     *
     * This method is invoked by [TokenRefreshInterceptor] when:
     * 1. A 401 Unauthorized response is received
     * 2. The token refresh attempt (via [RefreshToken.isRefreshed]) has failed
     *
     * ## Common Actions
     * - Navigate to the login screen
     * - Clear user session data
     * - Notify the user that they need to log in again
     */
    suspend fun onUnauthorized()
}