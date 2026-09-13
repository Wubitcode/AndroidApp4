package com.wubitcode.androidapp4.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages podcast subscriptions stored locally on the device.
 *
 * Assignment 7 stored basic subscription state.
 * Assignment 8 expands this so the app can save enough podcast
 * information to build a complete "My Subscriptions" screen.
 */
object SubscriptionManager {

    private const val PREFS_NAME =
        "superpodcast_subscriptions"

    private const val SUBSCRIPTIONS_KEY =
        "saved_subscriptions"

    /*
     * Legacy key used by the Assignment 7 version.
     *
     * Keeping this temporarily allows podcasts previously
     * subscribed to in Assignment 7 to remain recognized.
     */
    private const val LEGACY_SUBSCRIPTIONS_KEY =
        "subscribed_podcasts"

    private val gson = Gson()

    /**
     * Returns all fully saved podcast subscriptions.
     */
    fun getSubscriptions(
        context: Context
    ): List<SubscribedPodcast> {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val json =
            preferences.getString(
                SUBSCRIPTIONS_KEY,
                null
            ) ?: return emptyList()

        return try {

            val type =
                object :
                    TypeToken<List<SubscribedPodcast>>() {}.type

            gson.fromJson<List<SubscribedPodcast>>(
                json,
                type
            ) ?: emptyList()

        } catch (_: Exception) {

            /*
             * If stored subscription data becomes invalid,
             * return an empty list instead of crashing the app.
             */
            emptyList()
        }
    }

    /**
     * Returns true when the specified podcast is subscribed.
     *
     * Both the new Assignment 8 data and the older Assignment 7
     * ID-only subscription data are checked.
     */
    fun isSubscribed(
        context: Context,
        podcastId: Long
    ): Boolean {

        val savedPodcast =
            getSubscriptions(context).any {
                it.podcastId == podcastId
            }

        if (savedPodcast) {
            return true
        }

        /*
         * Check Assignment 7's original ID-only storage
         * so previous subscriptions continue to work.
         */
        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val legacySubscriptions =
            preferences.getStringSet(
                LEGACY_SUBSCRIPTIONS_KEY,
                emptySet()
            ) ?: emptySet()

        return legacySubscriptions.contains(
            podcastId.toString()
        )
    }

    /**
     * Saves the complete podcast information required
     * by the My Subscriptions screen.
     *
     * If the podcast is already saved, its information
     * is replaced with the latest values.
     */
    fun subscribe(
        context: Context,
        podcast: SubscribedPodcast
    ) {

        val subscriptions =
            getSubscriptions(context)
                .toMutableList()

        /*
         * Remove an older copy before adding the latest
         * version so duplicate subscriptions are prevented.
         */
        subscriptions.removeAll {
            it.podcastId == podcast.podcastId
        }

        subscriptions.add(podcast)

        saveSubscriptions(
            context,
            subscriptions
        )

        /*
         * Also retain the ID in the legacy storage while
         * Assignment 8 is being developed.
         */
        saveLegacySubscriptionId(
            context,
            podcast.podcastId
        )
    }

    /**
     * Temporary compatibility method for code created
     * during Assignment 7.
     *
     * This keeps the project compiling until the details
     * screen is updated to save complete podcast information.
     */
    fun subscribe(
        context: Context,
        podcastId: Long
    ) {

        saveLegacySubscriptionId(
            context,
            podcastId
        )
    }

    /**
     * Removes a podcast from both the new subscription
     * list and Assignment 7's legacy ID storage.
     */
    fun unsubscribe(
        context: Context,
        podcastId: Long
    ) {

        val subscriptions =
            getSubscriptions(context)
                .toMutableList()

        subscriptions.removeAll {
            it.podcastId == podcastId
        }

        saveSubscriptions(
            context,
            subscriptions
        )

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val legacySubscriptions =
            preferences.getStringSet(
                LEGACY_SUBSCRIPTIONS_KEY,
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        legacySubscriptions.remove(
            podcastId.toString()
        )

        preferences.edit()
            .putStringSet(
                LEGACY_SUBSCRIPTIONS_KEY,
                legacySubscriptions
            )
            .apply()
    }

    /**
     * Writes the complete list of subscriptions
     * to SharedPreferences as JSON.
     */
    private fun saveSubscriptions(
        context: Context,
        subscriptions: List<SubscribedPodcast>
    ) {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val json =
            gson.toJson(subscriptions)

        preferences.edit()
            .putString(
                SUBSCRIPTIONS_KEY,
                json
            )
            .apply()
    }

    /**
     * Saves a podcast ID using the original Assignment 7
     * storage format for temporary backward compatibility.
     */
    private fun saveLegacySubscriptionId(
        context: Context,
        podcastId: Long
    ) {

        val preferences =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val legacySubscriptions =
            preferences.getStringSet(
                LEGACY_SUBSCRIPTIONS_KEY,
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        legacySubscriptions.add(
            podcastId.toString()
        )

        preferences.edit()
            .putStringSet(
                LEGACY_SUBSCRIPTIONS_KEY,
                legacySubscriptions
            )
            .apply()
    }
}