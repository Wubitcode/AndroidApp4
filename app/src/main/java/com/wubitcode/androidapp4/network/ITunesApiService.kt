package com.wubitcode.androidapp4.network

import com.wubitcode.androidapp4.model.PodcastSearchResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Defines the network requests used by SuperPodcast.
 *
 * The service supports:
 * - Searching for podcasts through the iTunes Search API.
 * - Downloading a selected podcast's RSS feed.
 */
interface ITunesApiService {

    /**
     * Searches iTunes for podcasts matching the user's search term.
     *
     * Example:
     * https://itunes.apple.com/search
     * ?term=cybersecurity
     * &media=podcast
     * &entity=podcast
     * &limit=25
     */
    @GET("search")
    fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast",
        @Query("entity") entity: String = "podcast",
        @Query("limit") limit: Int = 25
    ): Call<PodcastSearchResponse>

    /**
     * Downloads the RSS/XML feed for a selected podcast.
     *
     * @Url allows Retrofit to use the complete RSS URL returned
     * by the podcast search instead of the normal iTunes base URL.
     */
    @GET
    fun getPodcastFeed(
        @Url feedUrl: String
    ): Call<ResponseBody>
}