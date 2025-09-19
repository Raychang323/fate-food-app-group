package com.fatefulsupper.app.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R

class ConnoisseurCheckFragment : Fragment() {

    private lateinit var viewModel: ConnoisseurCheckViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connoisseur_check, container, false)
        // Corrected line for ViewModel initialization:
        viewModel = ViewModelProvider(this).get(ConnoisseurCheckViewModel::class.java)

        val buttonToLogin = view.findViewById<Button>(R.id.button_to_login)
        val buttonToRegister = view.findViewById<Button>(R.id.button_to_register)
        val buttonToLazyMode = view.findViewById<Button>(R.id.button_to_lazy_mode)
        val buttonToLuckyMeal = view.findViewById<Button>(R.id.button_to_lucky_meal)

        buttonToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_loginFragment)
        }

        buttonToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_registerFragment)
        }

        buttonToLazyMode.setOnClickListener {
            findNavController().navigate(R.id.action_connoisseurCheckFragment_to_lazyModeFragment)
        }

        buttonToLuckyMeal.setOnClickListener {
             findNavController().navigate(R.id.action_connoisseurCheckFragment_to_luckyMealFragment)
        }

        return view
    }
}
