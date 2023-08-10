package com.panosdim.moneytrack.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.panosdim.moneytrack.prefs
import java.time.Instant
import java.util.Date

fun isJWTExpired(): Boolean {
    val decodedJWT: DecodedJWT = JWT.decode(prefs.token)
    val expirationDate = decodedJWT.expiresAt
    val currentTimestamp = Date.from(Instant.now())

    return currentTimestamp.after(expirationDate)
}