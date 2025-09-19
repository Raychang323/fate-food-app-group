package com.fatefulsupper.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.google.android.material.textfield.TextInputEditText

class EmailVerificationFragment : Fragment() {

    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var codeEditText: TextInputEditText
    private lateinit var verifyButton: Button
    private lateinit var resendButton: Button
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_verification, container, false)
        viewModel = ViewModelProvider(this).get(EmailVerificationViewModel::class.java)

        codeEditText = view.findViewById(R.id.editText_verification_code)
        verifyButton = view.findViewById(R.id.button_verify_code_submit)
        resendButton = view.findViewById(R.id.button_resend_verification_code)
        errorTextView = view.findViewById(R.id.textView_verification_error)

        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            viewModel.verifyCode(code)
        }

        resendButton.setOnClickListener {
            viewModel.resendVerificationCode()
            // Optionally, provide immediate feedback that resend was triggered
            Toast.makeText(context, "驗證碼已重新寄送 (模擬)", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            verifyButton.isEnabled = !isLoading
            // codeEditText.isEnabled = !isLoading // Optionally disable input during verification
        }

        viewModel.verificationResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // Navigate to LoginFragment with argument to show Snackbar
                val action = EmailVerificationFragmentDirections.actionEmailVerifiedToLogin()
                findNavController().navigate(action)
                viewModel.onAttemptComplete() // Reset LiveData
            }
        }

        viewModel.verificationError.observe(viewLifecycleOwner) { errorMessage ->
            errorTextView.text = errorMessage
            errorTextView.isVisible = errorMessage != null
        }

        viewModel.resendCooldownActive.observe(viewLifecycleOwner) { isActive ->
            resendButton.isEnabled = !isActive
            if (!isActive) {
                resendButton.text = "重新寄送驗證碼"
            }
        }

        viewModel.resendCooldownSeconds.observe(viewLifecycleOwner) { seconds ->
            if (viewModel.resendCooldownActive.value == true && seconds > 0) {
                resendButton.text = "重新寄送 (${seconds}s)"
            } else if (viewModel.resendCooldownActive.value == false) {
                 resendButton.text = "重新寄送驗證碼"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Clear error when navigating away or fragment is paused, unless an operation is in progress
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            viewModel.onAttemptComplete()
        }
    }
}
