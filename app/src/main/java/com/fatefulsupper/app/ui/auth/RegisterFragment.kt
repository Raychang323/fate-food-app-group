package com.fatefulsupper.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        usernameEditText = view.findViewById(R.id.editText_username_register)
        emailEditText = view.findViewById(R.id.editText_email_register)
        passwordEditText = view.findViewById(R.id.editText_password_register)
        confirmPasswordEditText = view.findViewById(R.id.editText_confirm_password_register)
        registerButton = view.findViewById(R.id.button_register_submit)
        errorTextView = view.findViewById(R.id.textView_register_error)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            viewModel.register(username, email, password, confirmPassword)
        }

        observeViewModel()
    private lateinit var editTextUserId: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextUsername: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var spinnerRole: Spinner
    private lateinit var registerButton: Button
    private lateinit var errorTextView: TextView

    private val roles = arrayOf("general", "merchant")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // View binding
        editTextUserId = view.findViewById(R.id.editText_userid_register)
        editTextPassword = view.findViewById(R.id.editText_password_register)
        editTextEmail = view.findViewById(R.id.editText_email_register)
        editTextUsername = view.findViewById(R.id.editText_username_register)
        confirmPasswordEditText = view.findViewById(R.id.editText_confirm_password_register)
        spinnerRole = view.findViewById(R.id.autoCompleteRole)
        registerButton = view.findViewById(R.id.button_register_submit)
        errorTextView = view.findViewById(R.id.textView_register_error)

        setupSpinner()
        setupObservers()
        setupClickListener()

        return view
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            registerButton.isEnabled = !isLoading
            // You could also show/hide a ProgressBar here
        }

        viewModel.registrationStepResult.observe(viewLifecycleOwner) { registrationSuccess ->
            if (registrationSuccess) {
                findNavController().navigate(R.id.action_registerFragment_to_emailVerificationFragment)
                viewModel.onRegistrationAttemptComplete() // Reset LiveData
            }
        }

        viewModel.registrationError.observe(viewLifecycleOwner) { errorMessage ->
            errorTextView.text = errorMessage
            errorTextView.isVisible = errorMessage != null
        }
    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    private fun setupClickListener() {
        registerButton.setOnClickListener {
            if (viewModel.isLoading.value == true) return@setOnClickListener

            val userid = editTextUserId.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val role = spinnerRole.selectedItem.toString()

            if (!validateForm(userid, username, email, password, confirmPassword)) return@setOnClickListener

            viewModel.register(userid, password, email, username, role)
        }
    }

    private fun validateForm(
        userid: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            userid.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                showError("請填寫所有欄位")
                false
            }
            password != confirmPassword -> {
                showError("密碼與確認密碼不一致")
                false
            }
            else -> {
                errorTextView.isVisible = false
                true
            }
        }
    }

    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.isVisible = true
    }

    private fun setupObservers() {
        // 按鈕防重複點擊
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            registerButton.isEnabled = !isLoading
        })

        // 註冊成功單次導航
        viewModel.registrationStepResult.observe(viewLifecycleOwner, Observer { success ->
            success?.let {
                if (it) {
                    // 取得 userid
                    val userid = editTextUserId.text.toString().trim()

                    // 準備 bundle 傳給 EmailVerificationFragment
                    val bundle = Bundle().apply {
                        putString("userid", userid)
                    }

                    findNavController().navigate(
                        R.id.action_registerFragment_to_emailVerificationFragment,
                        bundle
                    )

                    viewModel.onRegistrationAttemptComplete()
                }
            }
        })

        // 錯誤訊息單次顯示
        viewModel.registrationError.observe(viewLifecycleOwner, Observer { error ->
            error?.let { showError(it) }
        })
    }

    override fun onPause() {
        super.onPause()
        // Clear error when navigating away or fragment is paused, unless a registration is in progress
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            viewModel.onRegistrationAttemptComplete() // also clears the error in ViewModel
        // 生命週期安全清理
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            viewModel.onRegistrationAttemptComplete()
        }
    }
}
