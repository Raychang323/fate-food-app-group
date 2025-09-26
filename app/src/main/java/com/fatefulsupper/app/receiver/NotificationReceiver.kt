package com.fatefulsupper.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fatefulsupper.app.util.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver" // Added for consistency
    }

    override fun onReceive(context: Context, intent: Intent) {
        // --- MODIFIED HERE ---
        Log.e(TAG, "NOTIFICATION TRIGGER RECEIVED! Intent Action: ${intent.action}")
        Log.d(TAG, "Notification trigger received. Showing notification.")
        // --- END MODIFICATION ---
        NotificationHelper.showNotification(context)
    }
}