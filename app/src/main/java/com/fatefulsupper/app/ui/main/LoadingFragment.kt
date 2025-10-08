package com.fatefulsupper.app.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // Added import
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import kotlinx.coroutines.delay // Added import
import kotlinx.coroutines.launch // Added import

class LoadingFragment : Fragment() {

    private lateinit var foodieModeViewModel: FoodieModeViewModel // Renamed to avoid confusion if not always used
    private val TAG = "LoadingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sourceFragment = arguments?.getString("sourceFragment")
        Log.d(TAG, "onViewCreated: sourceFragment is $sourceFragment")

        when (sourceFragment) {
            "guestMode" -> {
                lifecycleScope.launch {
                    // TODO: 在未來實際的API串接時，將這段延遲程式碼替換為API呼叫和結果處理。
                    // 此處假設 Guest Mode 會直接提供餐廳列表，不需要 ActionChoiceFragment
                    delay(3000) // Simulate loading for 3 seconds
                    if (isAdded) { // Ensure fragment is still attached to activity
                        Log.d(TAG, "Guest Mode loading complete, navigating to RestaurantListFragment.")
                        // Navigate directly to RestaurantListFragment
                        findNavController().navigate(R.id.action_loadingFragment_to_restaurantListFragment)
                    }
                }
            }
            "foodieMode" -> {
                // Keep the existing ViewModel observation for Foodie Mode
                foodieModeViewModel = ViewModelProvider(requireActivity())[FoodieModeViewModel::class.java]
                Log.d(TAG, "onViewCreated: Observing navigateToResultsEvent for FoodieMode.")

                foodieModeViewModel.navigateToResultsEvent.observe(viewLifecycleOwner) { event ->
                    event.getContentIfNotHandled()?.let {
                        Log.d(TAG, "navigateToResultsEvent observed (FoodieMode), navigating to ActionChoiceFragment.")
                        // Navigate to ActionChoiceFragment. Data will be fetched from ViewModel there.
                        findNavController().navigate(R.id.action_loadingFragment_to_actionChoiceFragment)
                    }
                }
            }
            else -> {
                // Default case, perhaps log an error or handle unexpected source
                Log.e(TAG, "Unknown or missing sourceFragment: $sourceFragment. Navigating to ActionChoice-Fragment as default.")
                foodieModeViewModel = ViewModelProvider(requireActivity())[FoodieModeViewModel::class.java]
                foodieModeViewModel.navigateToResultsEvent.observe(viewLifecycleOwner) { event ->
                    event.getContentIfNotHandled()?.let {
                        findNavController().navigate(R.id.action_loadingFragment_to_actionChoiceFragment)
                    }
                }
            }
        }
    }
}
