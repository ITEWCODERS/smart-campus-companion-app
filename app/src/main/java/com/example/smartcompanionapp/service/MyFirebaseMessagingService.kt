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
import com.example.smartcompanionapp.data.session.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessaging"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle both Data messages and Notification messages
        val data  = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "New Announcement"
        val body  = data["body"]  ?: remoteMessage.notification?.body  ?: "Tap to see what's new."

        // ── STEP 1: Always show notification visually (Instant) ──────────────
        wakeScreen()
        showNotification(title, body)

        // ── STEP 2: Trigger background sync if a user is logged in ───────────
        val userId = SessionManager(applicationContext).getUsername()
        if (!userId.isNullOrBlank()) {
            val syncWork = OneTimeWorkRequestBuilder<AnnouncementSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(syncWork)
        }
    }

    private fun wakeScreen() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "SmartCampus:AnnouncementWakeLock"
        )
        wakeLock.acquire(3000L)
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

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, AnnouncementNotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification, "Mark as Read", markReadPendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
    }
}
