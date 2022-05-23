package com.panosdim.moneytrack

import android.app.Application
import androidx.room.Room
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.moneytrack.db.AppDatabase

val prefs: Prefs by lazy {
    App.prefs!!
}

val db by lazy {
    App.db
}

val auth by lazy {
    App.auth
}

const val BACKEND_URL = "https://moneytrack.dsw.mywire.org/api/"
const val TAG = "MONEY_TRACK"
const val YEARS_TO_FETCH = "3"

class App : Application() {
    companion object {
        var prefs: Prefs? = null
        lateinit var db: AppDatabase
        lateinit var instance: App private set
        lateinit var auth: FirebaseAuth
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "moneytrack"
        )
            .fallbackToDestructiveMigration()
            .build()
        auth = Firebase.auth
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
    }
}