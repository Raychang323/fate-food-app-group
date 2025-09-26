package com.fatefulsupper.app.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.fatefulsupper.app.R
import com.fatefulsupper.app.util.NotificationScheduler // Added import
import com.fatefulsupper.app.util.SetupConstants

class NotificationSettingsDialog : DialogFragment() {

    interface NotificationDialogListener {
        fun onNotificationSettingsSaved()
        fun onNotificationSetupSkipped()
    }

    private var listener: NotificationDialogListener? = null
    private var isFirstTimeSetup: Boolean = true

    private lateinit var checkboxMonday: CheckBox
    // ... (other checkboxes)
    private lateinit var checkboxTuesday: CheckBox
    private lateinit var checkboxWednesday: CheckBox
    private lateinit var checkboxThursday: CheckBox
    private lateinit var checkboxFriday: CheckBox
    private lateinit var checkboxSaturday: CheckBox
    private lateinit var checkboxSunday: CheckBox
    private lateinit var timePicker: TimePicker
    private lateinit var buttonSaveNext: Button
    private lateinit var buttonSkipCancel: Button

    private lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (targetFragment is NotificationDialogListener) {
            listener = targetFragment as NotificationDialogListener
        } else if (parentFragment is NotificationDialogListener) {
            listener = parentFragment as NotificationDialogListener
        } else if (context is NotificationDialogListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        sharedPreferences = requireActivity().getSharedPreferences(
            SetupConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        isFirstTimeSetup = arguments?.getBoolean(ARG_IS_FIRST_TIME_SETUP, true) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_notification_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkboxMonday = view.findViewById(R.id.checkbox_monday)
        checkboxTuesday = view.findViewById(R.id.checkbox_tuesday)
        checkboxWednesday = view.findViewById(R.id.checkbox_wednesday)
        checkboxThursday = view.findViewById(R.id.checkbox_thursday)
        checkboxFriday = view.findViewById(R.id.checkbox_friday)
        checkboxSaturday = view.findViewById(R.id.checkbox_saturday)
        checkboxSunday = view.findViewById(R.id.checkbox_sunday)
        timePicker = view.findViewById(R.id.time_picker_notification)
        buttonSaveNext = view.findViewById(R.id.button_next_notification)
        buttonSkipCancel = view.findViewById(R.id.button_skip_notification)

        timePicker.setIs24HourView(true)
        loadCurrentSettings()

        if (!isFirstTimeSetup) {
            buttonSaveNext.text = getString(R.string.button_save)
            buttonSkipCancel.text = getString(R.string.button_cancel)
        } else {
            buttonSaveNext.text = getString(R.string.button_next)
            buttonSkipCancel.text = getString(R.string.button_skip)
        }

        buttonSaveNext.setOnClickListener {
            saveSettings()
            NotificationScheduler.scheduleNotifications(requireContext()) // Reschedule
            listener?.onNotificationSettingsSaved()
            dismiss()
        }

        buttonSkipCancel.setOnClickListener {
            // Even if skipped/cancelled, ensure the scheduler reflects the current state
            // (e.g., if user unselected all days and then skipped, alarms should be cancelled)
            // saveSettings() is not called here, so it uses the settings *before* this dialog was shown,
            // or the last saved state if coming from settings.
            // A more robust approach might be to only schedule if settings *were* saved,
            // or to explicitly pass a "shouldSave" flag.
            // For now, scheduling based on current SharedPreferences is reasonable.
            // If the user "skips" first-time setup, default settings (which might be "no days") are used.
            NotificationScheduler.scheduleNotifications(requireContext()) // Reschedule
            listener?.onNotificationSetupSkipped()
            dismiss()
        }
    }

    private fun loadCurrentSettings() {
        val savedDays = sharedPreferences.getStringSet(
            SetupConstants.KEY_NOTIFICATION_DAYS,
            SetupConstants.DEFAULT_NOTIFICATION_DAYS
        ) ?: SetupConstants.DEFAULT_NOTIFICATION_DAYS

        checkboxMonday.isChecked = "MONDAY" in savedDays
        checkboxTuesday.isChecked = "TUESDAY" in savedDays
        checkboxWednesday.isChecked = "WEDNESDAY" in savedDays
        checkboxThursday.isChecked = "THURSDAY" in savedDays
        checkboxFriday.isChecked = "FRIDAY" in savedDays
        checkboxSaturday.isChecked = "SATURDAY" in savedDays
        checkboxSunday.isChecked = "SUNDAY" in savedDays

        val savedHour = sharedPreferences.getInt(
            SetupConstants.KEY_NOTIFICATION_HOUR,
            SetupConstants.DEFAULT_NOTIFICATION_HOUR
        )
        val savedMinute = sharedPreferences.getInt(
            SetupConstants.KEY_NOTIFICATION_MINUTE,
            SetupConstants.DEFAULT_NOTIFICATION_MINUTE
        )

        timePicker.hour = savedHour
        timePicker.minute = savedMinute
    }

    private fun saveSettings() {
        val selectedDays = mutableSetOf<String>()
        if (checkboxMonday.isChecked) selectedDays.add("MONDAY")
        if (checkboxTuesday.isChecked) selectedDays.add("TUESDAY")
        if (checkboxWednesday.isChecked) selectedDays.add("WEDNESDAY")
        if (checkboxThursday.isChecked) selectedDays.add("THURSDAY")
        if (checkboxFriday.isChecked) selectedDays.add("FRIDAY")
        if (checkboxSaturday.isChecked) selectedDays.add("SATURDAY")
        if (checkboxSunday.isChecked) selectedDays.add("SUNDAY")

        with(sharedPreferences.edit()) {
            putStringSet(SetupConstants.KEY_NOTIFICATION_DAYS, selectedDays)
            putInt(SetupConstants.KEY_NOTIFICATION_HOUR, timePicker.hour)
            putInt(SetupConstants.KEY_NOTIFICATION_MINUTE, timePicker.minute)
            apply()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        const val TAG = "NotificationSettingsDialog"
        private const val ARG_IS_FIRST_TIME_SETUP = "isFirstTimeSetup"

        fun newInstance(isFirstTimeSetup: Boolean = true): NotificationSettingsDialog {
            val args = Bundle().apply {
                putBoolean(ARG_IS_FIRST_TIME_SETUP, isFirstTimeSetup)
            }
            return NotificationSettingsDialog().apply {
                arguments = args
            }
        }
    }
}