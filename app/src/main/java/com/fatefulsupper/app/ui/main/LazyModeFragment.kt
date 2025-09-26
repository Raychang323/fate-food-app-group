package com.fatefulsupper.app.ui.main

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.FoodTypeCard
import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.ui.custom.CustomQuadrantView
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
    private lateinit var quadrantViewMoodHunger: CustomQuadrantView

    private var lastGeneratedRestaurants: List<Restaurant>? = null
    private var initialTouchX = 0f
    private var initialCardTranslationX = 0f
    private val swipeThreshold = 150f
    private val cardAnimationDuration = 200L
    private val rotationMultiplier = 0.1f
    private val maxRotationAngle = 15f

    // --- State for mood selection persistence ---
    private var moodSelectionCompletedThisSession = false
    companion object {
        private const val KEY_MOOD_SELECTION_COMPLETED = "mood_selection_completed"
    }
    // --- End State ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            moodSelectionCompletedThisSession = it.getBoolean(KEY_MOOD_SELECTION_COMPLETED, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_lazy_mode, container, false)
        viewModel = ViewModelProvider(this)[LazyModeViewModel::class.java]

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
        quadrantViewMoodHunger = view.findViewById(R.id.quadrant_view_mood_hunger)

        // Initial visibility setup - will be adjusted by observeViewModel
        noMoreCardsContainer.isVisible = false
        quadrantViewMoodHunger.isVisible = false

        setupTouchListener()
        setupClickListeners()
        setupQuadrantViewListener()
        observeViewModel()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuadrantViewColors()
        // If returning and mood selection was done, UI update might be needed here
        // or ensure observeViewModel handles it correctly based on restored state.
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_MOOD_SELECTION_COMPLETED, moodSelectionCompletedThisSession)
    }

    private fun setupQuadrantViewColors() {
        if (!::quadrantViewMoodHunger.isInitialized) return

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            quadrantViewMoodHunger.setAxisColor(ContextCompat.getColor(requireContext(), R.color.quadrant_axis_color_dark))
            quadrantViewMoodHunger.setGridColor(ContextCompat.getColor(requireContext(), R.color.quadrant_grid_color_dark))
            quadrantViewMoodHunger.setTextColor(ContextCompat.getColor(requireContext(), R.color.quadrant_text_color_dark))
            quadrantViewMoodHunger.setSelectedPointColor(ContextCompat.getColor(requireContext(), R.color.quadrant_selected_point_color_dark))
            quadrantViewMoodHunger.setCenterPointColor(ContextCompat.getColor(requireContext(), R.color.quadrant_center_point_color_dark))
        } else {
            quadrantViewMoodHunger.setAxisColor(ContextCompat.getColor(requireContext(), R.color.quadrant_axis_color_light))
            quadrantViewMoodHunger.setGridColor(ContextCompat.getColor(requireContext(), R.color.quadrant_grid_color_light))
            quadrantViewMoodHunger.setTextColor(ContextCompat.getColor(requireContext(), R.color.quadrant_text_color_light))
            quadrantViewMoodHunger.setSelectedPointColor(ContextCompat.getColor(requireContext(), R.color.quadrant_selected_point_color_light))
            quadrantViewMoodHunger.setCenterPointColor(ContextCompat.getColor(requireContext(), R.color.quadrant_center_point_color_light))
        }
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
    }

    private fun setupQuadrantViewListener() {
        quadrantViewMoodHunger.setOnQuadrantSelectedListener(object : CustomQuadrantView.OnQuadrantSelectedListener {
            override fun onQuadrantSelected(x: Float, y: Float) {
                Log.d("LazyModeFragment", "Quadrant selected: X=$x, Y=$y")
                Toast.makeText(context, "狀態已記錄: 飢餓度 $x, 開心度 $y", Toast.LENGTH_SHORT).show()
                
                moodSelectionCompletedThisSession = true // Mark mood selection as done for this session

                quadrantViewMoodHunger.isVisible = false
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
            // moodSelectionCompletedThisSession should also guard quadrant visibility here.
            val shouldShowQuadrant = isCompletionActive && !moodSelectionCompletedThisSession 

            if (foodTypeCard != null && !isCompletionActive) { // Card is primary if not in completion
                updateFoodTypeCardUI(foodTypeCard)
                foodCardView.isVisible = true
                likeButton.isVisible = true
                dislikeButton.isVisible = true
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
            } else if (!isCompletionActive) { // No card, not in completion
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
            }
            // Visibility for completion state (quadrant or recommendations) is handled by showCompletionMessage observer
        }

        viewModel.showCompletionMessage.observe(viewLifecycleOwner) { show ->
            if (show) { // Card swiping is complete
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false

                val isLoggedIn = true // Placeholder
                if (isLoggedIn) {
                    if (!moodSelectionCompletedThisSession) {
                        Log.d("LazyModeFragment", "Completion: Showing mood/hunger quadrant view.")
                        noMoreCardsContainer.isVisible = false
                        quadrantViewMoodHunger.setInitialPoint(0f, 0f)
                        quadrantViewMoodHunger.isVisible = true
                    } else {
                        Log.d("LazyModeFragment", "Completion: Mood already selected. Showing recommendation options.")
                        quadrantViewMoodHunger.isVisible = false
                        showRecommendationOptions("您已完成心情選擇，請查看推薦！")
                    }
                } else {
                    Log.d("LazyModeFragment", "Completion: Not logged in. Showing recommendation options directly.")
                    quadrantViewMoodHunger.isVisible = false
                    showRecommendationOptions("感謝您的選擇！準備好查看為您量身打造的推薦了嗎？")
                }
            } else { // Not in completion message state (e.g., new card loaded or fragment start)
                moodSelectionCompletedThisSession = false // Reset for next completion cycle
                
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
                // Visibility of card elements is handled by currentFoodTypeCard observer
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
        if (lastGeneratedRestaurants == null) {
            val preferences = viewModel.foodTypePreferencesCollected.value ?: emptyList()
            Log.d("LazyModeFragment", "Generating recommendations with prefs: $preferences")
            lastGeneratedRestaurants = generateSimulatedLlamaResponse(preferences)
        }
        Log.d("LazyModeFragment", "Recommendations available: ${lastGeneratedRestaurants?.size ?: 0} items.")
    }

    private fun generateSimulatedLlamaResponse(preferences: List<Pair<String, Boolean>>): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val baseCount = 20
        val prefText = if (preferences.isEmpty()) "通用" else preferences.joinToString { it.first + ":" + if (it.second) "喜歡" else "不喜歡" }
        for (i in 1..baseCount) {
            restaurants.add(
                Restaurant(
                    id = "llama_res_lazy_mood_$i",
                    name = "AI推薦餐廳 #$i (基於心情)",
                    photoUrl = "https://picsum.photos/seed/restaurant_mood_$i/300/200",
                    cuisine = if (i % 3 == 0) "特色川菜" else if (i % 3 == 1) "創意日料" else "溫馨義式",
                    briefDescription = "這是由AI根據您的 ($prefText) 偏好及當前狀態特別推薦的第 $i 家餐廳。",
                    isFavorite = false,
                    latitude = 25.0330 + (i * 0.0012),
                    longitude = 121.5654 + (i * 0.0012)
                )
            )
        }
        return restaurants.shuffled().take(10)
    }

    private fun updateFoodTypeCardUI(foodTypeCard: FoodTypeCard) {
        foodNameTextView.text = foodTypeCard.name
        foodDescriptionTextView.text = foodTypeCard.description
        try {
            foodImageView.setImageResource(foodTypeCard.imageResId)
        } catch (e: Exception) {
            Log.e("LazyModeFragment", "Error setting image resource for food type card: ${foodTypeCard.id}", e)
            foodImageView.setImageResource(R.drawable.ic_menu_gallery)
        }
    }

    override fun onResume() {
        super.onResume()
        // Visibility is largely handled by LiveData observers now,
        // especially considering the new moodSelectionCompletedThisSession state.
        // If showCompletionMessage is true and moodSelectionCompletedThisSession is true,
        // observeViewModel should correctly show recommendations.
        // If showCompletionMessage is false, it should show card or empty state.
    }
}
