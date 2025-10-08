package com.fatefulsupper.app.ui.settings

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fatefulsupper.app.R
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog
import com.fatefulsupper.app.ui.dialog.SupperBlacklistDialog
import com.fatefulsupper.app.util.TokenManager

class SettingsFragment : PreferenceFragmentCompat(),
    NotificationSettingsDialog.NotificationDialogListener,
    SupperBlacklistDialog.SupperBlacklistDialogListener {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        tokenManager = TokenManager(requireContext())

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

    override fun onBlacklistSettingsSaved(blacklistedIds: Set<Int>) { // Changed type to Set<Int>
        Log.d(TAG, "Supper blacklist settings saved with IDs: $blacklistedIds")
        val userId = tokenManager.getUserId()
        if (userId == null) {
            Log.e(TAG, "User ID not found when saving blacklist settings.")
            Toast.makeText(requireContext(), "User not logged in. Cannot save blacklist.", Toast.LENGTH_SHORT).show()
            return
        }
        settingsViewModel.updateNightSnackBlacklist(userId, blacklistedIds)
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped/cancelled.")
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}