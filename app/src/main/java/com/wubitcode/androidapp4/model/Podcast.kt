package com.wubitcode.androidapp4.model

/**
 * Represents one podcast returned by the iTunes Search API.
 *
 * Only the fields needed by SuperPodcast are included here.
 */
data class Podcast(
    val collectionId: Long,
    val collectionName: String?,
    val artistName: String?,
    val artworkUrl100: String?,
    val feedUrl: String?,
    val primaryGenreName: String?
)