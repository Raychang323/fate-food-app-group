package com.fatefulsupper.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log // Added for logging
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fatefulsupper.app.MainActivity // To open MainActivity when notification is clicked
import com.fatefulsupper.app.R // For notification icon

object NotificationHelper {

    private const val TAG = "NotificationHelper" // Added for logging
    private const val CHANNEL_ID = "supper_notification_channel"
    private const val CHANNEL_NAME = "宵夜推薦通知"
    private const val CHANNEL_DESCRIPTION = "接收每日宵夜推薦的通知"
    private const val NOTIFICATION_ID = 101 // Unique ID for the notification

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // MODIFIED: Changed importance to HIGH for Heads-up notification
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            // MODIFIED: Updated log to reflect importance
            Log.d(TAG, "Notification channel '$CHANNEL_ID' created with importance: $importance.")
        }
    }

    fun showNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("該吃宵夜啦！")
            .setContentText("看看今天有什麼推薦的宵夜吧？")
            // MODIFIED: Changed priority to HIGH for Heads-up notification (especially for pre-Oreo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Consider adding .setDefaults(NotificationCompat.DEFAULT_ALL) or specific sound/vibrate
            // if you want sound and vibration for heads-up on all versions.
            // For Android O+, sound/vibration are configured on the channel itself.

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification will not be shown on Android 13+.")
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification might not be shown on older versions or behavior might be inconsistent.")
                }
                return
            }
            Log.d(TAG, "POST_NOTIFICATIONS permission granted. Attempting to show notification.")
            notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Notification with ID $NOTIFICATION_ID dispatched.")
        }
    }
}