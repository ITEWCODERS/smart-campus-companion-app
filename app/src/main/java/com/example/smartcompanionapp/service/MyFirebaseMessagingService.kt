package com.example.smartcompanionapp.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.smartcompanionapp.MainActivity
import com.example.smartcompanionapp.R
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.session.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives FCM push notifications.
 * Optimised for Infinix/Tecno background reliability.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessaging"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // ── STEP 0: Check if notifications are enabled ─────────────────────
        val sessionManager = SessionManager(applicationContext)
        if (!sessionManager.isNotificationsEnabled()) {
            Log.d(TAG, "Notifications are disabled in settings. Skipping...")
            return
        }

        // ── STEP 1: Parse payload ────────────────────────────────────────────
        val data  = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Campus Update"
        val body  = data["body"]  ?: remoteMessage.notification?.body  ?: "New announcement posted."

        // ── STEP 2: Immediate visual notification ────────────────────────────
        // We show the notification FIRST to ensure visibility before the system 
        // has a chance to limit background processing.
        wakeScreen()
        showNotification(title, body)

        // ── STEP 3: Background Sync ──────────────────────────────────────────
        // Only trigger sync if a user is actually logged in.
        val userId = sessionManager.getUsername() ?: ""
        if (userId.isNotEmpty()) {
            // Use WorkManager for the database sync because it survives if 
            // this Service process is killed.
            val syncWork = OneTimeWorkRequestBuilder<AnnouncementSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(syncWork)
        }
    }

    private fun wakeScreen() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "SmartCampus:WakeLock"
            )
            wakeLock.acquire(5000L) // Wakes screen for 5 seconds
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock failed", e)
        }
    }

    private fun showNotification(title: String, body: String) {
        AnnouncementNotificationService.createNotificationChannel(this)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "all_announcements")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadIntent = Intent(this, MarkAsReadReceiver::class.java).apply {
            putExtra("announcement_title", title)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt() + 1,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, AnnouncementNotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📢 $title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification, "Mark as Read", markReadPendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(AnnouncementNotificationService.stableId(title), notification)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New token: $token")
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
    }
}
