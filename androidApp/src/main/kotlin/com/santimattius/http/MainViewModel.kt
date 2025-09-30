package com.santimattius.http

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santimattius.http.configuration.CacheConfig
import com.santimattius.http.configuration.HttpClientConfig
import com.santimattius.http.configuration.LogLevel
import com.santimattius.http.extension.getBodyAs
import com.santimattius.http.interceptor.Interceptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.measureTimedValue

data class MainUiState(
    val response: String = "",
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val client = HttpClient.create(
        config = HttpClientConfig(
            baseUrl = "https://www.freetogame.com/api"
        ).enableLogging(true)
            .logLevel(LogLevel.BASIC)
            .cache(
                cacheConfig = CacheConfig(
                    enabled = true,
                    cacheDirectory = "http_cache",
                    maxCacheSize = 10L * 1024 * 1024, // 10 MB
                    cacheTtl = 60 * 60 * 1000 // 1 hour
                )
            )
    ).addInterceptors(object : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): HttpResponse {
            //Hello interceptor
            Log.d("MainViewModel", "Hello from interceptor")
            return chain.proceed(chain.request)
        }
    })

    fun call() {
        viewModelScope.launch {
            try {
                val request = HttpRequest.get("/game")
                    .queryParam("id", "475")
                    .header("Content-Type", "application/json")
                    .build()
                val execute = measureTimedValue { client.execute(request = request) }
                Log.d("MainViewModel", "call (${execute.duration}): ${execute.value}")
                val dto = execute.value.getBodyAs<Game>()
                Log.d("MainViewModel", "dto: $dto")
                _uiState.value = _uiState.value.copy(response = execute.value.body ?: "empty body")
            } catch (ex: Exception) {
                Log.e("MainViewModel", "call: error", ex)
            }
        }
    }
}

@Serializable
data class Game(
    val id: Long,
    val title: String,
    val thumbnail: String
)