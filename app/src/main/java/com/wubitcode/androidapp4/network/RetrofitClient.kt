package com.wubitcode.androidapp4.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides a single Retrofit instance for SuperPodcast.
 *
 * Retrofit handles communication with the iTunes Search API,
 * while Gson converts JSON responses into Kotlin data classes.
 */
object RetrofitClient {

    private const val BASE_URL = "https://itunes.apple.com/"

    /**
     * API service used throughout the app for podcast searches.
     *
     * The instance is created only when it is first needed.
     */
    val apiService: ITunesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApiService::class.java)
    }
}