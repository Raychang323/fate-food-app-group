package com.fatefulsupper.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.fatefulsupper.app.receiver.NotificationReceiver
import java.util.Calendar
import java.util.Locale

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private const val PENDING_INTENT_REQUEST_CODE_BASE = 200
    private const val TEST_NOTIFICATION_REQUEST_CODE = 999

    fun scheduleNotifications(context: Context) {
        val sharedPreferences = context.getSharedPreferences(
            SetupConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        val selectedDays = sharedPreferences.getStringSet(
            SetupConstants.KEY_NOTIFICATION_DAYS,
            SetupConstants.DEFAULT_NOTIFICATION_DAYS
        ) ?: SetupConstants.DEFAULT_NOTIFICATION_DAYS

        val hour = sharedPreferences.getInt(
            SetupConstants.KEY_NOTIFICATION_HOUR,
            SetupConstants.DEFAULT_NOTIFICATION_HOUR
        )
        val minute = sharedPreferences.getInt(
            SetupConstants.KEY_NOTIFICATION_MINUTE,
            SetupConstants.DEFAULT_NOTIFICATION_MINUTE
        )

        // Cancel all previously scheduled notifications (including regular and old test ones)
        // This is a broader cancel, ensuring a clean slate before scheduling new regular notifications.
        cancelScheduledNotifications(context)

        if (selectedDays.isEmpty()) {
            Log.d(TAG, "No days selected for notifications. All notification schedules cancelled.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        selectedDays.forEach { dayString ->
            val calendarDay = mapDayStringToCalendarDay(dayString)
            if (calendarDay == -1) {
                Log.e(TAG, "Invalid day string: $dayString")
                return@forEach
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calendarDay)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (System.currentTimeMillis() > timeInMillis) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.fatefulsupper.app.SCHEDULED_NOTIFICATION_FOR_DAY_$calendarDay"
            }

            val pendingIntentRequestCode = PENDING_INTENT_REQUEST_CODE_BASE + calendarDay
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                pendingIntentRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                Log.d(
                    TAG,
                    "[setRepeating] Scheduling notification for ${dayString.uppercase(Locale.ROOT)} at $hour:$minute. Next trigger: ${calendar.time}"
                )
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "[setRepeating] SecurityException while scheduling notification: ${e.message}")
            }
        }
    }

    fun scheduleTestNotification(context: Context, triggerDelaySeconds: Int = 30) {
        Log.d(TAG, "Attempting to schedule a SINGLE TEST NOTIFICATION to trigger in $triggerDelaySeconds seconds.")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. First, cancel any PREVIOUSLY scheduled test notification specifically
        val oldTestIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.fatefulsupper.app.TEST_NOTIFICATION"
        }
        val oldTestPendingIntent = PendingIntent.getBroadcast(
            context,
            TEST_NOTIFICATION_REQUEST_CODE,
            oldTestIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Use FLAG_NO_CREATE to check if it exists
        )
        if (oldTestPendingIntent != null) {
            alarmManager.cancel(oldTestPendingIntent)
            oldTestPendingIntent.cancel() // Also cancel the PendingIntent itself
            Log.d(TAG, "Cancelled PREVIOUS test notification (request code $TEST_NOTIFICATION_REQUEST_CODE)")
        }

        // Create the NEW intent and pendingIntent for the test notification
        val newTestIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.fatefulsupper.app.TEST_NOTIFICATION"
            putExtra("test_message", "This is a TEST NOTIFICATION!")
        }

        val newTestPendingIntent = PendingIntent.getBroadcast(
            context,
            TEST_NOTIFICATION_REQUEST_CODE, // Re-using the same request code is fine, it will update due to FLAG_UPDATE_CURRENT
            newTestIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and higher
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM and user must grant it via settings for this test notification.")
                Toast.makeText(context, "無法排程此測試通知的精確時間，請在應用程式設定中允許「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                
                try {
                    Log.d(TAG, "Attempting to open ACTION_REQUEST_SCHEDULE_EXACT_ALARM settings.")
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open ACTION_REQUEST_SCHEDULE_EXACT_ALARM settings, falling back to ACTION_APPLICATION_DETAILS_SETTINGS.", e)
                    try {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e2: Exception) {
                         Log.e(TAG, "Failed to open ACTION_APPLICATION_DETAILS_SETTINGS.", e2)
                         Toast.makeText(context, "請手動前往應用程式設定頁面授予「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                    }
                }
                return // Stop execution if permission is not granted
            }
        }
        
        // Note: We are no longer calling the broad cancelScheduledNotifications(context) here
        // to avoid cancelling the PendingIntent we just created for the new test notification.
        // The specific cancellation of the *old* test notification is handled above.

        val calendar = Calendar.getInstance().apply { // Calendar setup should be after permission check & old alarm cancellation
            add(Calendar.SECOND, triggerDelaySeconds)
        }

        try {
            Log.d(TAG, "[setExact] Attempting to schedule NEW SINGLE TEST NOTIFICATION to trigger at: ${calendar.time}")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                newTestPendingIntent // Use the new pendingIntent
            )
            Log.d(TAG, "[setExact] NEW SINGLE TEST NOTIFICATION scheduled successfully for ${calendar.time}")
            Toast.makeText(context, "已設定測試通知，將於 $triggerDelaySeconds 秒後觸發", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "[setExact] SecurityException while scheduling test notification: ${e.message}", e)
            Toast.makeText(context, "設定測試通知時發生安全性錯誤", Toast.LENGTH_LONG).show()
        }
    }

    fun cancelScheduledNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d(TAG, "Cancelling all scheduled notifications (regular and any active test).")

        // Cancel repeating notification schedules
        val allPossibleDays = SetupConstants.DEFAULT_NOTIFICATION_DAYS.union(
            setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        )
        allPossibleDays.forEach { dayString ->
            val calendarDay = mapDayStringToCalendarDay(dayString)
            if (calendarDay != -1) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = "com.fatefulsupper.app.SCHEDULED_NOTIFICATION_FOR_DAY_$calendarDay"
                }
                val pendingIntentRequestCode = PENDING_INTENT_REQUEST_CODE_BASE + calendarDay
                val pendingIntent = PendingIntent.getBroadcast(
                    context, pendingIntentRequestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "Cancelled repeating notification schedule for request code $pendingIntentRequestCode")
                }
            }
        }

        // Cancel test notification schedule specifically as well (this makes the function a complete cancel-all)
        val testNotificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.fatefulsupper.app.TEST_NOTIFICATION"
        }
        val testNotificationPendingIntent = PendingIntent.getBroadcast(
            context, TEST_NOTIFICATION_REQUEST_CODE, testNotificationIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (testNotificationPendingIntent != null) {
            alarmManager.cancel(testNotificationPendingIntent)
            testNotificationPendingIntent.cancel()
            Log.d(TAG, "Cancelled active test notification schedule from general cancel (request code $TEST_NOTIFICATION_REQUEST_CODE)")
        }
    }

    private fun mapDayStringToCalendarDay(dayString: String): Int {
        return when (dayString.uppercase(Locale.ROOT)) {
            "SUNDAY" -> Calendar.SUNDAY
            "MONDAY" -> Calendar.MONDAY
            "TUESDAY" -> Calendar.TUESDAY
            "WEDNESDAY" -> Calendar.WEDNESDAY
            "THURSDAY" -> Calendar.THURSDAY
            "FRIDAY" -> Calendar.FRIDAY
            "SATURDAY" -> Calendar.SATURDAY
            else -> -1 
        }
    }
}