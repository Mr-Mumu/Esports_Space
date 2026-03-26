package com.esports.space.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null
        while (attempt < maxRetries) {
            try {
                val response = chain.proceed(chain.request())
                if (response.code == 429) {
                    val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: (2L shl attempt)
                    response.close()
                    Thread.sleep(retryAfter * 1000)
                    attempt++
                    continue
                }
                if (response.code in 500..599 && attempt < maxRetries - 1) {
                    response.close()
                    Thread.sleep((2L shl attempt) * 1000)
                    attempt++
                    continue
                }
                return response
            } catch (e: IOException) {
                lastException = e
                attempt++
                if (attempt < maxRetries) Thread.sleep((2L shl attempt) * 1000)
            }
        }
        throw lastException ?: IOException("Max retries exceeded")
    }
}
