package com.panosdim.moneytrack.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.panosdim.moneytrack.prefs

class CheckCredentialsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent: Intent

        if (prefs.email.isEmpty() or prefs.password.isEmpty()) {
            finish()
            intent =
                Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}