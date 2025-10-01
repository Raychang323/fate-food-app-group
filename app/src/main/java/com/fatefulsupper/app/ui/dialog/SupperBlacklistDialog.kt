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
import android.util.Log // 導入 Log 類

class SupperBlacklistDialog : DialogFragment() {

    interface SupperBlacklistDialogListener {
        fun onBlacklistSettingsSaved(selectedCategoryIds: List<Int>) // 修改這裡，接收 List<Int>
        fun onBlacklistSettingsSaved()
        fun onBlacklistSetupSkipped()
    }

    private var listener: SupperBlacklistDialogListener? = null
    private var isFirstTimeSetup: Boolean = true // To store the mode

    private lateinit var checkboxContainer: LinearLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonSkip: Button // XML ID is button_back_blacklist

    private lateinit var sharedPreferences: SharedPreferences
    private val dynamicallyCreatedCheckBoxes = mutableMapOf<String, CheckBox>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Priority: Target Fragment, then Parent Fragment, then Activity
        if (targetFragment is SupperBlacklistDialogListener) {
            listener = targetFragment as SupperBlacklistDialogListener
        } else if (parentFragment is SupperBlacklistDialogListener) {
            listener = parentFragment as SupperBlacklistDialogListener
        } else if (context is SupperBlacklistDialogListener) {
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
        return inflater.inflate(R.layout.dialog_supper_blacklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkboxContainer = view.findViewById(R.id.checkbox_container_blacklist)
        buttonSave = view.findViewById(R.id.button_save_blacklist)
        buttonSkip = view.findViewById(R.id.button_back_blacklist) // Matches XML ID

        populateCheckBoxes()
        loadCurrentBlacklist()

        // Adjust button texts and behavior based on the mode
        if (isFirstTimeSetup) {
            buttonSave.text = getString(R.string.button_save_and_finish)
            buttonSkip.text = getString(R.string.button_skip_step)
        } else {
            buttonSave.text = getString(R.string.button_save)
            buttonSkip.text = getString(R.string.button_cancel)
        }

        buttonSave.setOnClickListener {
            val selectedCategoryIds = saveBlacklist() // 保存並獲取選中的 ID 清單
            if (isFirstTimeSetup) { // Only mark as fully completed if it's the first time setup
                markSetupAsFullyCompleted()
            }
            listener?.onBlacklistSettingsSaved(selectedCategoryIds) // 傳遞選中的 ID 清單
            saveBlacklist()
            if (isFirstTimeSetup) { // Only mark as fully completed if it's the first time setup
                markSetupAsFullyCompleted()
            }
            listener?.onBlacklistSettingsSaved()
            dismiss()
        }

        buttonSkip.setOnClickListener {
            if (isFirstTimeSetup) { // Only mark as fully completed if it's the first time setup
                markSetupAsFullyCompleted()
            }
            listener?.onBlacklistSetupSkipped()
            dismiss()
        }
    }

    private fun populateCheckBoxes() {
        checkboxContainer.removeAllViews()
        dynamicallyCreatedCheckBoxes.clear()

        // TODO: 目前這裡使用 SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS，
        // 這個 Map 的 value 是 String (typeKey)，我們需要一個方法將 typeKey 轉換為 categoryId (Int)。
        // 暫時假定 typeKey 可以直接解析為 Int，或者你需要提供 typeKey 到 Int 的映射關係。
        // 如果 SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS 能夠直接提供 Int 類型的 ID 會更好。

        SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS.forEach { (displayName, typeKey) ->
            val checkBox = CheckBox(context).apply {
                text = displayName
                tag = typeKey // 將 typeKey 設置為 tag
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

    private fun saveBlacklist(): List<Int> { // 返回選中的 Int ID 清單
        val selectedBlacklistedTypeKeys = mutableSetOf<String>()
        val selectedCategoryIds = mutableListOf<Int>() // 用於儲存 Int 類型的 ID

        dynamicallyCreatedCheckBoxes.forEach { (typeKey, checkBox) ->
            if (checkBox.isChecked) {
                selectedBlacklistedTypeKeys.add(typeKey)
                // TODO: 將 typeKey 轉換為 Int 類型的 categoryId
                // 這裡需要根據你的 SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS 的實際結構來實現轉換邏輯。
                // 暫時假設 typeKey 可以直接解析為 Int。如果不是，你需要提供一個映射。
                try {
                    val categoryId = typeKey.toInt()
                    selectedCategoryIds.add(categoryId)
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Invalid category ID format for typeKey: $typeKey", e)
                    // 處理轉換失敗的情況，例如跳過或記錄錯誤
                }
            }
        }

        with(sharedPreferences.edit()) {
            putStringSet(SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, selectedBlacklistedTypeKeys)
            apply()
        }
        return selectedCategoryIds
    private fun saveBlacklist() {
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
    }

    private fun markSetupAsFullyCompleted() {
        // This should only be called during the first-time setup.
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

        // Updated newInstance to accept the mode
        fun newInstance(isFirstTimeSetup: Boolean = true): SupperBlacklistDialog {
            val args = Bundle().apply {
                putBoolean(ARG_IS_FIRST_TIME_SETUP, isFirstTimeSetup)
            }
            return SupperBlacklistDialog().apply {
                arguments = args
            }
        }
    }
}