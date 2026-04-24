package com.example.smartcompanionapp.worker

import android.content.Context
import androidx.work.*
import com.example.smartcompanionapp.service.AnnouncementSyncWorker
import java.util.concurrent.TimeUnit

/**
 * STEP 7 — WORKMANAGER SCHEDULER
 *
 * Schedules AnnouncementSyncWorker to run periodically in the background.
 */
object AnnouncementWorkScheduler {

    private const val WORK_NAME = "announcement_sync_periodic"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<AnnouncementSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun syncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<AnnouncementSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeRequest)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
