package com.fatefulsupper.app.data.model

import androidx.annotation.DrawableRes

/**
 * Represents a food type card for the user to swipe in LazyModeFragment.
 */
data class FoodTypeCard(
    val id: String, // Unique identifier for the food type (e.g., "sushi", "pasta", "spicy")
    val name: String, // Display name of the food type (e.g., "日式料理", "義大利麵", "火鍋")
    val description: String, // A brief description or question for the card
    @DrawableRes val imageResId: Int // A placeholder drawable resource for the card
)
