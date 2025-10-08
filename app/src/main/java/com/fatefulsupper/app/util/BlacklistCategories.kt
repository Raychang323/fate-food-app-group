package com.fatefulsupper.app.util

import com.fatefulsupper.app.data.model.BlackCategory

object BlacklistCategories {
    /**
     * 靜態列表，用於前端顯示和根據 ID 查找詳細資訊
     */
    val ALL_CATEGORIES = listOf(
        BlackCategory(1, "convenience_store", "超商"),
        BlackCategory(2, "american_restaurant", "美式料理"),
        BlackCategory(3, "asian_restaurant", "亞洲料理"),
        BlackCategory(4, "bar_and_grill", "串燒"),
        BlackCategory(5, "barbecue_restaurant", "燒烤"),
        BlackCategory(6, "breakfast_restaurant", "早餐店（包含燒餅油條、清粥小菜）"),
        BlackCategory(7, "chinese_restaurant", "中式料理（包含台式料理）"),
        BlackCategory(8, "deli", "熟食"),
        BlackCategory(9, "diner", "餐館"),
        BlackCategory(10, "fast_food_restaurant", "速食"),
        BlackCategory(11, "hamburger_restaurant", "漢堡"),
        BlackCategory(12, "italian_restaurant", "義式料理"),
        BlackCategory(13, "japanese_restaurant", "日式料理"),
        BlackCategory(14, "pizza_restaurant", "披薩"),
        BlackCategory(15, "ramen_restaurant", "拉麵"),
        BlackCategory(16, "restaurant", "餐廳"),
        BlackCategory(17, "seafood_restaurant", "海鮮"),
        BlackCategory(18, "vegan_restaurant", "純素餐廳"),
        BlackCategory(19, "vegetarian_restaurant", "素食餐廳"),
        BlackCategory(20, "food_delivery", "食物外送"),
        BlackCategory(21, "meal_takeaway", "外帶餐點"),
        BlackCategory(22, "meal_delivery", "餐點外送")
    )
    
    /**
     * 方便快速從 ID 查找詳細資訊的映射表 (Map)
     */
    val CATEGORY_MAP = ALL_CATEGORIES.associateBy { it.id }
}