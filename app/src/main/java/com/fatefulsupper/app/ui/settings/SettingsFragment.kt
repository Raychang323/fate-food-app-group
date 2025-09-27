package com.fatefulsupper.app.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fatefulsupper.app.R
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog
import com.fatefulsupper.app.ui.dialog.SupperBlacklistDialog // Added import

class SettingsFragment : PreferenceFragmentCompat(),
    NotificationSettingsDialog.NotificationDialogListener, // Implemented listener
    SupperBlacklistDialog.SupperBlacklistDialogListener { // Implemented listener

    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Notification Settings Preference
        val notificationSettingsPref: Preference? = findPreference("pref_notification_settings")
        notificationSettingsPref?.setOnPreferenceClickListener {
            val dialog = NotificationSettingsDialog.newInstance(isFirstTimeSetup = false)
            // The dialog will find this fragment as a listener via parentFragmentManager
            dialog.show(parentFragmentManager, NotificationSettingsDialog.TAG)
            true
        }

        // Supper Blacklist Preference
        val supperBlacklistPref: Preference? = findPreference("pref_supper_blacklist")
        supperBlacklistPref?.setOnPreferenceClickListener {
            val blacklistDialog = SupperBlacklistDialog.newInstance(isFirstTimeSetup = false)
            // The dialog will find this fragment as a listener via parentFragmentManager
            blacklistDialog.show(parentFragmentManager, SupperBlacklistDialog.TAG)
            true
        }
    }

    // Implementation for NotificationDialogListener
    override fun onNotificationSettingsSaved(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification settings saved. Was first time setup: $isFirstTimeSetupContext")
        // Handle post-save actions if needed, e.g., update UI or show a confirmation
    }

    override fun onNotificationSetupSkipped(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification setup skipped. Was first time setup: $isFirstTimeSetupContext")
        // Handle post-skip actions if needed
    }

    // Implementation for SupperBlacklistDialogListener
    override fun onBlacklistSettingsSaved() {
        Log.d(TAG, "Supper blacklist settings saved.")
        // Handle post-save actions if needed
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped/cancelled.")
        // Handle post-skip/cancel actions if needed
    }
}