package com.fatefulsupper.app.data.model

data class UpdateBlacklistRequest(
    val categoryIds: List<Int> // 儲存所有被勾選的宵夜選項的唯一識別碼 (Int)
)
