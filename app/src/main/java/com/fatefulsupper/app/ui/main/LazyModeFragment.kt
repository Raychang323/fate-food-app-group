package com.fatefulsupper.app.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Context
import android.location.LocationManager
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_lazy_mode, container, false)
        viewModel = ViewModelProvider(requireActivity())[LazyModeViewModel::class.java]

        foodCardView = view.findViewById(R.id.cardView_food_item)
        foodImageView = view.findViewById(R.id.imageView_food_card_placeholder)
        foodNameTextView = view.findViewById(R.id.textView_food_card_name)
        foodDescriptionTextView = view.findViewById(R.id.textView_food_card_description)
        likeButton = view.findViewById(R.id.button_like)
        dislikeButton = view.findViewById(R.id.button_dislike)
        quadrantViewMoodHunger = view.findViewById(R.id.quadrant_view_mood_hunger)
        textViewStatusMessage = view.findViewById(R.id.textView_status_message_lazy_mode)

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
    }
}
