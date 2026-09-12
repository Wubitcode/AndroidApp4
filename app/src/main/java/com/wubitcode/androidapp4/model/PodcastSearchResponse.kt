package com.wubitcode.androidapp4.model

/**
 * Represents the complete response returned by the iTunes Search API.
 *
 * The API returns the number of results and a list of podcasts.
 */
data class PodcastSearchResponse(
    val resultCount: Int,
    val results: List<Podcast>
)