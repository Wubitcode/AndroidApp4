package com.wubitcode.androidapp4.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules periodic background checks for new episodes
 * from subscribed podcasts.
 */
object PodcastUpdateScheduler {

    private const val UNIQUE_WORK_NAME =
        "superpodcast_update_check"

    /**
     * Schedules podcast update checks every six hours
     * when an Internet connection is available.
     */
    fun schedule(context: Context) {

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val workRequest =
            PeriodicWorkRequestBuilder<PodcastUpdateWorker>(
                6,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

        /*
         * KEEP prevents duplicate background schedules
         * when the app is opened repeatedly.
         */
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}