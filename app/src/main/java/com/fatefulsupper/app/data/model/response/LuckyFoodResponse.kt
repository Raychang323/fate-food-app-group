package com.fatefulsupper.app.data.model.response

import com.google.gson.annotations.SerializedName

data class LuckyFoodResponse(
    val places: List<Place>
)

data class Place(
    val id: String,
    val displayName: DisplayName,
    val formattedAddress: String,
    @SerializedName("location")
    val apiLocation: ApiLocation,
    val types: List<String>,
    val rating: Double?
)

data class DisplayName(
    val text: String
)

data class ApiLocation(
    val latitude: Double,
    val longitude: Double
)
