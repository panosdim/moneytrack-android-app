package com.panosdim.moneytrack.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String = ""
)