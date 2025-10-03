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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fatefulsupper.app.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView

    private val navArgs: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        usernameEditText = view.findViewById(R.id.editText_username_email)
        passwordEditText = view.findViewById(R.id.editText_password)
        loginButton = view.findViewById(R.id.button_login_submit)
        errorTextView = view.findViewById(R.id.textView_login_error)
        val buttonGoToRegister = view.findViewById<Button>(R.id.button_go_to_register_from_login)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            viewModel.login(username, password)
        }

        buttonGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        observeViewModel()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navArgs.showRegistrationSuccessSnackbar) {
            // Check if this specific snackbar has already been shown for this navArgs instance
            val snackbarShownKey = "registration_snackbar_shown_${navArgs.hashCode()}"
            if (findNavController().currentBackStackEntry?.savedStateHandle?.get<Boolean>(snackbarShownKey) != true) {
                viewModel.onNavigatedFromRegistrationSuccess()
                findNavController().currentBackStackEntry?.savedStateHandle?.set(snackbarShownKey, true)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loginButton.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(viewLifecycleOwner) { loginSuccess ->
            if (loginSuccess) {
                // Snackbar for login success is now triggered from ViewModel
                // We will navigate after the snackbar has had a chance to be seen (or use its callback)
                // For simplicity now, we can add a small delay before navigation.
                lifecycleScope.launch {
                    delay(1000) // Adjust delay as needed, or use Snackbar callback
                    if(isAdded) { // Ensure fragment is still added
                        com.fatefulsupper.app.util.NotificationScheduler.checkLocationServices(requireActivity())
                        findNavController().navigate(R.id.action_loginFragment_to_lazyModeFragment)
                        viewModel.onLoginAttemptComplete() // Reset LiveData
                    }
                }
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { errorMessage ->
            errorTextView.text = errorMessage
            errorTextView.isVisible = errorMessage != null
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isLoading.value == false) {
            errorTextView.isVisible = false
            // Keep loginResult and snackbarMessage as is, they are handled by Event or specific flags
        }
    }
}
