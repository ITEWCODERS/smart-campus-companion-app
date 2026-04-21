package com.example.smartcompanionapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartcompanionapp.MainActivity
import com.example.smartcompanionapp.R
import com.example.smartcompanionapp.data.model.Announcement

/**
 * STEP 4 — NOTIFICATION SERVICE
 *
 * Responsibilities:
 *  1. createNotificationChannel() — registers the OS channel ONCE (idempotent).
 *     Must be called before any notify() call. Safe to call repeatedly.
 *  2. showAnnouncementNotification() — builds and posts the notification.
 *     Called by AnnouncementSyncWorker (WorkManager) from the background.
 *
 * WHY this works when the app is closed:
 *  - WorkManager keeps a JobScheduler/AlarmManager wake-lock that survives app death.
 *  - This object is called from within the Worker's doWork() coroutine, which runs
 *    in the system process's thread pool — no foreground Activity needed.
 *
 * NOTIFICATION FEATURES:
 *  - Notification SOUND   → uses default ringtone via RingtoneManager
 *  - BigTextStyle         → expands to show full announcement content
 *  - "Mark as Read" action → fires MarkAsReadReceiver without opening the app
 *  - Tap-to-open          → deep-links into AllAnnouncementsScreen via MainActivity intent
 */
object AnnouncementNotificationService {

    const val CHANNEL_ID   = "campus_announcements"
    private const val CHANNEL_NAME = "Campus Announcements"
    private const val CHANNEL_DESC = "New announcements from your campus"

    // ── 1. CHANNEL SETUP ─────────────────────────────────────────────────────

    /**
     * Registers the notification channel with the OS.
     * On Android < O this is a no-op.
     * Safe to call every time WorkManager starts — the OS ignores duplicate registrations.
     *
     * SOUND: We attach the default notification sound to the CHANNEL, not the individual
     * notification, because on Android O+ channel-level audio settings take precedence.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // AudioAttributes tells the OS this sound is for a notification (not media/alarm)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT  // shows in shade, plays sound
            ).apply {
                description = CHANNEL_DESC
                setSound(soundUri, audioAttributes) // attach sound to channel
                enableLights(true)                  // LED indicator on supported devices
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ── 2. POST NOTIFICATION ─────────────────────────────────────────────────

    /**
     * Builds and posts one notification per announcement.
     * Uses announcement.id as the notification ID so each announcement has its own
     * notification slot (they stack instead of replacing each other).
     *
     * @param context   Application context — safe to pass from WorkManager
     * @param announcement  The new announcement to display
     */
    fun showAnnouncementNotification(context: Context, announcement: Announcement) {

        // ── TAP ACTION: open AllAnnouncementsScreen ───────────────────────────
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // The NavController in MainActivity reads this extra and navigates accordingly
            putExtra("navigate_to", "all_announcements")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            announcement.id,       // unique request code per announcement
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── MARK AS READ ACTION: fires BroadcastReceiver ──────────────────────
        // This lets users dismiss + mark read WITHOUT opening the app.
        val markReadIntent = Intent(context, MarkAsReadReceiver::class.java).apply {
            putExtra("announcement_id", announcement.id)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            // offset by 10000 to avoid colliding with openIntent's request code
            announcement.id + 10000,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── BUILD NOTIFICATION ────────────────────────────────────────────────
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)   // add a 24dp vector to res/drawable
            .setContentTitle("📢 ${announcement.title}")
            .setContentText(announcement.content)
            // BigTextStyle: tapping the notification expands it to show full content
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(announcement.content)
                    .setBigContentTitle("📢 ${announcement.title}")
                    .setSummaryText("Campus Announcement")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)   // tap → open app
            .setAutoCancel(true)                   // dismiss on tap
            .setSound(soundUri)                    // sound for pre-O devices
            // Action button visible in the notification shade
            .addAction(
                R.drawable.ic_notification,        // small icon for the action
                "Mark as Read",
                markReadPendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(announcement.id, notification)
    }
}