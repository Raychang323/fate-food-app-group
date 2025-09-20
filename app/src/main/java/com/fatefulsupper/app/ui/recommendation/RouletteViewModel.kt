package com.fatefulsupper.app.ui.recommendation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fatefulsupper.app.data.model.Restaurant
import kotlin.random.Random

class RouletteViewModel : ViewModel() {

    // Pair: First is the index in the list, Second is the Restaurant object itself
    private val _targetRestaurantForSpin = MutableLiveData<Pair<Int, Restaurant>?>()
    val targetRestaurantForSpin: LiveData<Pair<Int, Restaurant>?> = _targetRestaurantForSpin

    private val _navigateToDetails = MutableLiveData<Restaurant?>()
    val navigateToDetails: LiveData<Restaurant?> = _navigateToDetails

    private var currentRestaurantsForRoulette: List<Restaurant> = emptyList()
    private var lastSelectedRestaurant: Restaurant? = null

    fun loadRestaurants(newRestaurants: List<Restaurant>) {
        currentRestaurantsForRoulette = newRestaurants
        Log.d("RouletteViewModel", "Loaded ${newRestaurants.size} restaurants for roulette.")
        _targetRestaurantForSpin.value = null // Reset any previous spin target
        lastSelectedRestaurant = null
    }

    // Called when the user clicks the spin button
    fun pickRestaurantForSpin() {
        if (currentRestaurantsForRoulette.isNotEmpty()) {
            val randomIndex = Random.nextInt(currentRestaurantsForRoulette.size)
            val restaurant = currentRestaurantsForRoulette[randomIndex]
            lastSelectedRestaurant = restaurant // Store for when animation finishes
            _targetRestaurantForSpin.value = Pair(randomIndex, restaurant)
            Log.d("RouletteViewModel", "Target for spin: ${restaurant.name} at index $randomIndex")
        } else {
            _targetRestaurantForSpin.value = null
            Log.w("RouletteViewModel", "Pick attempted but restaurant list is empty.")
        }
    }

    // Called by the Fragment when the RouletteWheelView animation is complete
    fun onSpinAnimationCompleted() {
        Log.d("RouletteViewModel", "Spin animation completed for ${lastSelectedRestaurant?.name}")
        _navigateToDetails.value = lastSelectedRestaurant
    }

    fun onNavigationComplete() {
        _navigateToDetails.value = null
        // Optionally, you might want to clear lastSelectedRestaurant here too
        // lastSelectedRestaurant = null 
        // Or reset _targetRestaurantForSpin if you want the wheel to be "fresh"
        // _targetRestaurantForSpin.value = null 
    }
}
