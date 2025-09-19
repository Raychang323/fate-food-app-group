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
    }

    override fun onPause() {
        super.onPause()
        // Clear error when navigating away or fragment is paused, unless a registration is in progress
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            viewModel.onRegistrationAttemptComplete() // also clears the error in ViewModel
        }
    }
}
