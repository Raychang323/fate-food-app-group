package com.fatefulsupper.app.data.model

import androidx.annotation.DrawableRes

data class FoodCardItem(
    val id: String,
    val name: String,
    @DrawableRes val imageResId: Int, // For local drawable resources as placeholders
    val description: String
)
