package com.wubitcode.androidapp4.model

/**
 * Represents a podcast that the user has saved as a subscription.
 *
 * The information stored here is enough to display the podcast
 * in the My Subscriptions screen and reopen its details and episodes.
 */
data class SubscribedPodcast(
    val podcastId: Long,
    val podcastName: String,
    val artistName: String,
    val genre: String,
    val feedUrl: String
)