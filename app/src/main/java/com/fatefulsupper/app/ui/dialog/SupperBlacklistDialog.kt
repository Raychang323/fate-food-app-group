package com.fatefulsupper.app.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.fatefulsupper.app.R
import com.fatefulsupper.app.util.SetupConstants

class SupperBlacklistDialog : DialogFragment() {

    interface SupperBlacklistDialogListener {
        fun onBlacklistSettingsSaved(blacklistedTypes: Set<String>)
        fun onBlacklistSetupSkipped()
    }

    private var listener: SupperBlacklistDialogListener? = null
    private var isFirstTimeSetup: Boolean = true

    private lateinit var checkboxContainer: LinearLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonSkip: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val dynamicallyCreatedCheckBoxes = mutableMapOf<String, CheckBox>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? SupperBlacklistDialogListener ?: context as? SupperBlacklistDialogListener
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
        return inflater.inflate(R.layout.dialog_supper_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkboxContainer = view.findViewById(R.id.checkbox_container_blacklist)
        buttonSave = view.findViewById(R.id.button_save_blacklist)
        buttonSkip = view.findViewById(R.id.button_back_blacklist)

        populateCheckBoxes()
        loadCurrentBlacklist()

        buttonSave.text = if (isFirstTimeSetup) getString(R.string.button_save_and_finish) else getString(R.string.button_save)
        buttonSkip.text = if (isFirstTimeSetup) getString(R.string.button_skip_step) else getString(R.string.button_cancel)

        buttonSave.setOnClickListener {
            val selectedTypes = saveBlacklist()
            if (isFirstTimeSetup) {
                markSetupAsFullyCompleted()
            }
            listener?.onBlacklistSettingsSaved(selectedTypes)
            dismiss()
        }

        buttonSkip.setOnClickListener {
            if (isFirstTimeSetup) {
                markSetupAsFullyCompleted()
            }
            listener?.onBlacklistSetupSkipped()
            dismiss()
        }
    }

    private fun populateCheckBoxes() {
        checkboxContainer.removeAllViews()
        dynamicallyCreatedCheckBoxes.clear()

        SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS.forEach { (displayName, typeKey) ->
            val checkBox = CheckBox(context).apply {
                text = displayName
                tag = typeKey
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            checkboxContainer.addView(checkBox)
            dynamicallyCreatedCheckBoxes[typeKey] = checkBox
        }
    }

    private fun loadCurrentBlacklist() {
        val blacklistedTypes = sharedPreferences.getStringSet(
            SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, emptySet()
        ) ?: emptySet()

        blacklistedTypes.forEach { typeKey ->
            dynamicallyCreatedCheckBoxes[typeKey]?.isChecked = true
        }
    }

    private fun saveBlacklist(): Set<String> {
        val selectedBlacklistedTypes = mutableSetOf<String>()
        dynamicallyCreatedCheckBoxes.forEach { (typeKey, checkBox) ->
            if (checkBox.isChecked) {
                selectedBlacklistedTypes.add(typeKey)
            }
        }
        with(sharedPreferences.edit()) {
            putStringSet(SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, selectedBlacklistedTypes)
            apply()
        }
        return selectedBlacklistedTypes
    }

    private fun markSetupAsFullyCompleted() {
        with(sharedPreferences.edit()) {
            putBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, true)
            apply()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        const val TAG = "SupperBlacklistDialog"
        private const val ARG_IS_FIRST_TIME_SETUP = "isFirstTimeSetup"

        fun newInstance(isFirstTimeSetup: Boolean = true): SupperBlacklistDialog {
            return SupperBlacklistDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_FIRST_TIME_SETUP, isFirstTimeSetup)
                }
            }
        }
    }
}