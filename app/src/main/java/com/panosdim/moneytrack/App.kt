package com.panosdim.moneytrack

import android.app.Application
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.moneytrack.db.AppDatabase

const val BACKEND_URL = "https://moneytrack.dsw.mywire.org/api/"
const val TAG = "MoneyTrack-Tag"

val paddingSmall = 4.dp
val paddingLarge = 8.dp
val paddingExtraLarge = 16.dp

val prefs: Prefs by lazy {
    App.prefs
}

val db by lazy {
    App.db
}

val auth by lazy {
    App.auth
}


class App : Application() {
    companion object {
        lateinit var prefs: Prefs
        lateinit var instance: App private set
        lateinit var auth: FirebaseAuth
        lateinit var db: AppDatabase
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        auth = Firebase.auth
        super.onCreate()
        instance = this
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "moneytrack"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}