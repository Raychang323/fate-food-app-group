package com.fatefulsupper.app.data.model

data class BlacklistData(
    val userid: String,
    val blackCategoryIds: List<Int>,
    val blackCategories: List<BlacklistedCategory>
)
