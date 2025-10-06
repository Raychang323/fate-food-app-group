package com.fatefulsupper.app.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fatefulsupper.app.R
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog
import com.fatefulsupper.app.ui.dialog.SupperBlacklistDialog

class SettingsFragment : PreferenceFragmentCompat(),
    NotificationSettingsDialog.NotificationDialogListener,
    SupperBlacklistDialog.SupperBlacklistDialogListener {

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        val notificationSettingsPref: Preference? = findPreference("pref_notification_settings")
        notificationSettingsPref?.setOnPreferenceClickListener {
            val dialog = NotificationSettingsDialog.newInstance(isFirstTimeSetup = false)
            dialog.show(parentFragmentManager, NotificationSettingsDialog.TAG)
            true
        }

        val supperBlacklistPref: Preference? = findPreference("pref_supper_blacklist")
        supperBlacklistPref?.setOnPreferenceClickListener {
            val blacklistDialog = SupperBlacklistDialog.newInstance(isFirstTimeSetup = false)
            blacklistDialog.show(parentFragmentManager, SupperBlacklistDialog.TAG)
            true
        }
    }

    override fun onNotificationSettingsSaved(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification settings saved. Was first time setup: $isFirstTimeSetupContext")
    }

    override fun onNotificationSetupSkipped(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification setup skipped. Was first time setup: $isFirstTimeSetupContext")
    }

    override fun onBlacklistSettingsSaved(blacklistedIds: Set<String>) {
        Log.d(TAG, "Supper blacklist settings saved with IDs: $blacklistedIds")
        val userId = "test_user_id" // TODO: Replace with actual user ID
        settingsViewModel.updateNightSnackBlacklist(userId, blacklistedIds)
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped/cancelled.")
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}