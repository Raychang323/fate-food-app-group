package com.fatefulsupper.app.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant // Required for casting

class ActionChoiceFragment : Fragment() {

    private lateinit var viewModel: LazyModeViewModel
    private lateinit var buttonGoToList: Button
    private lateinit var buttonGoToRoulette: Button
    private val TAG = "ActionChoiceFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_action_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LazyModeViewModel::class.java]

        buttonGoToList = view.findViewById(R.id.button_go_to_restaurant_list_from_choice)
        buttonGoToRoulette = view.findViewById(R.id.button_go_to_roulette_from_choice)

        buttonGoToList.setOnClickListener {
            navigateToRestaurantList()
        }

        buttonGoToRoulette.setOnClickListener {
            navigateToRoulette()
        }
    }

    private fun navigateToRestaurantList() {
        val restaurants = viewModel.recommendedRestaurants.value
        if (restaurants.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "沒有可供顯示的餐廳數據", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "No restaurants available for Restaurant List.")
            // Optionally, don't navigate or navigate to an empty state screen
            return
        }
        val restaurantsArray = restaurants.toTypedArray()
        val bundle = Bundle().apply {
            // Assuming RestaurantListFragment still expects "restaurants" as key for Parcelable array
            // Or, if it uses Safe Args, generate directions class and use that.
            // For now, using a generic bundle key if not using Safe Args for this specific action yet.
            putParcelableArray("restaurants", restaurantsArray) 
        }
        try {
            Log.d(TAG, "Navigating to RestaurantListFragment with ${restaurantsArray.size} restaurants.")
            // Replace with your actual action ID from nav_graph
            findNavController().navigate(R.id.action_actionChoiceFragment_to_restaurantListFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to RestaurantListFragment failed", e)
            Toast.makeText(requireContext(), "無法導航到列表頁面", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToRoulette() {
        val restaurants = viewModel.recommendedRestaurants.value
        if (restaurants.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "沒有可供輪盤使用的餐廳數據", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "No restaurants available for Roulette.")
            return
        }
        val restaurantsArray = restaurants.toTypedArray()
        val bundle = Bundle().apply {
            putParcelableArray("restaurantsForRoulette", restaurantsArray)
        }
        try {
            Log.d(TAG, "Navigating to RouletteFragment with ${restaurantsArray.size} restaurants.")
            // Replace with your actual action ID from nav_graph
            findNavController().navigate(R.id.action_actionChoiceFragment_to_rouletteFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to RouletteFragment failed", e)
            Toast.makeText(requireContext(), "無法導航到輪盤頁面", Toast.LENGTH_SHORT).show()
        }
    }
}
