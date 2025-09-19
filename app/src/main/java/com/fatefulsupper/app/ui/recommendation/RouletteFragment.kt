package com.fatefulsupper.app.ui.recommendation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.navigation.fragment.navArgs
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant // Ensure Restaurant is imported

class RouletteFragment : Fragment() {

    private lateinit var viewModel: RouletteViewModel
    private lateinit var spinButton: Button
    private lateinit var resultTextView: TextView

    private val args: RouletteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_roulette, container, false)
        viewModel = ViewModelProvider(this).get(RouletteViewModel::class.java)

        spinButton = view.findViewById(R.id.button_spin_roulette)
        resultTextView = view.findViewById(R.id.textView_selected_roulette_result)

        val restaurantsFromArgs = args.restaurantsForRoulette?.toList()

        if (restaurantsFromArgs != null && restaurantsFromArgs.isNotEmpty()) {
            viewModel.loadRestaurants(restaurantsFromArgs) // ViewModel should handle this list
            spinButton.isEnabled = true
            resultTextView.text = "準備好了嗎？點擊按鈕來旋轉命運輪盤！"
            resultTextView.isVisible = true
        } else {
            spinButton.isEnabled = false
            resultTextView.text = "輪盤沒有可用的餐廳資料。"
            resultTextView.isVisible = true
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
            resultTextView.text = "旋轉中..."
            resultTextView.isVisible = true
            spinButton.isEnabled = false 
            viewModel.spinAndSelectRestaurant()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedRestaurant.observe(viewLifecycleOwner) { restaurant ->
            // Only re-enable spin button if there was valid data initially and spin is complete
            if (args.restaurantsForRoulette?.isNotEmpty() == true) {
                spinButton.isEnabled = true
            }

            if (restaurant != null) {
                resultTextView.text = "選中了： ${restaurant.name}"
                resultTextView.isVisible = true
            } else {
                if (resultTextView.text.toString().contains("旋轉中")) { 
                     resultTextView.text = "無法選定餐廳，請檢查資料。"
                } 
                // If it already said "輪盤沒有可用的餐廳資料。", don't overwrite
            }
        }

        // Assuming navigateToDetails LiveData in ViewModel emits the selected Restaurant object
        viewModel.navigateToDetails.observe(viewLifecycleOwner) { restaurant ->
            restaurant?.let { selectedRestaurant -> // selectedRestaurant is the full Restaurant object
                Log.d("RouletteFragment", "Navigating to details for: ${selectedRestaurant.name}, ID: ${selectedRestaurant.id}")
                Handler(Looper.getMainLooper()).postDelayed({
                    val action = RouletteFragmentDirections.actionRouletteFragmentToRestaurantDetailsFragment(
                        restaurantId = selectedRestaurant.id, // Pass the ID
                        selectedRestaurantFull = selectedRestaurant // Pass the full Restaurant object
                    )
                    findNavController().navigate(action)
                    viewModel.onNavigationComplete() 
                }, 1000) 
            }
        }
    }
}
