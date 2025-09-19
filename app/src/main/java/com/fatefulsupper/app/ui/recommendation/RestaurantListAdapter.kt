package com.fatefulsupper.app.ui.recommendation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant
import com.bumptech.glide.Glide // Import Glide

class RestaurantListAdapter(private val onItemClicked: (Restaurant) -> Unit) :
    ListAdapter<Restaurant, RestaurantListAdapter.RestaurantViewHolder>(RestaurantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = getItem(position)
        holder.bind(restaurant)
        holder.itemView.setOnClickListener {
            onItemClicked(restaurant)
        }
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textView_restaurant_item_name)
        private val cuisineTextView: TextView = itemView.findViewById(R.id.textView_restaurant_item_cuisine)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textView_restaurant_item_description)
        private val photoImageView: ImageView = itemView.findViewById(R.id.imageView_restaurant_item_photo)

        fun bind(restaurant: Restaurant) {
            nameTextView.text = restaurant.name
            cuisineTextView.text = restaurant.cuisine ?: ""
            descriptionTextView.text = restaurant.briefDescription ?: ""

            if (restaurant.photoUrl != null && restaurant.photoUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(restaurant.photoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Using Android built-in icon
                    .error(android.R.drawable.ic_menu_report_image)   // Using Android built-in icon
                    .centerCrop()
                    .into(photoImageView)
            } else {
                Glide.with(itemView.context)
                    .load(android.R.drawable.ic_menu_report_image) // Using Android built-in icon for no image
                    .centerCrop()
                    .into(photoImageView)
            }
            cuisineTextView.visibility = if (restaurant.cuisine.isNullOrBlank()) View.GONE else View.VISIBLE
            descriptionTextView.visibility = if (restaurant.briefDescription.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }
}

class RestaurantDiffCallback : DiffUtil.ItemCallback<Restaurant>() {
    override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
        return oldItem == newItem
    }
}
