package com.fatefulsupper.app.ui.recommendation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fatefulsupper.app.data.model.Restaurant
import kotlin.random.Random

class RouletteViewModel : ViewModel() {

    private val _selectedRestaurant = MutableLiveData<Restaurant?>()
    val selectedRestaurant: LiveData<Restaurant?> = _selectedRestaurant

    private val _navigateToDetails = MutableLiveData<Restaurant?>()
    val navigateToDetails: LiveData<Restaurant?> = _navigateToDetails

    // This list will be populated by the Fragment from Safe Args
    private var currentRestaurantsForRoulette: List<Restaurant> = emptyList()

    // Method to be called by the Fragment to set the restaurants
    fun loadRestaurants(newRestaurants: List<Restaurant>) {
        currentRestaurantsForRoulette = newRestaurants
        Log.d("RouletteViewModel", "Loaded ${newRestaurants.size} restaurants for roulette.")
        // Reset selection if new list is loaded, in case old selection is not in new list
        _selectedRestaurant.value = null 
    }

    fun spinAndSelectRestaurant() {
        if (currentRestaurantsForRoulette.isNotEmpty()) {
            val randomIndex = Random.nextInt(currentRestaurantsForRoulette.size)
            val restaurant = currentRestaurantsForRoulette[randomIndex]
            _selectedRestaurant.value = restaurant
            _navigateToDetails.value = restaurant
            Log.d("RouletteViewModel", "Spun and selected: ${restaurant.name}")
        } else {
            _selectedRestaurant.value = null
            _navigateToDetails.value = null
            Log.w("RouletteViewModel", "Spin attempted but restaurant list is empty.")
        }
    }

    fun onNavigationComplete() {
        _navigateToDetails.value = null
    }
}
