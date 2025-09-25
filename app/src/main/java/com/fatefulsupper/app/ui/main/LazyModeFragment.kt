package com.fatefulsupper.app.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.test.cancel
import androidx.core.graphics.alpha
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.FoodTypeCard
import com.fatefulsupper.app.data.model.Restaurant
import com.google.android.material.card.MaterialCardView
import com.fatefulsupper.app.ui.custom.CustomQuadrantView

// Import CustomQuadrantView if it's in a different package and Android Studio doesn't auto-import

class LazyModeFragment : Fragment(), View.OnTouchListener {

    private lateinit var viewModel: LazyModeViewModel

    private lateinit var foodCardView: MaterialCardView
    private lateinit var foodImageView: ImageView
    private lateinit var foodNameTextView: TextView
    private lateinit var foodDescriptionTextView: TextView
    private lateinit var noMoreCardsContainer: LinearLayout
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button
    private lateinit var buttonGoToRoulette: Button
    private lateinit var completionMessageTextView: TextView
    private lateinit var getRecommendationsButton: Button

    // New Views for Quadrant Selection
    private lateinit var quadrantViewMoodHunger: com.fatefulsupper.app.ui.custom.CustomQuadrantView
    // private lateinit var buttonSubmitMoodHunger: Button // Optional: if you have a separate submit button

    private var lastGeneratedRestaurants: List<Restaurant>? = null

