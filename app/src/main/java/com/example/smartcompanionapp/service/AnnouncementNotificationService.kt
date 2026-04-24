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
 * Service to handle creation of notification channels and displaying notifications.
 */
object AnnouncementNotificationService {

    const val CHANNEL_ID   = "campus_announcements_v2"
    private const val CHANNEL_NAME = "Campus Announcements"
    private const val CHANNEL_DESC = "New announcements from your campus"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showAnnouncementNotification(context: Context, announcement: Announcement) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "all_announcements")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            announcement.id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadIntent = Intent(context, MarkAsReadReceiver::class.java).apply {
            putExtra("announcement_id", announcement.id)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            announcement.id + 10000,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📢 ${announcement.title}")
            .setContentText(announcement.content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(announcement.content)
                    .setBigContentTitle("📢 ${announcement.title}")
                    .setSummaryText("Campus Announcement")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .addAction(R.drawable.ic_notification, "Mark as Read", markReadPendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(announcement.id, notification)
    }

    /**
     * Shows a summary notification when multiple announcements are found at once.
     * Prevents the system from "shedding" (blocking) the app for being too noisy.
     */
    fun showSummaryNotification(context: Context, count: Int) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "all_announcements")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            9999, // static ID for summary
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Campus Announcements")
            .setContentText("You have $count new announcements.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setNumber(count)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(9999, notification)
    }
}
