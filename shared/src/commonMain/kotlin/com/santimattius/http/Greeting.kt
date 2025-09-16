package com.santimattius.http

/**
 * A simple greeting class that demonstrates platform-specific functionality.
 *
 * This class provides a basic example of how to use platform-specific code
 * in a Kotlin Multiplatform project. It retrieves the current platform name
 * and returns a greeting message that includes the platform name.
 *
 * ## Example Usage
 * ```kotlin
 * val greeting = Greeting()
 * println(greeting.greet()) // Output: "Hello, Android!" or "Hello, iOS!" etc.
 * ```
 *
 * @see Platform The interface providing platform-specific information
 * @see getPlatform Function to retrieve the platform-specific implementation
 */
class Greeting {
    private val platform = getPlatform()

    /**
     * Returns a greeting message that includes the current platform name.
     *
     * This method demonstrates how to use platform-specific code by accessing
     * the platform name through the [Platform] interface.
     *
     * @return A greeting string that includes the current platform name
     *
     * @sample com.santimattius.http.samples.greetingSample
     */
    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}