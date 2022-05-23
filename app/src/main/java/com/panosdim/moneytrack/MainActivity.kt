package com.panosdim.moneytrack

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.panosdim.moneytrack.api.Webservice
import com.panosdim.moneytrack.api.data.LoginRequest
import com.panosdim.moneytrack.api.webservice
import com.panosdim.moneytrack.utils.checkForNewVersion
import com.panosdim.moneytrack.utils.refId
import com.panosdim.moneytrack.utils.startIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {
    private lateinit var manager: DownloadManager
    private lateinit var onComplete: BroadcastReceiver
    private var client: Webservice = webservice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val referenceId = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (referenceId != -1L && referenceId == refId) {
                    val apkUri = manager.getUriForDownloadedFile(refId)
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    installIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(installIntent)
                }

            }
        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener(this@MainActivity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d(TAG, "signInAnonymously:success")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Fail to login anonymously.", it)
                }
        }

        // Check for new version
        checkForNewVersion(this)
    }

    override fun onResume() {
        super.onResume()

        refreshToken()
    }

    private fun refreshToken() {
        if (prefs.email.isNotEmpty() and prefs.password.isNotEmpty()) {
            val scope = CoroutineScope(Dispatchers.Main)

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val response =
                            client.login(
                                LoginRequest(
                                    prefs.email, prefs.password
                                )
                            )
                        prefs.token = response.token
                    }
                } catch (e: HttpException) {
                    startIntent(this@MainActivity, LoginActivity::class.java)
                }
            }
        } else {
            startIntent(this, LoginActivity::class.java)
        }
    }
}