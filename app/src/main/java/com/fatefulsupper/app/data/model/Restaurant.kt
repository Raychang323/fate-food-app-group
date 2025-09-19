package com.fatefulsupper.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Restaurant(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val cuisine: String? = null,
    val briefDescription: String? = null,
    val fullDescription: String? = null, // Added for more detail if needed
    val address: String? = null,
    val phoneNumber: String? = null,
    val openingHours: String? = null,
    val rating: Float? = null, // e.g., 4.5f
    val latitude: Double? = null,
    val longitude: Double? = null,
    var isFavorite: Boolean = false // Keep this for user's own favorites tracking
) : Parcelable