    private var initialTouchX = 0f
    private var initialCardTranslationX = 0f
    private val swipeThreshold = 150f
    private val cardAnimationDuration = 200L
    private val rotationMultiplier = 0.1f
    private val maxRotationAngle = 15f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lazy_mode, container, false)
        viewModel = ViewModelProvider(this).get(LazyModeViewModel::class.java)

        foodCardView = view.findViewById(R.id.cardView_food_item)
        foodImageView = view.findViewById(R.id.imageView_food_card_placeholder)
        foodNameTextView = view.findViewById(R.id.textView_food_card_name)
        foodDescriptionTextView = view.findViewById(R.id.textView_food_card_description)
        noMoreCardsContainer = view.findViewById(R.id.linearLayout_no_more_cards_container)
        completionMessageTextView = view.findViewById(R.id.textView_completion_message)
        getRecommendationsButton = view.findViewById(R.id.button_get_recommendations)
        likeButton = view.findViewById(R.id.button_like)
        dislikeButton = view.findViewById(R.id.button_dislike)
        buttonGoToRoulette = view.findViewById(R.id.button_go_to_roulette_from_lazy)

        // Initialize new views
        quadrantViewMoodHunger = view.findViewById(R.id.quadrant_view_mood_hunger)
        // buttonSubmitMoodHunger = view.findViewById(R.id.button_submit_mood_hunger) // Uncomment if using

        noMoreCardsContainer.isVisible = false
        completionMessageTextView.isVisible = false
        getRecommendationsButton.isVisible = false
        buttonGoToRoulette.isVisible = false
        quadrantViewMoodHunger.isVisible = false
        // buttonSubmitMoodHunger.isVisible = false // Uncomment if using

        setupTouchListener()
        setupClickListeners()
        setupQuadrantViewListener() // Setup listener for the new view
        observeViewModel()
        return view
    }

    private fun setupClickListeners() {
        likeButton.setOnClickListener {
            viewModel.likeCurrentCard()
        }

        dislikeButton.setOnClickListener {
            viewModel.dislikeCurrentCard()
        }

        getRecommendationsButton.setOnClickListener {
            if (lastGeneratedRestaurants != null && lastGeneratedRestaurants!!.isNotEmpty()) {
                val action = LazyModeFragmentDirections.actionLazyModeFragmentToRestaurantListFragment(
                    listFilterType = "ai_generated",
                    likedRestaurantIds = null,
                    aiGeneratedRestaurants = lastGeneratedRestaurants!!.toTypedArray()
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "AI推薦資料仍在產生中或無資料，請稍候再試。", Toast.LENGTH_SHORT).show()
            }
        }

        buttonGoToRoulette.setOnClickListener {
            if (lastGeneratedRestaurants != null && lastGeneratedRestaurants!!.isNotEmpty()) {
                val action = LazyModeFragmentDirections.actionLazyModeFragmentToRouletteFragment(
                    restaurantsForRoulette = lastGeneratedRestaurants!!.toTypedArray()
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "輪盤資料仍在產生中或無資料，請稍候再試。", Toast.LENGTH_SHORT).show()
            }
        }

        // Optional: click listener for a separate submit button for mood/hunger
        // buttonSubmitMoodHunger.setOnClickListener {
        // val selectedPoint = quadrantViewMoodHunger.getSelectedPoint()
        // Log.d("LazyModeFragment", "Submit Mood/Hunger: X=${selectedPoint.first}, Y=${selectedPoint.second}")
        // viewModel.userSelectedMoodAndHunger(selectedPoint.first, selectedPoint.second)
        // quadrantViewMoodHunger.isVisible = false
        // buttonSubmitMoodHunger.isVisible = false
        // showRecommendationOptions("狀態已確認！準備好查看推薦了嗎？")
        // }
    }

    private fun setupQuadrantViewListener() {
        quadrantViewMoodHunger.setOnQuadrantSelectedListener(object : com.fatefulsupper.app.ui.custom.CustomQuadrantView.OnQuadrantSelectedListener {
            override fun onQuadrantSelected(x: Float, y: Float) {
                Log.d("LazyModeFragment", "Quadrant selected: X=$x, Y=$y")
                Toast.makeText(context, "狀態已記錄: 飢餓度 $x, 開心度 $y", Toast.LENGTH_SHORT).show()

                // Pass to ViewModel
                // viewModel.userSelectedMoodAndHunger(x, y) // You'll need to create this in ViewModel

                // After selection, hide quadrant and show recommendation buttons
                quadrantViewMoodHunger.isVisible = false
                // buttonSubmitMoodHunger.isVisible = false // Hide if you have it

                showRecommendationOptions("狀態已更新！準備好查看為您量身打造的推薦了嗎？")
            }
        })
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        foodCardView.setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (view.id != R.id.cardView_food_item || !foodCardView.isVisible || noMoreCardsContainer.isVisible || quadrantViewMoodHunger.isVisible) {
            // Also ignore touch if quadrant view is visible
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialCardTranslationX = view.translationX
                view.parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                view.translationX = initialCardTranslationX + deltaX
                var rotationAngle = view.translationX * rotationMultiplier
                if (rotationAngle > maxRotationAngle) rotationAngle = maxRotationAngle
                if (rotationAngle < -maxRotationAngle) rotationAngle = -maxRotationAngle
                view.rotation = rotationAngle
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                view.parent.requestDisallowInterceptTouchEvent(false)
                val currentTranslationX = view.translationX
                val parentWidth = (view.parent as ViewGroup).width
                if (currentTranslationX > swipeThreshold) {
                    view.animate().translationX(parentWidth.toFloat()).rotation(maxRotationAngle * 1.5f).alpha(0f)
                        .setDuration(cardAnimationDuration).withEndAction { viewModel.likeCurrentCard() }.start()
                } else if (currentTranslationX < -swipeThreshold) {
                    view.animate().translationX(-parentWidth.toFloat()).rotation(-maxRotationAngle * 1.5f).alpha(0f)
                        .setDuration(cardAnimationDuration).withEndAction { viewModel.dislikeCurrentCard() }.start()
                } else {
                    view.animate().translationX(0f).rotation(0f).alpha(1f).setDuration(cardAnimationDuration).start()
                }
                return true
            }
        }
        return false
    }

    private fun observeViewModel() {
        viewModel.currentFoodTypeCard.observe(viewLifecycleOwner) { foodTypeCard ->
            foodCardView.animate().cancel()
            foodCardView.translationX = 0f
            foodCardView.alpha = 1f
            foodCardView.rotation = 0f

            val isCompletionActive = viewModel.showCompletionMessage.value == true
            val isQuadrantVisible = quadrantViewMoodHunger.isVisible // Check if quadrant is already shown

            if (foodTypeCard != null && !isCompletionActive && !isQuadrantVisible) {
                updateFoodTypeCardUI(foodTypeCard)
                foodCardView.isVisible = true
                likeButton.isVisible = true
                dislikeButton.isVisible = true
            } else if (!isCompletionActive && !isQuadrantVisible) { // Only hide card if not in completion and not showing quadrant
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false
            }
        }

        viewModel.showCompletionMessage.observe(viewLifecycleOwner) { show ->
            if (show) {
                foodCardView.isVisible = false // Hide card view components
                likeButton.isVisible = false
                dislikeButton.isVisible = false

                // **PLACEHOLDER: Replace with actual login check from ViewModel or AuthManager**
                val isLoggedIn = true
                // Example: val isLoggedIn = viewModel.isUserLoggedIn()

                if (isLoggedIn) {
                    Log.d("LazyModeFragment", "Card swiping complete. Showing mood/hunger quadrant view.")
                    noMoreCardsContainer.isVisible = false // Hide recommendation options initially
                    completionMessageTextView.isVisible = false
                    getRecommendationsButton.isVisible = false
                    buttonGoToRoulette.isVisible = false

                    quadrantViewMoodHunger.setInitialPoint(0f, 0f) // Reset to center before showing
                    quadrantViewMoodHunger.isVisible = true
                    // buttonSubmitMoodHunger.isVisible = true // Show if you have a separate submit button
                } else {
                    // Non-logged-in user or if quadrant feature is disabled for them
                    Log.d("LazyModeFragment", "Card swiping complete. Showing recommendation options directly.")
                    showRecommendationOptions("感謝您的選擇！準備好查看為您量身打造的推薦了嗎？")
                }
            } else {
                // Not in completion message state
                quadrantViewMoodHunger.isVisible = false // Ensure quadrant is hidden
                // buttonSubmitMoodHunger.isVisible = false // Ensure button is hidden

                noMoreCardsContainer.isVisible = false // Hide recommendation options
                completionMessageTextView.isVisible = false
                getRecommendationsButton.isVisible = false
                buttonGoToRoulette.isVisible = false

                // Restore card view if there's a card to show
                val hasCurrentCard = viewModel.currentFoodTypeCard.value != null
                foodCardView.isVisible = hasCurrentCard
                likeButton.isVisible = hasCurrentCard
                dislikeButton.isVisible = hasCurrentCard
            }
        }
    }

    private fun showRecommendationOptions(message: String) {
        noMoreCardsContainer.isVisible = true
        completionMessageTextView.text = message
        completionMessageTextView.isVisible = true
        getRecommendationsButton.isVisible = true
        buttonGoToRoulette.isVisible = true

        // Generate/retrieve recommendations
        // This logic might need to be refined based on when mood/hunger data is available to ViewModel
        if (lastGeneratedRestaurants == null) {
            val preferences = viewModel.foodTypePreferencesCollected.value ?: emptyList()
            // Here you might get the mood/hunger from quadrantViewMoodHunger.getSelectedPoint()
            // if you decide to pass it directly to generateSimulatedLlamaResponse,
            // or better, ensure ViewModel has it and provides it.
            // val moodHunger = quadrantViewMoodHunger.getSelectedPoint() // Example
            // Log.d("LazyModeFragment", "Generating recommendations with prefs: $preferences and mood/hunger: $moodHunger")
            Log.d("LazyModeFragment", "Generating recommendations with prefs: $preferences")
            lastGeneratedRestaurants = generateSimulatedLlamaResponse(preferences)
            // viewModel.onPreferencesHandled() // Consider if this is still needed or handled differently
        }
        Log.d("LazyModeFragment", "Recommendations available: ${lastGeneratedRestaurants?.size ?: 0} items.")
    }


    // This is a placeholder for getting recommendations.
    // In a real app, this would involve ViewModel calls to a repository/backend.
    private fun generateSimulatedLlamaResponse(preferences: List<Pair<String, Boolean>>): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val baseCount = 20 // Number of restaurants to generate
        val prefText = if (preferences.isEmpty()) "通用" else preferences.joinToString { it.first + ":" + if (it.second) "喜歡" else "不喜歡" }
        // Potentially use hunger/mood data if available here
        // val currentMoodHunger = quadrantViewMoodHunger.getSelectedPoint() // Example if needed directly

        for (i in 1..baseCount) {
            restaurants.add(
                Restaurant(
                    id = "llama_res_lazy_mood_$i", // Added mood to id for distinction
                    name = "AI推薦餐廳 #$i (基於心情)",
                    photoUrl = "https://picsum.photos/seed/restaurant_mood_$i/300/200", // Different seed
                    cuisine = if (i % 3 == 0) "特色川菜" else if (i % 3 == 1) "創意日料" else "溫馨義式",
                    briefDescription = "這是由AI根據您的 ($prefText) 偏好及當前狀態特別推薦的第 $i 家餐廳。",
                    isFavorite = false,
                    latitude = 25.0330 + (i * 0.0012), // Slightly different coordinates
                    longitude = 121.5654 + (i * 0.0012)
                )
            )
        }
        return restaurants.shuffled().take(10) // Simulate some variation and limit to 10 for roulette
    }

    private fun updateFoodTypeCardUI(foodTypeCard: FoodTypeCard) {
        foodNameTextView.text = foodTypeCard.name
        foodDescriptionTextView.text = foodTypeCard.description
        try {
            foodImageView.setImageResource(foodTypeCard.imageResId)
        } catch (e: Exception) {
            Log.e("LazyModeFragment", "Error setting image resource for food type card: ${foodTypeCard.id}", e)
            foodImageView.setImageResource(R.drawable.ic_menu_gallery) // Fallback image
        }
    }

    override fun onResume() {
        super.onResume()
        // Simplified onResume logic, main visibility is handled by LiveData observers
        val isShowingCompletion = viewModel.showCompletionMessage.value == true
        val isQuadrantVisible = quadrantViewMoodHunger.isVisible

        if (!isShowingCompletion && !isQuadrantVisible) {
            val hasCurrentCard = ::foodCardView.isInitialized && viewModel.currentFoodTypeCard.value != null
            if (hasCurrentCard) {
                foodCardView.isVisible = true
                likeButton.isVisible = true
                dislikeButton.isVisible = true
                foodCardView.translationX = 0f
                foodCardView.alpha = 1f
                foodCardView.rotation = 0f
            } else {
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false
            }
        }
    }
}
