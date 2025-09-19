package com.fatefulsupper.app.data.preferences

import android.content.Context

object FavoritesManager {

    private const val PREFS_NAME = "fateful_supper_favorites_prefs"
    private const val KEY_FAVORITE_RESTAURANT_IDS = "favorite_restaurant_ids"

    private fun getSharedPreferences(context: Context) = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteRestaurantIds(context: Context): Set<String> {
        val prefs = getSharedPreferences(context)
        return prefs.getStringSet(KEY_FAVORITE_RESTAURANT_IDS, emptySet()) ?: emptySet()
    }

    fun addFavoriteRestaurant(context: Context, restaurantId: String) {
        val prefs = getSharedPreferences(context)
        val favorites = getFavoriteRestaurantIds(context).toMutableSet()
        if (favorites.add(restaurantId)) {
            prefs.edit().putStringSet(KEY_FAVORITE_RESTAURANT_IDS, favorites).apply()
        }
    }

    fun removeFavoriteRestaurant(context: Context, restaurantId: String) {
        val prefs = getSharedPreferences(context)
        val favorites = getFavoriteRestaurantIds(context).toMutableSet()
        if (favorites.remove(restaurantId)) {
            prefs.edit().putStringSet(KEY_FAVORITE_RESTAURANT_IDS, favorites).apply()
        }
    }

    fun isRestaurantFavorite(context: Context, restaurantId: String): Boolean {
        return getFavoriteRestaurantIds(context).contains(restaurantId)
    }

    fun toggleFavoriteStatus(context: Context, restaurantId: String): Boolean {
        val isCurrentlyFavorite = isRestaurantFavorite(context, restaurantId)
        if (isCurrentlyFavorite) {
            removeFavoriteRestaurant(context, restaurantId)
            return false
        } else {
            addFavoriteRestaurant(context, restaurantId)
            return true
        }
    }
}
