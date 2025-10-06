package com.fatefulsupper.app.util

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException while scheduling notification: ${e.message}")
            }
        }
    }

    fun cancelScheduledNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d(TAG, "Cancelling all scheduled notifications.")

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
                }
            }
        }

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
        }
    }

    fun scheduleTestNotification(context: Context, triggerDelaySeconds: Int = 30) {
        Log.d(TAG, "Attempting to schedule a SINGLE TEST NOTIFICATION to trigger in $triggerDelaySeconds seconds.")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val newTestIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.fatefulsupper.app.TEST_NOTIFICATION"
            putExtra("test_message", "This is a TEST NOTIFICATION!")
        }

        val newTestPendingIntent = PendingIntent.getBroadcast(
            context,
            TEST_NOTIFICATION_REQUEST_CODE,
            newTestIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "無法排程此測試通知的精確時間，請在應用程式設定中允許「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                try {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: Exception) {
                    try {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e2: Exception) {
                         Toast.makeText(context, "請手動前往應用程式設定頁面授予「鬧鐘與提醒」權限。", Toast.LENGTH_LONG).show()
                    }
                }
                return
            }
        }
        
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, triggerDelaySeconds)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                newTestPendingIntent
            )
            Toast.makeText(context, "已設定測試通知，將於 $triggerDelaySeconds 秒後觸發", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "設定測試通知時發生安全性錯誤", Toast.LENGTH_LONG).show()
        }
    }

    fun checkLocationServices(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null, cannot check location services.")
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
                return
            }

            if (context is Activity) {
                AlertDialog.Builder(context)
                    .setTitle("允許「Fateful Supper」存取您的位置嗎？")
                    .setMessage("為了協助您搜尋附近的餐廳，「Fateful Supper」需要存取您的位置資訊。建議開啟定位服務以獲得更精準的搜尋結果。")
                    .setPositiveButton("是，前往設定") { _, _ ->
                        prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, false).apply()
                        try {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "請手動前往系統設定開啟定位服務。", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("否，暫不開啟") { _, _ ->
                        prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, true).apply()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                if (!userPreviouslyDenied) {
                    Toast.makeText(context, "為了搜尋附近餐廳，請開啟定位服務。", Toast.LENGTH_LONG).show()
                    try {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "請手動前往系統設定開啟定位服務。", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            prefs.edit().putBoolean(PREF_KEY_USER_DENIED_LOCATION_PROMPT, false).apply()
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