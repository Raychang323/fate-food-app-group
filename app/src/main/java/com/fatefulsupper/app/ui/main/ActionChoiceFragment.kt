package com.fatefulsupper.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant

class ActionChoiceFragment : Fragment() {

    private lateinit var viewModel: FoodieModeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_action_choice, container, false)

        viewModel = ViewModelProvider(requireActivity())[FoodieModeViewModel::class.java]

        val recommendationSummary: TextView = view.findViewById(R.id.textView_action_choice_prompt)
        val viewRecommendationsButton: Button = view.findViewById(R.id.button_go_to_restaurant_list_from_choice)
        val startRouletteButton: Button = view.findViewById(R.id.button_go_to_roulette_from_choice)

        viewModel.recommendedRestaurants.observe(viewLifecycleOwner) { restaurants ->
            if (restaurants.isNullOrEmpty()) {
                recommendationSummary.text = "未能生成推薦。"
                viewRecommendationsButton.isEnabled = false
                startRouletteButton.isEnabled = false
            } else {
                recommendationSummary.text = "為您發現了 ${restaurants.size} 家符合條件的餐廳。"
                viewRecommendationsButton.isEnabled = true
                startRouletteButton.isEnabled = restaurants.size > 1
            }
        }

        viewRecommendationsButton.setOnClickListener {
            val restaurants = viewModel.recommendedRestaurants.value
            if (!restaurants.isNullOrEmpty()) {
                val action = ActionChoiceFragmentDirections.actionActionChoiceFragmentToRestaurantListFragment(
                    restaurants.toTypedArray(), "AI推薦", null
                )
                findNavController().navigate(action)
            }
        }

        startRouletteButton.setOnClickListener {
            val restaurants = viewModel.recommendedRestaurants.value
            if (!restaurants.isNullOrEmpty()) {
                val action = ActionChoiceFragmentDirections.actionActionChoiceFragmentToRouletteFragment(restaurants.toTypedArray())
                findNavController().navigate(action)
            }
        }

        return view
    }
}