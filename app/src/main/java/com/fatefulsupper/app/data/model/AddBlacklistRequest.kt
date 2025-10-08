package com.fatefulsupper.app.data.model

data class AddBlacklistRequest(
    /**
     * 要加入黑名單的美食類別 ID (整數)
     * 對應到您的黑名單數字 1, 2, 3...
     */
    val categoryId: Int
)