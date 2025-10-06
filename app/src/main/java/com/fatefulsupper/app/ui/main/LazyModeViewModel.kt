package com.fatefulsupper.app.ui.main

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.FoodTypeCard
import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.util.Event
import com.fatefulsupper.app.util.SetupConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MasterFoodItem(
    val id: String,
    val name: String,
    val description: String,
    val imageResId: Int
)

class LazyModeViewModel(application: Application) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(SetupConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentFoodTypeCard = MutableLiveData<FoodTypeCard?>()
    val currentFoodTypeCard: LiveData<FoodTypeCard?> = _currentFoodTypeCard

    private val _foodTypePreferencesCollected = MutableLiveData<List<Pair<String, Boolean>>?>()
    val foodTypePreferencesCollected: LiveData<List<Pair<String, Boolean>>?> = _foodTypePreferencesCollected

    private val _showCompletionMessage = MutableLiveData<Boolean>(false)
    val showCompletionMessage: LiveData<Boolean> = _showCompletionMessage

    private val _isLoadingRecommendations = MutableLiveData<Boolean>(false)
    val isLoadingRecommendations: LiveData<Boolean> = _isLoadingRecommendations

    private val _navigateToLoadingEvent = MutableLiveData<Event<Unit>>()
    val navigateToLoadingEvent: LiveData<Event<Unit>> = _navigateToLoadingEvent

    private val _navigateToResultsEvent = MutableLiveData<Event<Unit>>()
    val navigateToResultsEvent: LiveData<Event<Unit>> = _navigateToResultsEvent

    private val _navigationHasBeenHandled = MutableLiveData<Boolean>(false)
    val navigationHasBeenHandled: LiveData<Boolean> = _navigationHasBeenHandled

    private val _recommendedRestaurants = MutableLiveData<List<Restaurant>?>()
    val recommendedRestaurants: LiveData<List<Restaurant>?> = _recommendedRestaurants

    private var cardIndex = 0
    private val userFoodTypePreferences = mutableListOf<Pair<String, Boolean>>()

    internal val masterFoodItemsList: List<MasterFoodItem> =
        SetupConstants.SUPPER_TYPES_BLACKLIST_OPTIONS.map { (displayName, typeKey) ->
            MasterFoodItem(
                id = typeKey,
                name = displayName,
                description = "宵夜精選：$displayName",
                imageResId = R.drawable.ic_placeholder_generic_food
            )
        }

    internal var foodTypeCards: MutableList<FoodTypeCard> = mutableListOf()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        prepareRandomFoodTypeCards()
        loadNextFoodTypeCard()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES) {
            resetLazyModeState()
        }
    }

    internal fun prepareRandomFoodTypeCards() {
        val blacklistedTypeKeys = sharedPreferences.getStringSet(SetupConstants.KEY_BLACKLISTED_SUPPER_TYPES, emptySet()) ?: emptySet()
        val nonBlacklistedItems = masterFoodItemsList.filter { !blacklistedTypeKeys.contains(it.id) }

        foodTypeCards.clear()
        if (nonBlacklistedItems.isNotEmpty()) {
            val selectedItems = nonBlacklistedItems.shuffled(Random(System.nanoTime())).take(NUMBER_OF_CARDS_TO_SHOW)
            selectedItems.forEach { masterItem ->
                foodTypeCards.add(
                    FoodTypeCard(
                        id = masterItem.id,
                        name = masterItem.name,
                        description = masterItem.description,
                        imageResId = masterItem.imageResId
                    )
                )
            }
        }
    }

    private fun loadNextFoodTypeCard() {
        if (cardIndex < foodTypeCards.size) {
            _currentFoodTypeCard.value = foodTypeCards[cardIndex]
            _showCompletionMessage.value = false
        } else {
            _currentFoodTypeCard.value = null
            _showCompletionMessage.value = true
            if (userFoodTypePreferences.isNotEmpty() || foodTypeCards.isEmpty()) { 
                _foodTypePreferencesCollected.value = ArrayList(userFoodTypePreferences)
            }
        }
    }

    private fun recordPreferenceAndLoadNext(liked: Boolean) {
        _currentFoodTypeCard.value?.let { currentCard ->
            userFoodTypePreferences.add(Pair(currentCard.id, liked))
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

    fun onFoodTypeCardSwiped(liked: Boolean) {
        recordPreferenceAndLoadNext(liked)
    }

    fun resetLazyModeState() {
        cardIndex = 0
        userFoodTypePreferences.clear()
        _foodTypePreferencesCollected.value = null
        _showCompletionMessage.value = false
        _isLoadingRecommendations.value = false
        _navigationHasBeenHandled.value = false
        _recommendedRestaurants.value = null
        prepareRandomFoodTypeCards()
        loadNextFoodTypeCard()
    }

    fun onPreferencesHandled() {
        _foodTypePreferencesCollected.value = null
    }

    fun fetchRecommendations(mood: String, hunger: String) {
        _isLoadingRecommendations.value = true
        _navigateToLoadingEvent.value = Event(Unit)

        viewModelScope.launch {
            delay(2000L)
            val dummyRestaurants = listOf(
                Restaurant(id = "101", name = "推薦餐廳A", cuisine = "依偏好推薦", photoUrl = null, briefDescription = "為您精心挑選"),
                Restaurant(id = "102", name = "推薦餐廳B", cuisine = "依心情推薦", photoUrl = null, briefDescription = "符合您的期待"),
                Restaurant(id = "103", name = "推薦餐廳C", cuisine = "隨機精選", photoUrl = null, briefDescription = "帶來驚喜美味")
            )
            _recommendedRestaurants.value = dummyRestaurants
            _isLoadingRecommendations.value = false
            _navigateToResultsEvent.value = Event(Unit)
            _navigationHasBeenHandled.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        private const val TAG = "LazyModeViewModel"
        private const val NUMBER_OF_CARDS_TO_SHOW = 3
    }
}