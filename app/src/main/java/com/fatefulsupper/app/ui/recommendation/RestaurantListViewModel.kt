package com.fatefulsupper.app.ui.recommendation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fatefulsupper.app.data.model.Restaurant

class RestaurantListViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    // Keep a full list of all available dummy restaurants for "all" type
    private val allDummyRestaurants: List<Restaurant> = listOf(
        Restaurant(
            id = "res1",
            name = "海之味壽司屋",
            photoUrl = "https://via.placeholder.com/300/FF0000/FFFFFF?Text=Sushi",
            cuisine = "日式料理",
            briefDescription = "提供最新鮮的生魚片與創意壽司捲。",
            latitude = 25.0330, longitude = 121.5654, isFavorite = false
        ),
        Restaurant(
            id = "res2",
            name = "媽媽的義大利麵廚房",
            photoUrl = "https://via.placeholder.com/300/00FF00/FFFFFF?Text=Pasta",
            cuisine = "義式料理",
            briefDescription = "家常義大利麵與道地醬料，溫暖您的胃。",
            latitude = 25.0340, longitude = 121.5664, isFavorite = true
        ),
        Restaurant(
            id = "res3",
            name = "火燄山川菜館",
            photoUrl = "https://via.placeholder.com/300/0000FF/FFFFFF?Text=Sichuan",
            cuisine = "中式川菜",
            briefDescription = "麻辣鮮香，挑戰您的味蕾極限！",
            latitude = 25.0350, longitude = 121.5674, isFavorite = false
        ),
        // Keep only a few for "all" as the primary new source is AI or liked
        Restaurant(
            id = "res4",
            name = "晨曦早午餐坊",
            photoUrl = "https://via.placeholder.com/300/FFFF00/000000?Text=Brunch",
            cuisine = "早午餐",
            briefDescription = "悠閒享用豐盛早午餐與香醇咖啡的好去處。",
            latitude = 25.0360, longitude = 121.5684, isFavorite = false
        )
    )

    init {
        // Default load, can be overridden by specific calls from fragment
        // loadRestaurants(filterType = "all", likedIds = null, aiGeneratedList = null)
    }

    fun loadRestaurants(filterType: String?, likedIds: Array<String>?, aiGeneratedList: List<Restaurant>?) {
        val likedIdSet = likedIds?.toSet() // Convert array to set for efficient lookup

        val filteredList = when (filterType) {
            "ai_generated" -> {
                aiGeneratedList ?: emptyList() // Use AI list if provided
            }
            "liked" -> {
                if (!likedIdSet.isNullOrEmpty()) {
                    // In a real app with a proper repository, you'd fetch these by ID.
                    // For now, if allDummyRestaurants is the master list, filter from it.
                    // This part needs careful thought if AI list is primary and liked items might be outside allDummyRestaurants
                    allDummyRestaurants.filter { restaurant -> restaurant.id in likedIdSet } // Placeholder logic
                } else {
                    emptyList() 
                }
            }
            else -> { // Default to "all" or any other type
                allDummyRestaurants
            }
        }
        _restaurants.value = filteredList
    }
}
