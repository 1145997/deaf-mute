package dev.forint.deafmute.cameraxdemo.repository

import dev.forint.deafmute.cameraxdemo.network.api.RecognitionApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RecognitionApiFactory {
    fun create(baseUrl: String): RecognitionApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecognitionApiService::class.java)
    }

    fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().ifBlank {
            throw IllegalArgumentException("Recognition API Base 不能为空")
        }
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}

