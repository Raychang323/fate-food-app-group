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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.FoodTypeCard
import com.fatefulsupper.app.data.model.Restaurant
import com.google.android.material.card.MaterialCardView

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

        noMoreCardsContainer.isVisible = false
        completionMessageTextView.isVisible = false
        getRecommendationsButton.isVisible = false
        buttonGoToRoulette.isVisible = false

        setupTouchListener()
        setupClickListeners()
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
                Toast.makeText(context, "AI推薦資料仍在產生中，請稍候再試。", Toast.LENGTH_SHORT).show()
            }
        }

        buttonGoToRoulette.setOnClickListener {
            if (lastGeneratedRestaurants != null && lastGeneratedRestaurants!!.isNotEmpty()) {
                val action = LazyModeFragmentDirections.actionLazyModeFragmentToRouletteFragment(
                    restaurantsForRoulette = lastGeneratedRestaurants!!.toTypedArray()
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "輪盤資料仍在產生中，請稍候再試。", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        foodCardView.setOnTouchListener(this)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (view.id != R.id.cardView_food_item || !foodCardView.isVisible || noMoreCardsContainer.isVisible) {
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
            if (foodTypeCard != null && !isCompletionActive) {
                updateFoodTypeCardUI(foodTypeCard)
                foodCardView.isVisible = true
                likeButton.isVisible = true
                dislikeButton.isVisible = true
            } else if (!isCompletionActive) {
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false
            }
        }

        viewModel.showCompletionMessage.observe(viewLifecycleOwner) { show ->
            noMoreCardsContainer.isVisible = show
            completionMessageTextView.isVisible = show
            getRecommendationsButton.isVisible = show
            buttonGoToRoulette.isVisible = show

            if (show) {
                completionMessageTextView.text = "感謝您的選擇！準備好查看為您量身打造的推薦了嗎？"
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false

                if (lastGeneratedRestaurants == null) { // Generate only if not already generated in this lifecycle
                    val preferences = viewModel.foodTypePreferencesCollected.value
                    if (preferences != null && preferences.isNotEmpty()) {
                        Log.d("LazyModeFragment", "Food Type Preferences from ViewModel: $preferences")
                        Toast.makeText(context, "偏好已收集，正在根據偏好產生AI推薦...", Toast.LENGTH_SHORT).show()
                        lastGeneratedRestaurants = generateSimulatedLlamaResponse(preferences)
                        viewModel.onPreferencesHandled() // Notify ViewModel only if actual preferences were used
                    } else {
                        Log.w("LazyModeFragment", "Preferences from ViewModel are null or empty. Generating default test AI recommendations.")
                        Toast.makeText(context, "未能收集到偏好，正在產生通用測試推薦...", Toast.LENGTH_SHORT).show()
                        lastGeneratedRestaurants = generateSimulatedLlamaResponse(emptyList()) // Force generation with empty prefs
                    }
                    Log.d("LazyModeFragment", "Generated AI Recommendations: ${lastGeneratedRestaurants?.size} items")
                }
            } else {
                // Reset when not showing completion message (e.g., user navigates back or restarts)
                lastGeneratedRestaurants = null
                val hasCurrentCard = viewModel.currentFoodTypeCard.value != null
                foodCardView.isVisible = hasCurrentCard
                likeButton.isVisible = hasCurrentCard
                dislikeButton.isVisible = hasCurrentCard
            }
        }
    }

    private fun generateSimulatedLlamaResponse(preferences: List<Pair<String, Boolean>>): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val baseCount = 20
        val prefText = if (preferences.isEmpty()) "通用" else preferences.joinToString { it.first + ":" + if (it.second) "喜歡" else "不喜歡" }
        for (i in 1..baseCount) {
            restaurants.add(
                Restaurant(
                    id = "llama_res_lazy_$i",
                    name = "AI推薦餐廳 #$i",
                    photoUrl = "https://picsum.photos/seed/restaurant$i/300/200",
                    cuisine = if (i % 3 == 0) "特色川菜" else if (i % 3 == 1) "創意日料" else "溫馨義式",
                    briefDescription = "這是由AI根據您的 ($prefText) 偏好特別推薦的第 $i 家餐廳。",
                    isFavorite = false,
                    latitude = 25.0330 + (i * 0.001),
                    longitude = 121.5654 + (i * 0.001)
                )
            )
        }
        return restaurants
    }

    private fun updateFoodTypeCardUI(foodTypeCard: FoodTypeCard) {
        foodNameTextView.text = foodTypeCard.name
        foodDescriptionTextView.text = foodTypeCard.description
        try {
            foodImageView.setImageResource(foodTypeCard.imageResId)
        } catch (e: Exception) {
            foodImageView.setImageResource(R.drawable.ic_menu_gallery)
            Log.e("LazyModeFragment", "Error setting image resource for food type card: ${foodTypeCard.id}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        val isShowingCompletion = viewModel.showCompletionMessage.value == true

        noMoreCardsContainer.isVisible = isShowingCompletion
        completionMessageTextView.isVisible = isShowingCompletion
        getRecommendationsButton.isVisible = isShowingCompletion
        buttonGoToRoulette.isVisible = isShowingCompletion

        val hasCurrentCard = ::foodCardView.isInitialized && viewModel.currentFoodTypeCard.value != null
        foodCardView.isVisible = !isShowingCompletion && hasCurrentCard
        likeButton.isVisible = !isShowingCompletion && hasCurrentCard
        dislikeButton.isVisible = !isShowingCompletion && hasCurrentCard

        if (isShowingCompletion) {
            completionMessageTextView.text = "感謝您的選擇！準備好查看為您量身打造的推薦了嗎？"
            // If completion is shown, ensure restaurant list is populated, even if preferences were empty
            if (lastGeneratedRestaurants == null) {
                val preferences = viewModel.foodTypePreferencesCollected.value
                if (preferences != null && preferences.isNotEmpty()) {
                    Log.d("LazyModeFragment", "onResume: Preferences available, re-generating AI list with actual preferences.")
                    lastGeneratedRestaurants = generateSimulatedLlamaResponse(preferences)
                } else {
                    Log.w("LazyModeFragment", "onResume: Preferences from ViewModel are null or empty. Re-generating default test AI recommendations.")
                    lastGeneratedRestaurants = generateSimulatedLlamaResponse(emptyList()) // Force generation
                }
                 Log.d("LazyModeFragment", "onResume: Generated AI Recommendations: ${lastGeneratedRestaurants?.size} items")
            }
        } else {
            // If not in completion state, and current card is available, ensure it's displayed correctly.
            if (hasCurrentCard) {
                foodCardView.translationX = 0f
                foodCardView.alpha = 1f
                foodCardView.rotation = 0f
            }
        }
    }
}
