package com.fatefulsupper.app.data.model

/**
 * 黑名單類別的詳細資訊
 */
data class BlackCategory(
    val id: Int,
    val categoryKey: String, // 例如 "convenience_store"
    val categoryName: String // 例如 "超商"
)