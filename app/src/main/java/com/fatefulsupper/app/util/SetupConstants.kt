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

    // Data class to hold supper type information including an integer ID
    data class SupperType(
        val id: Int,
        val displayName: String,
        val typeKey: String
    )

    // Supper Types for Blacklist (Display Name to API Key)
    val SUPPER_TYPES_BLACKLIST_OPTIONS = listOf(
        SupperType(1, "超商", "convenience_store"),
        SupperType(2, "美式料理", "american_restaurant"),
        SupperType(3, "亞洲料理", "asian_restaurant"),
        SupperType(4, "串燒", "bar_and_grill"),
        SupperType(5, "燒烤", "barbecue_restaurant"),
        SupperType(6, "早餐店（包含燒餅油條、清粥小菜）", "breakfast_restaurant"),
        SupperType(7, "中式料理（包含台式料理）", "chinese_restaurant"),
        SupperType(8, "熟食", "deli"),
        SupperType(9, "餐館", "diner"),
        SupperType(10, "速食", "fast_food_restaurant"),
        SupperType(11, "漢堡", "hamburger_restaurant"),
        SupperType(12, "義式料理", "italian_restaurant"),
        SupperType(13, "日式料理", "japanese_restaurant"),
        SupperType(14, "披薩", "pizza_restaurant"),
        SupperType(15, "拉麵", "ramen_restaurant"),
        SupperType(16, "餐廳", "restaurant"),
        SupperType(17, "海鮮", "seafood_restaurant"),
        SupperType(18, "純素餐廳", "vegan_restaurant"),
        SupperType(19, "素食餐廳", "vegetarian_restaurant"),
        SupperType(20, "食物外送", "food_delivery"),
        SupperType(21, "外帶餐點", "meal_takeaway"),
        SupperType(22, "餐點外送", "meal_delivery")
    )
}
