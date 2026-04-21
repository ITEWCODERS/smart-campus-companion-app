package com.example.smartcompanionapp.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * STEP 7 — WORKMANAGER SCHEDULER
 *
 * Schedules AnnouncementSyncWorker to run periodically in the background.
 *
 * KEY DECISIONS:
 *
 * 1. PERIODIC vs ONE-TIME work:
 *    We use PeriodicWorkRequest (repeating every 15 min) because:
 *    - One-time work fires once and stops. We need ongoing background checks.
 *    - 15 minutes is the MINIMUM interval WorkManager allows (OS restriction).
 *    - WorkManager may run it later than 15 min on low-battery or Doze devices,
 *      but it WILL eventually run — that's the guarantee.
 *
 * 2. UNIQUE work with KEEP policy:
 *    enqueueUniquePeriodicWork(..., ExistingPeriodicWorkPolicy.KEEP, ...) ensures:
 *    - Only ONE sync job ever exists in the queue.
 *    - Calling schedule() again (e.g., after app restart) doesn't add duplicates.
 *    - Use REPLACE instead of KEEP if you want a config change to reset the timer.
 *
 * 3. CONSTRAINTS:
 *    - NETWORK_CONNECTED: only run when internet is available (Firestore needs it).
 *    - No battery-not-low constraint because announcements may be urgent.
 *
 * 4. INITIAL DELAY = 0:
 *    The first run happens as soon as constraints are met (network available).
 *    Subsequent runs follow the 15-minute period.
 */
object AnnouncementWorkScheduler {

    private const val WORK_NAME = "announcement_sync_periodic"

    /**
     * Call this from Application.onCreate() or MainActivity.onCreate().
     * Safe to call multiple times — KEEP policy prevents duplicates.
     */
    fun schedule(context: Context) {
        // ── CONSTRAINTS ───────────────────────────────────────────────────────
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // needs internet for Firestore
            .build()

        // ── PERIODIC REQUEST ──────────────────────────────────────────────────
        val syncRequest = PeriodicWorkRequestBuilder<AnnouncementSyncWorker>(
            repeatInterval = 15,              // minimum allowed by WorkManager
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(             // if Result.retry() is returned:
                BackoffPolicy.EXPONENTIAL,   // wait 10s, 20s, 40s… before retrying
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // ── ENQUEUE ───────────────────────────────────────────────────────────
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // don't reset if already scheduled
            syncRequest
        )
    }

    /**
     * Immediately triggers a ONE-TIME sync (e.g., when user pulls to refresh).
     * This is separate from the periodic schedule and won't affect its timer.
     */
    fun syncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<AnnouncementSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueue(oneTimeRequest)
    }

    /**
     * Cancel the periodic sync (e.g., when user disables Campus Announcements
     * toggle in the Notifications settings screen).
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}