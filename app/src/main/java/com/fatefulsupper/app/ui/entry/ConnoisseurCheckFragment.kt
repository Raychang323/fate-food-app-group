package com.fatefulsupper.app.ui.entry

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.BuildConfig
import com.fatefulsupper.app.R
import com.fatefulsupper.app.databinding.FragmentConnoisseurCheckBinding // Import ViewBinding
import com.fatefulsupper.app.ui.auth.LoginViewModel
import com.fatefulsupper.app.util.NotificationScheduler

class ConnoisseurCheckFragment : Fragment() {

    private var _binding: FragmentConnoisseurCheckBinding? = null // ViewBinding property
    private val binding get() = _binding!! // Non-null accessor

    private lateinit var viewModel: ConnoisseurCheckViewModel
    private val loginViewModel: LoginViewModel by activityViewModels()

    companion object {
        private const val TAG = "ConnoisseurCheckFrag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnoisseurCheckBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ConnoisseurCheckViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_loginFragment)
        }

        binding.buttonLogout.setOnClickListener {
            loginViewModel.logout()
        }

        loginViewModel.logoutSuccess.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { 
                Toast.makeText(requireContext(), "登出成功", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_registerFragment)
        }

        binding.buttonToFoodieMode.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_foodieModeFragment)
        }

        binding.buttonToLuckyMeal.setOnClickListener {
             findNavController().navigate(R.id.action_connoisseurCheckFragment_to_luckyMealFragment)
        }

        binding.buttonToGuestMode.setOnClickListener {
            Log.d(TAG, "Guest Mode button clicked. Navigating to LoadingFragment.")
            val action = ConnoisseurCheckFragmentDirections.actionConnoisseurCheckFragmentToLoadingFragment("guestMode")
            findNavController().navigate(action)
        }

        // Logic for Test Notification Button
        if (BuildConfig.DEBUG) {
            binding.buttonTestNotification.visibility = View.VISIBLE
            binding.buttonTestNotification.setOnClickListener {
                Log.d(TAG, "Test Notification button clicked. Scheduling test notification.")
                NotificationScheduler.scheduleTestNotification(requireContext(), 15)
                Toast.makeText(requireContext(), getString(R.string.test_notification_scheduled_message), Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.buttonTestNotification.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding to avoid memory leaks
    }
}
