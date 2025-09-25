package com.fatefulsupper.app.ui.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant
import com.google.android.material.appbar.CollapsingToolbarLayout

class RestaurantDetailsFragment : Fragment() {

    private lateinit var viewModel: RestaurantDetailsViewModel
    private val args: RestaurantDetailsFragmentArgs by navArgs()

    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var restaurantImageView: ImageView
    private lateinit var restaurantNameTextView: TextView
    private lateinit var restaurantCuisineTextView: TextView
    private lateinit var restaurantDescriptionTextView: TextView
    private lateinit var favoriteToggleButton: ImageButton
    private lateinit var getDirectionsButton: Button
    private lateinit var spinAgainButton: Button // Added this line

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_details, container, false)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(RestaurantDetailsViewModel::class.java)

        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar_details)
        toolbar = view.findViewById(R.id.toolbar_details)
        restaurantImageView = view.findViewById(R.id.imageView_restaurant_detail_photo)
        restaurantNameTextView = view.findViewById(R.id.textView_restaurant_detail_name)
        restaurantCuisineTextView = view.findViewById(R.id.textView_restaurant_detail_cuisine)
        restaurantDescriptionTextView = view.findViewById(R.id.textView_restaurant_detail_description)
        favoriteToggleButton = view.findViewById(R.id.imageButton_favorite_toggle)
        getDirectionsButton = view.findViewById(R.id.button_get_directions)
        spinAgainButton = view.findViewById(R.id.button_spin_again) // Added this line

        setupToolbar()
        observeViewModel()
        setupClickListeners() // Original click listeners
        // Specific click listener for spinAgainButton will be in onViewCreated based on condition

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val fullRestaurantData = args.selectedRestaurantFull
        if (fullRestaurantData != null) {
            Log.d("RestaurantDetails", "Displaying details from passed Restaurant object: ${fullRestaurantData.name}")
            updateUiContent(fullRestaurantData)
            viewModel.setLoadedRestaurantDetails(fullRestaurantData) 
        } else {
            Log.d("RestaurantDetails", "selectedRestaurantFull is null, loading details by ID: ${args.restaurantId}")
            viewModel.loadRestaurantDetails(args.restaurantId)
        }

        // --- Logic for spinAgainButton ---
        if (args.sourceIsRoulette) {
            spinAgainButton.visibility = View.VISIBLE
            spinAgainButton.setOnClickListener {
                findNavController().popBackStack()
            }
        } else {
            spinAgainButton.visibility = View.GONE
        }
        // --- End of logic for spinAgainButton ---
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true) 
    }

    private fun setupClickListeners() {
        favoriteToggleButton.setOnClickListener {
            viewModel.toggleFavoriteStatus()
        }
        // getDirectionsButton listener is set in updateUiContent based on data
    }

    private fun observeViewModel() {
        viewModel.restaurant.observe(viewLifecycleOwner) { restaurant ->
            if (restaurant != null) {
                if (args.selectedRestaurantFull == null) { 
                    Log.d("RestaurantDetails", "Observer updating UI from ViewModel for restaurant: ${restaurant.name}")
                    updateUiContent(restaurant)
                }
            } else {
                if (args.selectedRestaurantFull == null) { 
                    Toast.makeText(context, "Restaurant details could not be loaded.", Toast.LENGTH_LONG).show()
                    Log.e("RestaurantDetails", "Restaurant from ViewModel is null, and no direct data was passed.")
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.isCurrentRestaurantFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButtonVisual(isFavorite)
        }
    }

    private fun updateUiContent(restaurant: Restaurant) {
        collapsingToolbarLayout.title = restaurant.name
        restaurantNameTextView.text = restaurant.name
        restaurantCuisineTextView.text = restaurant.cuisine ?: "N/A"
        restaurantDescriptionTextView.text = restaurant.fullDescription ?: restaurant.briefDescription ?: "No description available."

        val imageUrl = restaurant.photoUrl

        Glide.with(this)
            .load(imageUrl) // Load the actual URL, even if null or empty
            .placeholder(R.drawable.place_holder)   // Your placeholder image
            .error(R.drawable.img_loading_error)       // Your error image
            .centerCrop()
            .into(restaurantImageView)

        if (restaurant.latitude != null && restaurant.longitude != null) {
            getDirectionsButton.setOnClickListener {
                val action = RestaurantDetailsFragmentDirections.actionRestaurantDetailsFragmentToMapDirectionFragment(
                    restaurant.latitude.toFloat(),
                    restaurant.longitude.toFloat(),
                    restaurant.name
                )
                findNavController().navigate(action)
            }
            getDirectionsButton.visibility = View.VISIBLE
        } else {
            getDirectionsButton.visibility = View.GONE
        }
    }

    private fun updateFavoriteButtonVisual(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteToggleButton.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            favoriteToggleButton.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Check if we should pop to RouletteFragment or just regular pop
                // If sourceIsRoulette is true, and we are at RestaurantDetailsFragment,
                // a normal popBackStack() might go to RouletteFragment if it's the immediate previous.
                // However, if the user navigated further from details (e.g. to map) and then back,
                // this simple pop might not be enough or might be too much.
                // For now, simple popBackStack is fine as per original logic.
                // If more complex back navigation from "Spin Again" source is needed,
                // we might need to pop up to a specific destination.
                findNavController().popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
