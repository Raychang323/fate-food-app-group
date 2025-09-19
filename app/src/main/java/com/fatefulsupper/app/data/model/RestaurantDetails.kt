package com.fatefulsupper.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RestaurantDetails(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val cuisine: String? = null,
    val detailedDescription: String? = null,
    val rating: Double? = null,
    val priceRange: String? = null, // e.g., "$", "$$", "$$$"
    val hours: Map<String, String>? = null, // e.g., {"Monday": "9am-10pm", ...}
    val websiteUrl: String? = null,
    val latitude: Double,
    val longitude: Double,
    val reviews: List<String>? = null, // Or a more complex Review object
    var isFavorite: Boolean = false
    // Add any other detailed fields required
) : Parcelable
