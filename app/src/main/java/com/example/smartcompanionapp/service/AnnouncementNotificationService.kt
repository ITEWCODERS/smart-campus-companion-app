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

object AnnouncementNotificationService {

    const val CHANNEL_ID = "campus_announcements_v2"
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
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                setSound(soundUri, audioAttributes)
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // Used by DashboardViewModel — has a real Room ID
    fun showAnnouncementNotification(context: Context, announcement: Announcement) {
        val baseId = stableId(announcement.title)

        val openPending = PendingIntent.getActivity(
            context,
            baseId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "all_announcements")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadPending = PendingIntent.getBroadcast(
            context,
            baseId + 50_000,
            Intent(context, MarkAsReadReceiver::class.java).apply {
                putExtra("announcement_id", announcement.id)
                putExtra("announcement_title", announcement.title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification, "Mark as Read", markReadPending)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(baseId, notification)
    }

    // Used by MyFirebaseMessagingService — title only, no Room ID yet
    fun showAnnouncementNotificationFromFcm(context: Context, title: String, body: String) {
        val baseId = stableId(title)

        val openPending = PendingIntent.getActivity(
            context,
            baseId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "all_announcements")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadPending = PendingIntent.getBroadcast(
            context,
            baseId + 50_000,
            Intent(context, MarkAsReadReceiver::class.java).apply {
                // No Room ID available — receiver will look up by title
                putExtra("announcement_title", title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📢 $title")
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle("📢 $title")
                    .setSummaryText("Campus Announcement")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification, "Mark as Read", markReadPending)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(baseId, notification)
    }

    fun showSummaryNotification(context: Context, count: Int) {
        val openPending = PendingIntent.getActivity(
            context,
            Int.MAX_VALUE - 1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "all_announcements")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Campus Announcements")
            .setContentText("You have $count new announcements.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setNumber(count)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(Int.MAX_VALUE - 1, notification)
    }

    fun stableId(title: String): Int {
        val h = title.hashCode()
        return Math.abs(if (h == Int.MIN_VALUE) 0 else h)
    }
}