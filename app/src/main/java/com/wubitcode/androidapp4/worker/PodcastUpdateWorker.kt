package com.wubitcode.androidapp4.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.SubscriptionManager
import com.wubitcode.androidapp4.network.RetrofitClient
import com.wubitcode.androidapp4.network.RssParser

/**
 * Background worker that checks subscribed podcast RSS feeds
 * for newly published episodes.
 */
class PodcastUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "podcast_updates"
        private const val CHANNEL_NAME = "Podcast Updates"
        private const val UPDATE_PREFS = "podcast_update_tracking"
        private const val LATEST_EPISODE_PREFIX = "latest_episode_"
    }

    /**
     * Called by WorkManager when the scheduled background
     * podcast check is executed.
     */
    override fun doWork(): Result {
        return try {
            createNotificationChannel()

            val subscriptions =
                SubscriptionManager.getSubscriptions(applicationContext)

            subscriptions.forEach { podcast ->
                try {
                    checkPodcastForNewEpisode(
                        podcastId = podcast.podcastId,
                        podcastName = podcast.podcastName,
                        feedUrl = podcast.feedUrl
                    )
                } catch (_: Exception) {
                    // Continue checking other subscriptions if one feed fails.
                }
            }

            Result.success()

        } catch (_: Exception) {
            Result.retry()
        }
    }

    /**
     * Downloads one RSS feed and compares its newest episode
     * with the last episode recorded by SuperPodcast.
     */
    private fun checkPodcastForNewEpisode(
        podcastId: Long,
        podcastName: String,
        feedUrl: String
    ) {
        if (feedUrl.isBlank()) {
            return
        }

        val response =
            RetrofitClient.apiService
                .getPodcastFeed(feedUrl)
                .execute()

        if (!response.isSuccessful) {
            return
        }

        val responseBody =
            response.body() ?: return

        val episodes =
            responseBody.use { body ->
                body.byteStream().use { inputStream ->
                    RssParser.parse(inputStream)
                }
            }

        val latestEpisode =
            episodes.firstOrNull() ?: return

        val latestEpisodeIdentifier =
            latestEpisode.title.trim()

        if (latestEpisodeIdentifier.isBlank()) {
            return
        }

        val preferences =
            applicationContext.getSharedPreferences(
                UPDATE_PREFS,
                Context.MODE_PRIVATE
            )

        val key =
            LATEST_EPISODE_PREFIX + podcastId

        val previouslySavedEpisode =
            preferences.getString(key, null)

        /*
         * The first check stores the current newest episode
         * without showing a notification.
         */
        if (previouslySavedEpisode == null) {
            preferences.edit()
                .putString(
                    key,
                    latestEpisodeIdentifier
                )
                .apply()

            return
        }

        /*
         * A changed newest episode means that a new episode
         * has appeared since the previous background check.
         */
        if (previouslySavedEpisode != latestEpisodeIdentifier) {
            showNewEpisodeNotification(
                podcastId = podcastId,
                podcastName = podcastName,
                episodeTitle = latestEpisode.title
            )

            preferences.edit()
                .putString(
                    key,
                    latestEpisodeIdentifier
                )
                .apply()
        }
    }

    /**
     * Creates the Android notification channel used for
     * podcast-update notifications.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description =
                        "Notifications for new episodes from subscribed podcasts."
                }

            val notificationManager =
                applicationContext.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a new-episode notification when notification
     * permission is available.
     */
    private fun showNewEpisodeNotification(
        podcastId: Long,
        podcastName: String,
        episodeTitle: String
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("New episode: $podcastName")
                .setContentText(episodeTitle)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(episodeTitle)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify(
                podcastId.hashCode(),
                notification
            )
    }
}