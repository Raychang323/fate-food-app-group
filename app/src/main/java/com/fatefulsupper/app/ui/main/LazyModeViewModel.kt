package com.fatefulsupper.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fatefulsupper.app.R // For placeholder images
import com.fatefulsupper.app.data.model.FoodTypeCard

class LazyModeViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentFoodTypeCard = MutableLiveData<FoodTypeCard?>()
    val currentFoodTypeCard: LiveData<FoodTypeCard?> = _currentFoodTypeCard

    // Triggers when all food type preferences have been collected
    // Holds a list of (FoodTypeID, isLiked: Boolean)
    private val _foodTypePreferencesCollected = MutableLiveData<List<Pair<String, Boolean>>?>()
    val foodTypePreferencesCollected: LiveData<List<Pair<String, Boolean>>?> = _foodTypePreferencesCollected

    private val _showCompletionMessage = MutableLiveData<Boolean>(false)
    val showCompletionMessage: LiveData<Boolean> = _showCompletionMessage

    private var cardIndex = 0
    private val userFoodTypePreferences = mutableListOf<Pair<String, Boolean>>()

    // Define three fixed food type cards
    private val foodTypeCards: List<FoodTypeCard> = listOf(
        FoodTypeCard(
            id = "spicy_preference", 
            name = "愛吃辣嗎？", 
            description = "今天想來點刺激的，還是口味溫和點好呢？", 
            imageResId = R.drawable.ic_placeholder_food_type_spicy // Replace with actual drawable
        ),
        FoodTypeCard(
            id = "meat_vs_veg_preference", 
            name = "肉食還是素食？", 
            description = "是無肉不歡，還是偏愛清爽的蔬食料理？", 
            imageResId = R.drawable.ic_placeholder_food_type_meat_veg // Replace with actual drawable
        ),
        FoodTypeCard(
            id = "cuisine_style_preference", 
            name = "異國風味或家常菜？", 
            description = "想嘗試點特別的異國料理，還是想念熟悉的家常味道？", 
            imageResId = R.drawable.ic_placeholder_food_type_cuisine // Replace with actual drawable
        )
    )

    init {
        loadNextFoodTypeCard()
    }

    private fun loadNextFoodTypeCard() {
        if (cardIndex < foodTypeCards.size) {
            _currentFoodTypeCard.value = foodTypeCards[cardIndex]
            _showCompletionMessage.value = false
        } else {
            _currentFoodTypeCard.value = null
            _showCompletionMessage.value = true // All cards swiped
            _foodTypePreferencesCollected.value = ArrayList(userFoodTypePreferences) // Trigger collection event
        }
    }

    private fun recordPreferenceAndLoadNext(liked: Boolean) {
        _currentFoodTypeCard.value?.let {
            userFoodTypePreferences.add(Pair(it.id, liked))
            cardIndex++
            loadNextFoodTypeCard()
        }
    }

    fun likeCurrentCard() {
        recordPreferenceAndLoadNext(true)
    }

    fun dislikeCurrentCard() {
        recordPreferenceAndLoadNext(false)
    }

    fun resetCardDeck() {
        cardIndex = 0
        userFoodTypePreferences.clear()
        _foodTypePreferencesCollected.value = null // Reset collection event
        loadNextFoodTypeCard()
    }

    // Call this after the collected preferences have been handled (e.g., after simulated API call)
    fun onPreferencesHandled() {
        _foodTypePreferencesCollected.value = null
    }
}
