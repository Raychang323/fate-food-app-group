package com.fatefulsupper.app.util

object SetupConstants {

    const val PREFS_NAME = "FatefulSupperFirstTimeSetupPrefs"
    const val KEY_FIRST_TIME_SETUP_COMPLETED = "isFirstTimeSetupCompleted"

    // Notification Settings Keys
    const val KEY_NOTIFICATION_DAYS = "notification_days"
    const val KEY_NOTIFICATION_HOUR = "notification_hour"
    const val KEY_NOTIFICATION_MINUTE = "notification_minute"
    const val DEFAULT_NOTIFICATION_HOUR = 21
    const val DEFAULT_NOTIFICATION_MINUTE = 0

    val DEFAULT_NOTIFICATION_DAYS = setOf(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    )

    // Supper Blacklist Key
    const val KEY_BLACKLISTED_SUPPER_TYPES = "blacklisted_supper_types"

    // Supper Types for Blacklist (Display Name to API Key)
    val SUPPER_TYPES_BLACKLIST_OPTIONS = mapOf(
        "餐廳" to "restaurant",
        "食品店" to "food_store",
        "食物外送 (Food Delivery)" to "food_delivery",
        "便利商店" to "convenience_store",
        "美式餐廳" to "american_restaurant",
        "亞洲餐廳" to "asian_restaurant",
        "餐酒吧" to "bar_and_grill",
        "燒烤餐廳" to "barbecue_restaurant",
        "早餐店" to "breakfast_restaurant",
        "中式餐廳" to "chinese_restaurant",
        "熟食店" to "deli",
        "美式簡餐館" to "diner",
        "速食餐廳" to "fast_food_restaurant",
        "漢堡店" to "hamburger_restaurant",
        "義式餐廳" to "italian_restaurant",
        "日式餐廳" to "japanese_restaurant",
        "餐點外送 (Meal Delivery)" to "meal_delivery",
        "外帶餐點" to "meal_takeaway",
        "披薩店" to "pizza_restaurant",
        "拉麵店" to "ramen_restaurant",
        "海鮮餐廳" to "seafood_restaurant",
        "全素餐廳 (Vegan)" to "vegan_restaurant",
        "蛋奶素餐廳 (Vegetarian)" to "vegetarian_restaurant"
    )
}