package com.panosdim.moneytrack.api.data

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)