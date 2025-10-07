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
import com.fatefulsupper.app.MainActivity
import com.fatefulsupper.app.R
import com.fatefulsupper.app.util.NotificationScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var useridEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        useridEditText = view.findViewById(R.id.editText_userid)
        passwordEditText = view.findViewById(R.id.editText_password)
        loginButton = view.findViewById(R.id.button_login_submit)
        errorTextView = view.findViewById(R.id.textView_login_error)
        val buttonGoToRegister = view.findViewById<Button>(R.id.button_go_to_register_from_login)

        loginButton.setOnClickListener {
            val userid = useridEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            viewModel.login(userid, password)
        }

        buttonGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loginButton.isEnabled = !isLoading
        }

        viewModel.loginSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { (userId, token) ->
                (activity as? MainActivity)?.handleLoginSuccess(userId, token)
                Snackbar.make(requireView(), "登入成功", Snackbar.LENGTH_LONG).show()
                NotificationScheduler.checkLocationServices(requireActivity())
                findNavController().navigate(R.id.action_loginFragment_to_lazyModeFragment)
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                errorTextView.text = errorMessage
                errorTextView.isVisible = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        errorTextView.isVisible = false
    }
}
