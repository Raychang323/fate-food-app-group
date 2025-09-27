package com.fatefulsupper.app.ui.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var restaurantImageView: ImageView
    private lateinit var restaurantNameTextView: TextView
    private lateinit var restaurantCuisineTextView: TextView
    private lateinit var restaurantDescriptionTextView: TextView
    private lateinit var favoriteToggleButton: ImageButton
    private lateinit var getDirectionsButton: Button
    private lateinit var spinAgainButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_details, container, false)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[RestaurantDetailsViewModel::class.java]

        collapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar_details)
        restaurantImageView = view.findViewById(R.id.imageView_restaurant_detail_photo)
        restaurantNameTextView = view.findViewById(R.id.textView_restaurant_detail_name)
        restaurantCuisineTextView = view.findViewById(R.id.textView_restaurant_detail_cuisine)
        restaurantDescriptionTextView = view.findViewById(R.id.textView_restaurant_detail_description)
        favoriteToggleButton = view.findViewById(R.id.imageButton_favorite_toggle)
        getDirectionsButton = view.findViewById(R.id.button_get_directions)
        spinAgainButton = view.findViewById(R.id.button_spin_again)

        observeViewModel()
        setupClickListeners()

        return view
    }

    // Simplified private function to handle CollapsingToolbarLayout setup (no title parameter)
    private fun internalSetupCollapsingToolbar() {
        this.collapsingToolbarLayout.title = "" // Set title to empty
        this.collapsingToolbarLayout.isTitleEnabled = false // Directly disable title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullRestaurantData = args.selectedRestaurantFull
        if (fullRestaurantData != null) {
            Log.d("RestaurantDetails", "Displaying from passed: ${fullRestaurantData.name}")
            updateUiContent(fullRestaurantData)
            viewModel.setLoadedRestaurantDetails(fullRestaurantData)
            internalSetupCollapsingToolbar() // Call without arguments
        } else {
            Log.d("RestaurantDetails", "Loading by ID: ${args.restaurantId}")
            viewModel.loadRestaurantDetails(args.restaurantId)
        }

        if (args.sourceIsRoulette) {
            spinAgainButton.visibility = View.VISIBLE
            spinAgainButton.setOnClickListener {
                findNavController().popBackStack()
            }
        } else {
            spinAgainButton.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewModel.restaurant.observe(viewLifecycleOwner) { restaurant ->
            if (restaurant != null) {
                updateUiContent(restaurant)
                if (args.selectedRestaurantFull == null) { 
                    internalSetupCollapsingToolbar() // Call without arguments
                }
            } else {
                if (args.selectedRestaurantFull == null) {
                    Toast.makeText(context, "Restaurant details could not be loaded.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.isCurrentRestaurantFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButtonVisual(isFavorite)
        }
    }

    private fun updateUiContent(restaurant: Restaurant) {
        restaurantNameTextView.text = restaurant.name
        restaurantCuisineTextView.text = restaurant.cuisine ?: "N/A"
        restaurantDescriptionTextView.text = restaurant.fullDescription ?: restaurant.briefDescription ?: "No description available."

        Glide.with(this)
            .load(restaurant.photoUrl)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.img_loading_error)
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

    private fun setupClickListeners() {
        favoriteToggleButton.setOnClickListener {
            viewModel.toggleFavoriteStatus()
        }
    }

    private fun updateFavoriteButtonVisual(isFavorite: Boolean) {
        if (isFavorite) {
            favoriteToggleButton.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            favoriteToggleButton.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
}
