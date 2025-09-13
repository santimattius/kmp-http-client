package com.santimattius.http

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform