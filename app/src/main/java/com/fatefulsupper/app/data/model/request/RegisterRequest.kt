package com.fatefulsupper.app.data.model.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val userid: String
)
