package com.panosdim.moneytrack.rest

import android.util.Log
import com.panosdim.moneytrack.BACKEND_URL
import com.panosdim.moneytrack.BuildConfig
import com.panosdim.moneytrack.TAG
import com.panosdim.moneytrack.prefs
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val client = HttpClient(OkHttp) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
        })
    }
    install(Logging) {
        if (BuildConfig.DEBUG) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.ALL
        }
    }
    defaultRequest {
        url(BACKEND_URL)
        bearerAuth(prefs.token)
        accept(ContentType.Application.Json.withCharset(Charsets.UTF_8))
    }
}