package com.santimattius.http

/**
 * Interface representing platform-specific functionality in a Kotlin Multiplatform project.
 *
 * This interface defines the contract for platform-specific implementations
 * that provide access to platform-specific features or information.
 *
 * ## Platform-Specific Implementations
 * - **Android**: Returns "Android" as the platform name
 * - **iOS**: Returns the device name (e.g., "iPhone", "iPad")
 * - **JVM**: Returns "JVM" as the platform name
 * - **JS**: Returns "JavaScript" as the platform name
 *
 * @see getPlatform Function to retrieve the platform-specific implementation
 */
interface Platform {
    /**
     * The name of the current platform.
     *
     * This property should return a string that identifies the current platform.
     * The exact value depends on the platform implementation.
     */
    val name: String
}

/**
 * Returns the platform-specific implementation of the [Platform] interface.
 *
 * This is an `expect` declaration in the common source set, which means each platform
 * must provide an actual implementation. The implementation is provided by the
 * Kotlin Multiplatform framework based on the target platform.
 *
 * @return The platform-specific implementation of [Platform]
 */
expect fun getPlatform(): Platform