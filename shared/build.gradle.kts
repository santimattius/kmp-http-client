import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.skie)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.santimattius.kmp"
version = "1.0.0-ALPHA01"

kotlin {
    androidLibrary {
        namespace = "com.santimattius.http.client"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcFrameworkName = "KMPHttpClient"
    val xcf = XCFramework(xcFrameworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcFrameworkName

            // Specify CFBundleIdentifier to uniquely identify the framework
            binaryOption("bundleId", "io.github.santimattius.http.${xcFrameworkName}")
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.startup.runtime)
        }
        
        commonMain.dependencies {
            // Kotlin
            implementation(libs.kotlinx.coroutines.core)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
            // Okio
            implementation(libs.okio)
            
            // Ktor plugins

        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

skie {
    isEnabled.set(true)
    swiftBundling {
        enabled = true
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "http-client", version.toString())

    pom {
        name = "KMPHttpClient"
        description = "A lightweight Kotlin Multiplatform HTTP client built on top of Ktor."
        inceptionYear = "2025"
        url = "https://github.com/santimattius/kmp-http-client/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "santiago-mattiauda"
                name = "Santiago Mattiauda"
                url = "https://github.com/santimattius"
            }
        }
        scm {
            url = "https://github.com/santimattius/kmp-http-client/"
            connection = "scm:git:git://github.com/santimattius/kmp-http-client.git"
            developerConnection = "scm:git:ssh://git@github.com/santimattius/kmp-http-client.git"
        }
    }
}