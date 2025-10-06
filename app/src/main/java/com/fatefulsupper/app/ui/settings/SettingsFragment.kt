package com.fatefulsupper.app.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.lifecycle.ViewModelProvider // 新增 ViewModelProvider 導入
import com.fatefulsupper.app.R
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog
import com.fatefulsupper.app.ui.dialog.SupperBlacklistDialog

class SettingsFragment : PreferenceFragmentCompat(),
    NotificationSettingsDialog.NotificationDialogListener,
    SupperBlacklistDialog.SupperBlacklistDialogListener {

    companion object {
        private const val TAG = "SettingsFragment"
    }

    private lateinit var settingsViewModel: SettingsViewModel // 實例化 ViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java] // 初始化 ViewModel

        // Notification Settings Preference
        val notificationSettingsPref: Preference? = findPreference("pref_notification_settings")
        notificationSettingsPref?.setOnPreferenceClickListener {
            val dialog = NotificationSettingsDialog.newInstance(isFirstTimeSetup = false)
            dialog.show(parentFragmentManager, NotificationSettingsDialog.TAG)
            true
        }

        // Supper Blacklist Preference
        val supperBlacklistPref: Preference? = findPreference("pref_supper_blacklist")
        supperBlacklistPref?.setOnPreferenceClickListener {
            val blacklistDialog = SupperBlacklistDialog.newInstance(isFirstTimeSetup = false)
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
    // 修改這裡，接收 selectedCategoryIds 參數
    override fun onBlacklistSettingsSaved(selectedCategoryIds: List<Int>) {
        Log.d(TAG, "Supper blacklist settings saved with IDs: $selectedCategoryIds")
        // TODO: 從某處獲取實際的 userId。目前使用一個模擬值。
        val userId = "test_user_id" // 請替換為實際的用戶 ID
        settingsViewModel.updateNightSnackBlacklist(userId, selectedCategoryIds)
        // Handle post-save actions if needed
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped/cancelled.")
        // Handle post-skip/cancel actions if needed
    }
}
