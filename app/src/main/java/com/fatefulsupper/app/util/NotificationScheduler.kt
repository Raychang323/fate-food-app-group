package com.fatefulsupper.app.util

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
// import android.content.SharedPreferences // Not directly needed in this snippet if prefs is passed or used locally
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.fatefulsupper.app.receiver.NotificationReceiver
import java.util.Calendar
import java.util.Locale

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private const val PENDING_INTENT_REQUEST_CODE_BASE = 200
    private const val TEST_NOTIFICATION_REQUEST_CODE = 999
    private const val PREF_KEY_USER_DENIED_LOCATION_PROMPT = "user_denied_location_prompt"

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
            // checkLocationServices(context) // Optionally check here too
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
        checkLocationServices(context) // Check location services after scheduling
    }

    fun checkLocationServices(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager? // Nullable for safety
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null, cannot check location services.")
            // No Toast here as per UX suggestion for "Cannot detect system location services" -
            // This case might be better handled by the calling fragment (LazyModeFragment) if it needs a Toast.
            // Or, if a generic Toast is desired here, it would be:
            // Toast.makeText(context, "無法偵測到系統定位服務，請檢查是否已開啟。", Toast.LENGTH_LONG).show()
            // For now, following the principle of reducing Toasts from this utility.
            return
        }

        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            Log.e(TAG, "Cannot access GPS_PROVIDER for location check", ex)
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            Log.e(TAG, "Cannot access NETWORK_PROVIDER for location check", ex)
        }

        val prefs = context.getSharedPreferences(SetupConstants.PREFS_NAME, Context.MODE_PRIVATE)

        if (!gpsEnabled && !networkEnabled) {
            val userPreviouslyDenied = prefs.getBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, false)

            if (userPreviouslyDenied) {
                Log.d(TAG, "User previously denied location prompt. Proceeding silently as per UX guidelines.")
                // Removed Toast: "若沒有使用地點則使用大概定位"
                return // Exit: User has denied, app should use approximate/no location logic silently.
            }

            Log.d(TAG, "Location services are not enabled. Prompting user if possible.")
            if (context is Activity) {
                AlertDialog.Builder(context)
                    .setTitle("允許「Fateful Supper」存取您的位置嗎？") // Keep title
                    .setMessage("為了協助您搜尋附近的餐廳，「Fateful Supper」需要存取您的位置資訊。建議開啟定位服務以獲得更精準的搜尋結果。") // Keep message
                    .setPositiveButton("是，前往設定") { _, _ ->
                        Log.d(TAG, "User chose to open location settings.")
                        prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, false).apply()
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to open location settings via dialog.", e)
                            // Keep Toast: Essential feedback for failed action.
                            Toast.makeText(context, "請手動前往系統設定開啟定位服務。", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("否，暫不開啟") { _, _ ->
                        Log.d(TAG, "User chose not to open location settings. Updating pref and proceeding silently.")
                        prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, true).apply()
                        // Removed Toast: "若沒有使用地點則使用大概定位"
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // Fallback for non-Activity context
                if (userPreviouslyDenied) {
                     Log.w(TAG, "Context is not an Activity, and user previously denied. Proceeding silently.")
                     // Removed Toast: "若沒有使用地點則使用大概定位"
                } else {
                    // This is the first time for non-activity context, and location is off.
                    Log.w(TAG, "Context is not an Activity, cannot show AlertDialog. Using Toast and attempting to open settings directly.")
                    // Keep Toast: Initial prompt for non-Activity context.
                    Toast.makeText(context, "為了搜尋附近餐廳，請開啟定位服務。", Toast.LENGTH_LONG).show()
                    try {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        // No Toast on success here, let the system settings opening be the feedback.
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to open location settings from non-activity context.", e)
                        // Keep Toast: Essential feedback for failed action.
                        Toast.makeText(context, "請手動前往系統設定開啟定位服務。", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Log.d(TAG, "Location services are enabled.")
            // If location services are enabled, reset the denial flag so user can be prompted again if they disable it later
            prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, false).apply()
        }
    }

    fun scheduleTestNotification(context: Context, triggerDelaySeconds: Int = 30) {
        Log.d(TAG, "Attempting to schedule a SINGLE TEST NOTIFICATION to trigger in $triggerDelaySeconds seconds.")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel any previous test notification
        // 1. First, cancel any PREVIOUSLY scheduled test notification specifically
        val oldTestIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.fatefulsupper.app.TEST_NOTIFICATION"
        }
        val oldTestPendingIntent = PendingIntent.getBroadcast(
            context,
            TEST_NOTIFICATION_REQUEST_CODE,
            oldTestIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (oldTestPendingIntent != null) {
            alarmManager.cancel(oldTestPendingIntent)
            oldTestPendingIntent.cancel()
            Log.d(TAG, "Cancelled PREVIOUS test notification (request code $TEST_NOTIFICATION_REQUEST_CODE)")
        }

        // Prepare new test notification
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
            TEST_NOTIFICATION_REQUEST_CODE, // Using the same request code to ensure it's "updated" or "replaced"
            TEST_NOTIFICATION_REQUEST_CODE, // Re-using the same request code is fine, it will update due to FLAG_UPDATE_CURRENT
            newTestIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check for S+ exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms. App needs SCHEDULE_EXACT_ALARM and user must grant it via settings for this test notification.")
                Toast.makeText(context, "無法排程此測試通知的精確時間，請在應用程式設定中允許「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                // Try to take user to settings
                try {
                    Log.d(TAG, "Attempting to open ACTION_REQUEST_SCHEDULE_EXACT_ALARM settings.")
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
                            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e2: Exception) {
                         Log.e(TAG, "Failed to open ACTION_APPLICATION_DETAILS_SETTINGS.", e2)
                         Toast.makeText(context, "請手動前往應用程式設定頁面授予「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                    }
                }
                return // Stop if permission is not granted and settings are attempted
            }
        }
        
        val calendar = Calendar.getInstance().apply {
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
                newTestPendingIntent
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

        // Cancel regular notifications
        val allPossibleDays = SetupConstants.DEFAULT_NOTIFICATION_DAYS.union(
            // Include all possible days to ensure any old/stray alarms are caught
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
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Important: FLAG_NO_CREATE
                )
                if (pendingIntent != null) { // Only cancel if it actually exists
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel() // Also cancel the PendingIntent itself
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "Cancelled repeating notification schedule for request code $pendingIntentRequestCode")
                }
            }
        }

        // Cancel any active test notification
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
            else -> -1 // Invalid day
        }
    }
}
       