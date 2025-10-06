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
import com.fatefulsupper.app.data.model.Restaurant

class RouletteFragment : Fragment() {

    private lateinit var viewModel: RouletteViewModel
    private lateinit var spinButton: Button
    private lateinit var rouletteWheelView: RouletteWheelView
    private lateinit var textViewCurrentSelection: TextView

    private val args: RouletteFragmentArgs by navArgs()
    private var restaurantsForRouletteLogic: List<Restaurant>? = null
    private var isSpinning: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_roulette, container, false)
        viewModel = ViewModelProvider(this).get(RouletteViewModel::class.java)

        spinButton = view.findViewById(R.id.button_spin_roulette)
        rouletteWheelView = view.findViewById(R.id.roulette_wheel_view)
        textViewCurrentSelection = view.findViewById(R.id.textView_current_selection)

        val rawRestaurantsFromArgs = args.restaurantsForRoulette?.toList()

        if (rawRestaurantsFromArgs != null && rawRestaurantsFromArgs.isNotEmpty()) {
            val processedRestaurants = if (rawRestaurantsFromArgs.size > 10) {
                rawRestaurantsFromArgs.take(10)
            } else {
                rawRestaurantsFromArgs
            }
            
            this.restaurantsForRouletteLogic = processedRestaurants
            viewModel.loadRestaurants(processedRestaurants)
            val restaurantNames = processedRestaurants.map { it.name ?: "Unnamed" }
            
            rouletteWheelView.setRestaurantList(restaurantNames)
            spinButton.isEnabled = true
            textViewCurrentSelection.text = "準備好了嗎？"
            rouletteWheelView.isVisible = true
            textViewCurrentSelection.isVisible = true
        } else {
            this.restaurantsForRouletteLogic = emptyList()
            spinButton.isEnabled = false
            textViewCurrentSelection.text = "輪盤資料為空"
            rouletteWheelView.isVisible = false
            textViewCurrentSelection.isVisible = true
            Toast.makeText(context, "輪盤資料為空，請先從 Lazy Mode 產生 AI 推薦。", Toast.LENGTH_LONG).show()
        }

        setupClickListeners()
        observeViewModel()

        return view
    }

    private fun setupClickListeners() {
        spinButton.setOnClickListener {
            if (!spinButton.isEnabled || isSpinning) {
                return@setOnClickListener
            }
            isSpinning = true
            textViewCurrentSelection.text = "轉動中..."
            textViewCurrentSelection.isVisible = true
            spinButton.isEnabled = false
            viewModel.pickRestaurantForSpin()
        }

        rouletteWheelView.onResultListener = { selectedRestaurantName ->
            isSpinning = false
            textViewCurrentSelection.text = "選中了： $selectedRestaurantName"
            if (restaurantsForRouletteLogic?.isNotEmpty() == true) {
                 spinButton.isEnabled = true
            }
            viewModel.onSpinAnimationCompleted()
        }

        rouletteWheelView.onItemHoverListener = { hoveredItemName ->
            if (isSpinning) {
                if (hoveredItemName != null) {
                    textViewCurrentSelection.text = "目前指到: $hoveredItemName"
                } else {
                     textViewCurrentSelection.text = "轉動中..."
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.targetRestaurantForSpin.observe(viewLifecycleOwner) { pair ->
            pair?.let { (index, restaurant) ->
                if (isSpinning && !spinButton.isEnabled && index < (restaurantsForRouletteLogic?.size ?: 0)) {
                     rouletteWheelView.spinToTarget(index)
                } else if (isSpinning && !spinButton.isEnabled) {
                    isSpinning = false
                    spinButton.isEnabled = true
                    textViewCurrentSelection.text = "出現錯誤，請重試"
                    Toast.makeText(context, "輪盤選擇出錯，請重試。", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.navigateToDetails.observe(viewLifecycleOwner) { restaurant ->
            restaurant?.let { selectedRestaurant ->
                val actualSelectedRestaurant = restaurantsForRouletteLogic?.find { it.id == selectedRestaurant.id } ?: selectedRestaurant
                val action = RouletteFragmentDirections.actionRouletteFragmentToRestaurantDetailsFragment(
                    restaurantId = actualSelectedRestaurant.id,
                    selectedRestaurantFull = actualSelectedRestaurant,
                    sourceIsRoulette = true
                )
                findNavController().navigate(action)
                viewModel.onNavigationComplete()
            }
        }
    }
}