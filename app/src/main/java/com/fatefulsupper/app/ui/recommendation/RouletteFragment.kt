package com.fatefulsupper.app.ui.recommendation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
// import android.widget.TextView // No longer needed
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant

class RouletteFragment : Fragment() {

    private lateinit var viewModel: RouletteViewModel
    private lateinit var spinButton: Button
    // private var resultTextView: TextView? = null // Removed
    private lateinit var rouletteWheelView: RouletteWheelView

    private val args: RouletteFragmentArgs by navArgs()
    private var restaurantsFromArgs: List<Restaurant>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_roulette, container, false)
        viewModel = ViewModelProvider(this).get(RouletteViewModel::class.java)

        spinButton = view.findViewById(R.id.button_spin_roulette)
        // resultTextView = view.findViewById(R.id.textView_selected_roulette_result) // Removed
        rouletteWheelView = view.findViewById(R.id.roulette_wheel_view)

        restaurantsFromArgs = args.restaurantsForRoulette?.toList()

        if (restaurantsFromArgs != null && restaurantsFromArgs!!.isNotEmpty()) {
            viewModel.loadRestaurants(restaurantsFromArgs!!)
            val restaurantNames = restaurantsFromArgs!!.map { it.name ?: "Unnamed" }
            rouletteWheelView.setRestaurantList(restaurantNames)
            spinButton.isEnabled = true
            // resultTextView?.text = "準備好了嗎？點擊按鈕來旋轉命運輪盤！" // Removed
            // resultTextView?.isVisible = true // Removed
            rouletteWheelView.isVisible = true
        } else {
            spinButton.isEnabled = false
            // resultTextView?.text = "輪盤沒有可用的餐廳資料。" // Removed
            // resultTextView?.isVisible = true // Removed
            rouletteWheelView.isVisible = false // Hide wheel if no data
            Log.w("RouletteFragment", "No restaurants passed for roulette.")
            Toast.makeText(context, "輪盤資料為空，請先從 Lazy Mode 產生 AI 推薦。", Toast.LENGTH_LONG).show()
        }

        setupClickListeners()
        observeViewModel()

        return view
    }

    private fun setupClickListeners() {
        spinButton.setOnClickListener {
            if (!spinButton.isEnabled) {
                Toast.makeText(context, "輪盤目前無法使用，缺少餐廳資料。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // resultTextView?.text = "旋轉中..." // Removed
            Toast.makeText(context, "輪盤旋轉中...", Toast.LENGTH_SHORT).show() // Optional: show a toast instead
            spinButton.isEnabled = false // Disable button during spin
            viewModel.pickRestaurantForSpin() // ViewModel will decide which restaurant to spin to
        }

        rouletteWheelView.onResultListener = { selectedRestaurantName ->
            // This is called when the RouletteWheelView animation finishes
            // resultTextView?.text = "選中了： $selectedRestaurantName" // Removed
            Toast.makeText(context, "選中了： $selectedRestaurantName", Toast.LENGTH_SHORT).show() // Optional: show a toast
            // Re-enable spin button only if there was valid data initially
            if (restaurantsFromArgs?.isNotEmpty() == true) {
                 spinButton.isEnabled = true
            }
            viewModel.onSpinAnimationCompleted() // Notify ViewModel that animation is done
        }
    }

    private fun observeViewModel() {
        viewModel.targetRestaurantForSpin.observe(viewLifecycleOwner) { pair ->
            pair?.let { (index, restaurant) ->
                Log.d("RouletteFragment", "ViewModel wants to spin to: ${restaurant.name} at index $index")
                // Only start spinning if the button was clicked (it would be disabled at this point)
                if (!spinButton.isEnabled) {
                     rouletteWheelView.spinToTarget(index)
                }
            }
        }

        viewModel.navigateToDetails.observe(viewLifecycleOwner) { restaurant ->
            restaurant?.let { selectedRestaurant ->
                Log.d("RouletteFragment", "Navigating to details for: ${selectedRestaurant.name}, ID: ${selectedRestaurant.id}")
                val action = RouletteFragmentDirections.actionRouletteFragmentToRestaurantDetailsFragment(
                    restaurantId = selectedRestaurant.id,
                    selectedRestaurantFull = selectedRestaurant
                )
                findNavController().navigate(action)
                viewModel.onNavigationComplete()
            }
        }
    }

    // override fun onDestroyView() { // No longer needed if resultTextView is fully removed
    //     super.onDestroyView()
    //     resultTextView = null 
    // }
}
