package com.panosdim.moneytrack.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.LiveData
import com.panosdim.moneytrack.BACKEND_URL
import com.panosdim.moneytrack.model.Category
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import javax.net.ssl.HttpsURLConnection


val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
var refId: Long = -1

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

fun moneyFormat(obj: Any): String {
    val symbols = DecimalFormatSymbols()
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val moneyFormat = DecimalFormat("#,##0.00 €", symbols)
    return moneyFormat.format(obj)
}

fun checkForNewVersion(context: Context) {
    val url: URL
    val response: String
    try {
        url = URL(BACKEND_URL + "apk/output.json")

        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doOutput = false

        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val version = JSONObject(response).getLong("versionCode")
            val appVersion = PackageInfoCompat.getLongVersionCode(
                    context.packageManager.getPackageInfo(
                            context.packageName,
                            0
                    )
            )
            if (version > appVersion && ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                val versionName = JSONObject(response).getString("versionName")
                downloadNewVersion(context, versionName)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun downloadNewVersion(context: Context, version: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
            DownloadManager.Request(Uri.parse(BACKEND_URL + "apk/app-release.apk"))
    request.setDescription("Downloading new version of MoneyTrack.")
    request.setTitle("New MoneyTrack Version")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "MoneyTrack-${version}.apk"
    )
    refId = manager.enqueue(request)
}

fun generateTextWatcher(validateFunc: () -> Unit): TextWatcher {
    return object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            validateFunc()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // Not required
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // Not required
        }
    }
}

fun <T> startIntent(context: Context, cls: Class<T>) {
    val intent = Intent(context, cls)
    intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

fun getCategoryName(id: Int, categories: LiveData<List<Category>>): String {
    return categories.value?.find { it.id == id }?.category ?: ""
}