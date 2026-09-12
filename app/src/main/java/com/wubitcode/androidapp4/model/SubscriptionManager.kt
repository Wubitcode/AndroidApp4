package com.wubitcode.androidapp4.model

import android.content.Context

/**
 * Manages locally saved podcast subscriptions.
 *
 * SharedPreferences is used so subscription choices remain
 * available after the app is closed and reopened.
 */
object SubscriptionManager {

    private const val PREFS_NAME = "superpodcast_subscriptions"
    private const val SUBSCRIPTIONS_KEY = "subscribed_podcasts"

    /**
     * Returns true when the specified podcast is already subscribed.
     */
    fun isSubscribed(
        context: Context,
        podcastId: Long
    ): Boolean {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val subscriptions =
            preferences.getStringSet(
                SUBSCRIPTIONS_KEY,
                emptySet()
            ) ?: emptySet()

        return subscriptions.contains(podcastId.toString())
    }

    /**
     * Saves a podcast subscription.
     */
    fun subscribe(
        context: Context,
        podcastId: Long
    ) {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val subscriptions =
            preferences.getStringSet(
                SUBSCRIPTIONS_KEY,
                emptySet()
            )?.toMutableSet() ?: mutableSetOf()

        subscriptions.add(podcastId.toString())

        preferences.edit()
            .putStringSet(
                SUBSCRIPTIONS_KEY,
                subscriptions
            )
            .apply()
    }

    /**
     * Removes a saved podcast subscription.
     */
    fun unsubscribe(
        context: Context,
        podcastId: Long
    ) {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val subscriptions =
            preferences.getStringSet(
                SUBSCRIPTIONS_KEY,
                emptySet()
            )?.toMutableSet() ?: mutableSetOf()

        subscriptions.remove(podcastId.toString())

        preferences.edit()
            .putStringSet(
                SUBSCRIPTIONS_KEY,
                subscriptions
            )
            .apply()
    }
}