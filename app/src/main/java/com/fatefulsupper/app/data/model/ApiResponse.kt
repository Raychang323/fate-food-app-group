package com.fatefulsupper.app.data.model

data class ApiResponse(
    val status: String,
    val message: String,
    val userid: String? = null,
    val username: String? = null
)
