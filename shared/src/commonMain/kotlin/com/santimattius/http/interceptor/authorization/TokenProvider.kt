package com.santimattius.http.interceptor.authorization

interface TokenProvider {
    suspend fun getToken(): String?
}