package com.fatefulsupper.app.ui.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.data.preferences.FavoritesManager
import kotlinx.coroutines.launch

class RestaurantDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _restaurant = MutableLiveData<Restaurant?>()
    val restaurant: LiveData<Restaurant?> = _restaurant

    private val _isCurrentRestaurantFavorite = MutableLiveData<Boolean>()
    val isCurrentRestaurantFavorite: LiveData<Boolean> = _isCurrentRestaurantFavorite

    // This dummy list should ideally be fetched from a repository
    private val dummyRestaurantList: List<Restaurant> = listOf(
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
        Restaurant(
            id = "res4",
            name = "晨曦早午餐坊",
            photoUrl = "https://via.placeholder.com/300/FFFF00/000000?Text=Brunch",
            cuisine = "早午餐",
            briefDescription = "悠閒享用豐盛早午餐與香醇咖啡的好去處。",
            latitude = 25.0360, longitude = 121.5684, isFavorite = false
        ),
        Restaurant(
            id = "res5",
            name = "墨西哥風情小館",
            photoUrl = "https://via.placeholder.com/300/00FFFF/000000?Text=Tacos",
            cuisine = "墨西哥料理",
            briefDescription = "道地塔可、恩七拉達，感受熱情拉丁美洲風味。",
            latitude = 25.0370, longitude = 121.5694, isFavorite = true
        )
    )

    fun loadRestaurantDetails(restaurantId: String) {
        viewModelScope.launch {
            val foundRestaurant = dummyRestaurantList.find { it.id == restaurantId }?.copy()
            if (foundRestaurant != null) {
                // Override isFavorite with value from FavoritesManager
                foundRestaurant.isFavorite = FavoritesManager.isRestaurantFavorite(getApplication(), foundRestaurant.id)
                _restaurant.postValue(foundRestaurant)
                _isCurrentRestaurantFavorite.postValue(foundRestaurant.isFavorite)
            } else {
                _restaurant.postValue(null) // Restaurant not found
                Log.w("RestaurantDetailsVM", "Restaurant with ID $restaurantId not found in dummy list.")
            }
        }
    }

    // New method to handle pre-loaded restaurant data
    fun setLoadedRestaurantDetails(loadedRestaurant: Restaurant) {
        viewModelScope.launch {
            // Make a copy to avoid modifying the passed object directly if it's from a shared source
            val restaurantCopy = loadedRestaurant.copy()
            // Ensure its favorite status is current according to FavoritesManager
            restaurantCopy.isFavorite = FavoritesManager.isRestaurantFavorite(getApplication(), restaurantCopy.id)
            
            _restaurant.postValue(restaurantCopy)
            _isCurrentRestaurantFavorite.postValue(restaurantCopy.isFavorite)
            Log.d("RestaurantDetailsVM", "Set loaded details for: ${restaurantCopy.name}, Fav: ${restaurantCopy.isFavorite}")
        }
    }

    fun toggleFavoriteStatus() {
        _restaurant.value?.let { currentRestaurant ->
            val newFavoriteStatus = FavoritesManager.toggleFavoriteStatus(getApplication(), currentRestaurant.id)
            // Update the copy in the LiveData
            val updatedRestaurant = currentRestaurant.copy(isFavorite = newFavoriteStatus)
            _restaurant.value = updatedRestaurant // Update the main restaurant LiveData so UI can observe full object changes
            _isCurrentRestaurantFavorite.value = newFavoriteStatus
        }
    }
}
