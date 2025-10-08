package com.fatefulsupper.app.data.response

import com.fatefulsupper.app.data.model.BlackCategory

data class BlacklistData(
    val userid: String,
    /**
     * 黑名單類別的 ID 列表 (例如: [1, 3, 7])
     */
    val blackCategoryIds: List<Int>?,
    /**
     * 黑名單類別的詳細資訊列表
     */
    val blackCategories: List<BlackCategory>?
)