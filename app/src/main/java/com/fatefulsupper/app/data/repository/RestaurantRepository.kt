package com.fatefulsupper.app.data.repository

import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.data.model.RestaurantDetails
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {

    // For Lazy Mode - card swiping (n16)
    suspend fun getInitialSwipeCards(): List<Restaurant>

    // Record user interaction from swipe - for Lamma AI (n19)
    suspend fun recordSwipeInteraction(
        itemId: String,
        liked: Boolean,
        hungerLevel: String?,
        mood: String?
    ): Boolean

    // For Recommended List (n21) - output from Lamma AI (n19)
    suspend fun getRecommendedRestaurantList(): List<Restaurant>

    // For Roulette (n18) - output from Lamma AI as "roulette.json" or dynamic list (n19)
    suspend fun getRouletteOptions(): List<Restaurant>

    // Get details for a specific restaurant (n22)
    suspend fun getRestaurantDetails(restaurantId: String): RestaurantDetails?

    // For Daily Lucky Meal (n29)
    suspend fun getDailyLuckyMeal(): Restaurant? // Or a specific LuckyMeal model

    // Favorite Restaurants
    fun getFavoriteRestaurants(): Flow<List<Restaurant>>
    suspend fun addRestaurantToFavorites(restaurantId: String)
    suspend fun removeRestaurantFromFavorites(restaurantId: String)
    suspend fun isFavorite(restaurantId: String): Boolean

    // Potentially, methods to directly interact with a local database or Lamma AI for syncing
    // suspend fun syncWithLammaAI(): Boolean
    // suspend fun storeLammaAIOutput(output: Any): Boolean // e.g. roulette.json
}
