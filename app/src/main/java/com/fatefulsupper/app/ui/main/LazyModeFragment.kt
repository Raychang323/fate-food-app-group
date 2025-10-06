package com.fatefulsupper.app.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.FoodTypeCard
import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.ui.custom.CustomQuadrantView
import com.fatefulsupper.app.util.NotificationScheduler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.card.MaterialCardView

class LazyModeFragment : Fragment(), View.OnTouchListener {

    private lateinit var viewModel: LazyModeViewModel

    private lateinit var foodCardView: MaterialCardView
    private lateinit var foodImageView: ImageView
    private lateinit var foodNameTextView: TextView
    private lateinit var foodDescriptionTextView: TextView
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button
    private lateinit var quadrantViewMoodHunger: CustomQuadrantView
    private lateinit var textViewStatusMessage: TextView
    private lateinit var noMoreCardsContainer: LinearLayout
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private var hasShownSystemLocationOffToast = false
    private var rationaleShownThisSession = false
    private var permissionRequestAttemptedThisSession = false

    private val TAG = "LazyModeFragment"
    private var moodSelectionCompletedThisSession = false

    companion object {
        private const val KEY_MOOD_SELECTION_COMPLETED = "mood_selection_completed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            when {
                fineLocationGranted || coarseLocationGranted -> {
                    hasShownSystemLocationOffToast = false
                    rationaleShownThisSession = false
                    permissionRequestAttemptedThisSession = false
                    fetchDeviceLocation()
                }
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.toast_location_permission_not_granted), Toast.LENGTH_LONG).show()
                }
            }
        }

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
        likeButton = view.findViewById(R.id.button_like)
        dislikeButton = view.findViewById(R.id.button_dislike)
        quadrantViewMoodHunger = view.findViewById(R.id.quadrant_view_mood_hunger)
        textViewStatusMessage = view.findViewById(R.id.textView_status_message_lazy_mode)
        noMoreCardsContainer = view.findViewById(R.id.linearLayout_no_more_cards_container)
        completionMessageTextView = view.findViewById(R.id.textView_completion_message)
        getRecommendationsButton = view.findViewById(R.id.button_get_recommendations)
        buttonGoToRoulette = view.findViewById(R.id.button_go_to_roulette_from_lazy)

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
        rationaleShownThisSession = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_MOOD_SELECTION_COMPLETED, moodSelectionCompletedThisSession)
    }

    @SuppressLint("ClickableViewAccessibility")
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

    override fun onResume() {
        super.onResume()
        NotificationScheduler.checkLocationServices(requireContext())
        requestLocationPermissions()

        if (viewModel.navigationHasBeenHandled.value == true) {
            viewModel.resetLazyModeState()
        } else {
            updateUiForCurrentState()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        foodCardView.setOnTouchListener(this)
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
                val moodValue = y
                val hungerValue = x
                val moodDescription = if (moodValue > 0.5) getString(R.string.mood_happy) else if (moodValue < -0.5) getString(R.string.mood_unhappy) else getString(R.string.mood_neutral)
                val hungerDescription = if (hungerValue > 0.5) getString(R.string.hunger_hungry) else if (hungerValue < -0.5) getString(R.string.hunger_not_hungry) else getString(R.string.hunger_neutral)
                
                Toast.makeText(requireContext(), getString(R.string.toast_mood_hunger_selected, moodDescription, hungerDescription), Toast.LENGTH_SHORT).show()
                
                viewModel.fetchRecommendations(hungerValue.toString(), moodValue.toString())
            }
        })
    }

    private fun observeViewModel() {
        viewModel.currentFoodTypeCard.observe(viewLifecycleOwner) { foodTypeCard ->
            foodCardView.animate().cancel()
            foodCardView.translationX = 0f
            foodCardView.alpha = 1f
            foodCardView.rotation = 0f

            val isCompletionActive = viewModel.showCompletionMessage.value == true
            val shouldShowQuadrant = isCompletionActive && !moodSelectionCompletedThisSession

            if (foodTypeCard != null && !isCompletionActive) {
                updateFoodTypeCardUI(foodTypeCard)
                foodCardView.isVisible = true
                likeButton.isVisible = true
                dislikeButton.isVisible = true
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
            } else if (!isCompletionActive) {
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
            }
        }

        viewModel.showCompletionMessage.observe(viewLifecycleOwner) { show ->
            if (show) {
                foodCardView.isVisible = false
                likeButton.isVisible = false
                dislikeButton.isVisible = false

                val isLoggedIn = true // Placeholder
                if (isLoggedIn) {
                    if (!moodSelectionCompletedThisSession) {
                        noMoreCardsContainer.isVisible = false
                        quadrantViewMoodHunger.setInitialPoint(0f, 0f)
                        quadrantViewMoodHunger.isVisible = true
                    } else {
                        quadrantViewMoodHunger.isVisible = false
                        showRecommendationOptions("您已完成心情選擇，請查看推薦！")
                    }
                } else {
                    quadrantViewMoodHunger.isVisible = false
                    showRecommendationOptions("感謝您的選擇！準備好查看為您量身打造的推薦了嗎？")
                }
            } else {
                moodSelectionCompletedThisSession = false
                quadrantViewMoodHunger.isVisible = false
                noMoreCardsContainer.isVisible = false
                val hasCurrentCard = viewModel.currentFoodTypeCard.value != null
                foodCardView.isVisible = hasCurrentCard
                likeButton.isVisible = hasCurrentCard
                dislikeButton.isVisible = hasCurrentCard
            }
        }

        viewModel.navigateToLoadingEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                try {
                    findNavController().navigate(R.id.action_lazyModeFragment_to_loadingFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.toast_navigation_to_loading_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.foodTypePreferencesCollected.observe(viewLifecycleOwner) { preferences ->
            if (preferences != null && preferences.isNotEmpty()) {
                Log.d(TAG, "Food type preferences collected: ${preferences.size} items.")
            }
        }
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

    private fun updateUiForCurrentState() {
        val currentCard = viewModel.currentFoodTypeCard.value
        val showCompletionMessage = viewModel.showCompletionMessage.value ?: false

        if (currentCard != null && !showCompletionMessage) {
            foodCardView.isVisible = true
            likeButton.isVisible = true
            dislikeButton.isVisible = true
            quadrantViewMoodHunger.isVisible = false
            textViewStatusMessage.isVisible = false
            foodNameTextView.text = currentCard.name
            foodDescriptionTextView.text = currentCard.description
            if (currentCard.imageResId != 0 && currentCard.imageResId != -1) {
                foodImageView.setImageResource(currentCard.imageResId)
            } else {
                foodImageView.setImageDrawable(null)
            }
        } else {
            foodCardView.isVisible = false
            likeButton.isVisible = false
            dislikeButton.isVisible = false
            quadrantViewMoodHunger.isVisible = true
            textViewStatusMessage.isVisible = true
            if (viewModel.masterFoodItemsList.isEmpty() || (viewModel.foodTypeCards.isEmpty() && viewModel.showCompletionMessage.value == true)) {
                textViewStatusMessage.text = getString(R.string.lazy_mode_no_cards_available_prompt)
            } else if (showCompletionMessage) {
                textViewStatusMessage.text = getString(R.string.lazy_mode_completion_message_mood_prompt)
            } else {
                textViewStatusMessage.text = getString(R.string.lazy_mode_initial_prompt_mood_hunger)
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
            lastGeneratedRestaurants = generateSimulatedLlamaResponse(preferences)
        }
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
            foodImageView.setImageResource(R.drawable.ic_menu_gallery)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            if (!hasShownSystemLocationOffToast) {
                val message = if (locationManager == null) getString(R.string.toast_location_manager_unavailable) else getString(R.string.toast_location_services_disabled_prompt)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                hasShownSystemLocationOffToast = true
            }
            return
        }

        hasShownSystemLocationOffToast = false 
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Location acquired
                } else {
                    fetchLastKnownLocation()
                }
            }
            .addOnFailureListener { 
                fetchLastKnownLocation()
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownLocation() {
         if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { 
                // Last known location acquired
            }
            .addOnFailureListener { 
                // Failed to get last known location
            }
    }

    private fun requestLocationPermissions() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            hasShownSystemLocationOffToast = false
            rationaleShownThisSession = false
            permissionRequestAttemptedThisSession = false
            fetchDeviceLocation()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (!rationaleShownThisSession) {
                    Toast.makeText(requireContext(), getString(R.string.toast_location_permission_rationale), Toast.LENGTH_LONG).show()
                    rationaleShownThisSession = true
                    locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionRequestAttemptedThisSession = true
                }
            } else {
                if (!permissionRequestAttemptedThisSession) {
                    locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionRequestAttemptedThisSession = true
                } else {
                    if (!rationaleShownThisSession) {
                         Toast.makeText(requireContext(), getString(R.string.toast_location_permission_denied_permanently), Toast.LENGTH_LONG).show()
                         rationaleShownThisSession = true
                    }
                }
            }
        }
    }
}
