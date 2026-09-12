package com.wubitcode.androidapp4.model

/**
 * Represents one podcast episode retrieved from an RSS feed.
 *
 * SuperPodcast uses this model to display episode information
 * and later provide audio playback.
 */
data class Episode(
    val title: String,
    val description: String?,
    val publicationDate: String?,
    val audioUrl: String?
)