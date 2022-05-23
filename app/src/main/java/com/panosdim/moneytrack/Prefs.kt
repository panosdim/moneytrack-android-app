package com.panosdim.moneytrack

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

const val PREFS_FILENAME = "credentials"
const val TOKEN = "token"
const val EMAIL = "email"
const val PASSWORD = "password"


class Prefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)

    var token: String
        get() = prefs.getString(TOKEN, "").toString()
        set(value) = prefs.edit().putString(TOKEN, value).apply()

    var email: String
        get() = prefs.getString(EMAIL, "").toString()
        set(value) = prefs.edit().putString(EMAIL, value).apply()

    var password: String
        get() = prefs.getString(PASSWORD, "").toString()
        set(value) = prefs.edit().putString(PASSWORD, value).apply()
}