package com.fatefulsupper.app.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Context
import android.location.LocationManager
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
// import android.widget.LinearLayout // Removed: noMoreCardsContainer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.fatefulsupper.app.ui.custom.CustomQuadrantView
import com.fatefulsupper.app.util.NotificationScheduler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.card.MaterialCardView


import com.fatefulsupper.app.data.model.Restaurant
import com.fatefulsupper.app.ui.custom.CustomQuadrantView
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private var hasShownSystemLocationOffToast = false // Used in fetchDeviceLocation
    private var rationaleShownThisSession = false
    private var permissionRequestAttemptedThisSession = false

    private val TAG = "LazyModeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            when {
                fineLocationGranted || coarseLocationGranted -> {
                    Log.d(TAG, "Location permission granted.")
                    // Removed Toast for permission granted - UX improvement
                    hasShownSystemLocationOffToast = false // Reset this flag as permission state might have changed
                    rationaleShownThisSession = false
                    permissionRequestAttemptedThisSession = false
                    fetchDeviceLocation()
                }
                else -> {
                    // Keep Toast for permission not granted
                    Toast.makeText(requireContext(), getString(R.string.toast_location_permission_not_granted), Toast.LENGTH_LONG).show()
                    // Example R.string.toast_location_permission_not_granted: "位置權限未授予。部分功能可能受限。"
                }
            }
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
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_lazy_mode, container, false)
        viewModel = ViewModelProvider(requireActivity())[LazyModeViewModel::class.java]
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
        Log.d(TAG, "onViewCreated")
        setupQuadrantViewColors()
        rationaleShownThisSession = false // Reset on view creation
    }

    private fun setupQuadrantViewColors() {
        // Optional styling
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

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners")
        likeButton.setOnClickListener {
            animateCardSwipe(true)
        }
        dislikeButton.setOnClickListener {
            animateCardSwipe(false)
        }
    }

    private fun animateCardSwipe(liked: Boolean) {
        val translationXEnd = if (liked) foodCardView.width.toFloat() * 1.5f else -foodCardView.width.toFloat() * 1.5f
        foodCardView.animate()
            .translationX(translationXEnd)
            .alpha(0f)
            .setDuration(cardAnimationDuration)
            .withEndAction {
                foodCardView.translationX = 0f
                foodCardView.alpha = 1f
                foodCardView.rotation = 0f
                viewModel.onFoodTypeCardSwiped(liked)
            }
            .start()
    }

    private fun setupQuadrantViewListener() {
        Log.d(TAG, "setupQuadrantViewListener CALLED")
        quadrantViewMoodHunger.setOnQuadrantSelectedListener(object : CustomQuadrantView.OnQuadrantSelectedListener {
            override fun onQuadrantSelected(x: Float, y: Float) {
                val moodValue = y
                val hungerValue = x
                val moodDescription = if (moodValue > 0.5) getString(R.string.mood_happy) else if (moodValue < -0.5) getString(R.string.mood_unhappy) else getString(R.string.mood_neutral)
                val hungerDescription = if (hungerValue > 0.5) getString(R.string.hunger_hungry) else if (hungerValue < -0.5) getString(R.string.hunger_not_hungry) else getString(R.string.hunger_neutral)
                // Example R.string.mood_happy: "開心"
                // Example R.string.hunger_hungry: "飢餓"

                Log.d(TAG, "Quadrant SELECTED! Raw X (Hunger): $x, Raw Y (Mood): $y")
                Log.d(TAG, "Interpreted - Mood: $moodDescription ($moodValue), Hunger: $hungerDescription ($hungerValue)")
                // Keep this Toast as it's direct feedback to user's selection
                Toast.makeText(requireContext(), getString(R.string.toast_mood_hunger_selected, moodDescription, hungerDescription), Toast.LENGTH_SHORT).show()
                // Example R.string.toast_mood_hunger_selected: "已選擇 心情: %1$s, 飢餓度: %2$s"

                Log.d(TAG, "Triggering fetchRecommendations from quadrant selection.")
                viewModel.fetchRecommendations(hungerValue.toString(), moodValue.toString())
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v?.id == R.id.cardView_food_item) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialCardTranslationX = foodCardView.translationX
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    foodCardView.translationX = initialCardTranslationX + dx
                    val rotation = dx * rotationMultiplier
                    foodCardView.rotation = rotation.coerceIn(-maxRotationAngle, maxRotationAngle)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val finalDx = event.rawX - initialTouchX
                    if (finalDx > swipeThreshold) {
                        animateCardSwipe(true)
                    } else if (finalDx < -swipeThreshold) {
                        animateCardSwipe(false)
                    } else {
                        foodCardView.animate().translationX(0f).rotation(0f).setDuration(cardAnimationDuration).start()
                    }
                }
            }
            return true
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

    private fun updateUiForCurrentState() {
        val currentCard = viewModel.currentFoodTypeCard.value
        val showCompletionMessage = viewModel.showCompletionMessage.value ?: false
        Log.d(TAG, "updateUiForCurrentState. Card: ${currentCard?.name}, CompletionMsg: $showCompletionMessage")

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

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel")
        viewModel.currentFoodTypeCard.observe(viewLifecycleOwner) { updateUiForCurrentState() }
        viewModel.showCompletionMessage.observe(viewLifecycleOwner) { updateUiForCurrentState() }
        viewModel.navigationHasBeenHandled.observe(viewLifecycleOwner) { Log.d(TAG, "navigationHasBeenHandled obs. Value: $it") }
        viewModel.navigateToLoadingEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Log.d(TAG, "navigateToLoadingEvent observed, navigating to LoadingFragment.")
                try {
                    findNavController().navigate(R.id.action_lazyModeFragment_to_loadingFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to LoadingFragment failed", e)
                    // Keep this Toast - essential error feedback
                    Toast.makeText(requireContext(), getString(R.string.toast_navigation_to_loading_failed), Toast.LENGTH_SHORT).show()
                    // Example R.string.toast_navigation_to_loading_failed: "無法開始載入推薦"
                }
            }
        }
        viewModel.foodTypePreferencesCollected.observe(viewLifecycleOwner) { preferences ->
            if (preferences != null && !preferences.isEmpty()) {
                Log.d(TAG, "Food type preferences collected: ${preferences.size} items.")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted for fetchDeviceLocation.");
            return; // Permission check should happen before calling this or handled by the callback
        }

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            if (!hasShownSystemLocationOffToast) {
                // This Toast is for cases where system location is off.
                // NotificationScheduler.checkLocationServices() in onResume handles the primary user-facing prompt.
                // This acts as a fallback or if checkLocationServices was bypassed/failed.
                val message = if (locationManager == null) getString(R.string.toast_location_manager_unavailable) else getString(R.string.toast_location_services_disabled_prompt)
                // Example R.string.toast_location_manager_unavailable: "無法偵測到定位服務"
                // Example R.string.toast_location_services_disabled_prompt: "請先開啟系統定位服務以獲取位置"
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                hasShownSystemLocationOffToast = true
            }
            return
        }
        // If we reach here, system services are considered on for this attempt.
        // Reset the flag if it was previously set by this fragment,
        // allowing NotificationScheduler or subsequent checks to show their specific toasts if needed.
        hasShownSystemLocationOffToast = false 
        Log.d(TAG, "Attempting to get current location.")
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "Current Location Acquired: Lat ${location.latitude}, Lon ${location.longitude}")
                    // Removed Toast showing lat/lon - UX improvement
                } else {
                    Log.d(TAG, "Current location is null, trying last known location.")
                    // Removed Toast for "trying last known location" - UX improvement
                    fetchLastKnownLocation()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting current location", exception)
                // Use generic failure Toast
                Toast.makeText(requireContext(), getString(R.string.toast_location_fetch_failed), Toast.LENGTH_LONG).show()
                // Example R.string.toast_location_fetch_failed: "獲取位置失敗，請檢查定位服務或稍後重試。"
                fetchLastKnownLocation() // Still try last known on failure
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownLocation() {
         if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted for fetchLastKnownLocation.");
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "Last known location acquired: Lat ${location.latitude}, Lon ${location.longitude}")
                    // Removed Toast showing lat/lon - UX improvement
                } else {
                    Log.d(TAG, "Last known location is also null.")
                    // Use generic failure Toast
                    Toast.makeText(requireContext(), getString(R.string.toast_location_fetch_failed), Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting last known location", exception)
                 // Use generic failure Toast
                Toast.makeText(requireContext(), getString(R.string.toast_location_fetch_failed), Toast.LENGTH_LONG).show()
            }
    }

    private fun requestLocationPermissions() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d(TAG, "Location permissions already granted.")
            hasShownSystemLocationOffToast = false // Reset this as permissions are granted
            rationaleShownThisSession = false
            permissionRequestAttemptedThisSession = false
            fetchDeviceLocation()
        } else {
            // Rationale and request logic - Toasts here are kept as they are important for UX
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (!rationaleShownThisSession) {
                    Toast.makeText(requireContext(), getString(R.string.toast_location_permission_rationale), Toast.LENGTH_LONG).show()
                    // Example R.string.toast_location_permission_rationale: "位置權限為推薦附近餐廳所必需，請考慮授予。"
                    rationaleShownThisSession = true
                    locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionRequestAttemptedThisSession = true
                }
            } else {
                if (!permissionRequestAttemptedThisSession) {
                    Log.d(TAG, "Requesting location permissions without prior rationale shown this session.")
                    locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionRequestAttemptedThisSession = true
                } else {
                    // Previously requested, and no rationale to show -> likely "don't ask again"
                    if (!rationaleShownThisSession) { // Check rationaleShownThisSession to ensure this specific message is shown only once if this path is hit multiple times
                         Toast.makeText(requireContext(), getString(R.string.toast_location_permission_denied_permanently), Toast.LENGTH_LONG).show()
                         // Example R.string.toast_location_permission_denied_permanently: "位置權限先前已被永久拒絕。如需使用相關功能，請至應用程式設定開啟。"
                         rationaleShownThisSession = true // Use rationaleShownThisSession to prevent this from repeating if onResume is called multiple times without state change
                    }
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume. navHandled: ${viewModel.navigationHasBeenHandled.value}")
        NotificationScheduler.checkLocationServices(requireContext()) // Handles its own more specific system-level location service Toasts/Dialogs
        requestLocationPermissions() // Handles permission requests and related Toasts

        if (viewModel.navigationHasBeenHandled.value == true) {
            Log.d(TAG, "onResume: Navigation was previously handled, resetting LazyMode state.")
            viewModel.resetLazyModeState()
        } else {
            updateUiForCurrentState()
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
