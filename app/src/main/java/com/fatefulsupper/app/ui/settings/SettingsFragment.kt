package com.fatefulsupper.app.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.fatefulsupper.app.R
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Find the notification settings preference
        val notificationSettingsPref: Preference? = findPreference("pref_notification_settings")
        notificationSettingsPref?.setOnPreferenceClickListener {
            // Show the NotificationSettingsDialog
            // Pass 'isFirstTimeSetup = false' as this is from general settings
            val dialog = NotificationSettingsDialog.newInstance(isFirstTimeSetup = false)
            dialog.show(parentFragmentManager, NotificationSettingsDialog.TAG)
            true
        }

        // TODO: Add listeners for other preferences if any (e.g., supper blacklist)
        val supperBlacklistPref: Preference? = findPreference("pref_supper_blacklist")
        supperBlacklistPref?.setOnPreferenceClickListener {
            // TODO: Implement showing SupperBlacklistDialog,
            // similar to NotificationSettingsDialog but make sure to pass isFirstTimeSetup = false
            // val blacklistDialog = SupperBlacklistDialog.newInstance(isFirstTimeSetup = false)
            // blacklistDialog.show(parentFragmentManager, SupperBlacklistDialog.TAG)
            // For now, let's just log or show a Toast
            android.widget.Toast.makeText(requireContext(), "黑名單設定待實作", android.widget.Toast.LENGTH_SHORT).show()
            true
        }
    }
}