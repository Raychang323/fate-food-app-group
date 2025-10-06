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

    private val userid: String by lazy {
        arguments?.getString(ARG_USERID)
            ?: throw IllegalArgumentException("userid is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_verification, container, false)

        // Initialize Views
        codeEditText = view.findViewById(R.id.editText_verification_code)
        verifyButton = view.findViewById(R.id.button_verify_code_submit)
        resendButton = view.findViewById(R.id.button_resend_verification_code)
        errorTextView = view.findViewById(R.id.textView_verification_error)

        // Initialize ViewModel with the factory
        val factory = EmailVerificationViewModelFactory(userid)
        viewModel = ViewModelProvider(this, factory).get(EmailVerificationViewModel::class.java)

        // Setup UI
        setupListeners()
        observeViewModel()

        return view
    }

    private fun setupListeners() {
        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isNotEmpty()) {
                viewModel.verifyCode(code)
            } else {
                Toast.makeText(context, "請輸入驗證碼", Toast.LENGTH_SHORT).show()
            }
        }

        resendButton.setOnClickListener {
            viewModel.resendVerificationCode()
            Toast.makeText(context, "驗證碼已重新寄送 (模擬)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            verifyButton.isEnabled = !isLoading
        }

        viewModel.verificationResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "驗證成功！", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_email_verified_to_login)
                viewModel.onAttemptComplete()
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
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            viewModel.onAttemptComplete()
        }
    }

    companion object {
        private const val ARG_USERID = "userid"

        fun newInstance(userid: String): EmailVerificationFragment {
            val fragment = EmailVerificationFragment()
            val args = Bundle().apply {
                putString(ARG_USERID, userid)
            }
            fragment.arguments = args
            return fragment
        }
    }
}