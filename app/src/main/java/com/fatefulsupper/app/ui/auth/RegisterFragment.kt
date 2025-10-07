package com.fatefulsupper.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    private lateinit var viewModel: RegisterViewModel

    private lateinit var editTextUserid: TextInputEditText
    private lateinit var editTextUsername: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // View binding
        editTextUserid = view.findViewById(R.id.editText_userid_register)
        editTextPassword = view.findViewById(R.id.editText_password_register)
        editTextEmail = view.findViewById(R.id.editText_email_register)
        editTextUsername = view.findViewById(R.id.editText_name_register)
        confirmPasswordEditText = view.findViewById(R.id.editText_confirm_password_register)
        registerButton = view.findViewById(R.id.button_register_submit)
        errorTextView = view.findViewById(R.id.textView_register_error)

        setupObservers()
        setupClickListener()

        return view
    }

    private fun setupClickListener() {
        registerButton.setOnClickListener {
            if (viewModel.isLoading.value == true) return@setOnClickListener

            val userid = editTextUserid.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (!validateForm(userid, username, email, password, confirmPassword)) return@setOnClickListener

            viewModel.register(email, password, username, userid)
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
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            registerButton.isEnabled = !isLoading
        }

        viewModel.registrationSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        viewModel.registrationError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { showError(it) }
        }
    }
}
