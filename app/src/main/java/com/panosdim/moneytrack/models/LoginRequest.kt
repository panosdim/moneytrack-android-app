package com.panosdim.moneytrack.models

import com.panosdim.moneytrack.prefs
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String = prefs.email,
    val password: String = prefs.password
)