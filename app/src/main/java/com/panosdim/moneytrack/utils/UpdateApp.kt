package com.panosdim.moneytrack.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.TAG
import com.panosdim.moneytrack.models.FileMetadata
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

var refId: Long = -1

private val json = Json { ignoreUnknownKeys = true }

fun checkForNewVersion(context: Context) {
    val metadataFileName = "output-metadata.json"
    val apkFileName = "app-release.apk"
    val backendUrl = "https://apps.dsw.mywire.org/money-track/"
    val url: URL

    try {
        url = URL(backendUrl + metadataFileName)
        val conn = url.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = true
        conn.requestMethod = "GET"
        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.useCaches = false

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            val data = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val fileMetadata = json.decodeFromString<FileMetadata>(data)
            val version = fileMetadata.elements[0].versionCode

            val appVersion = PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )
            )

            if (version > appVersion) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.new_version),
                        Toast.LENGTH_LONG
                    ).show()
                }

                val versionName = fileMetadata.elements[0].versionName

                // Download APK file
                val apkUri = Uri.parse(backendUrl + apkFileName)
                downloadNewVersion(context, apkUri, versionName)
            }
        }
    } catch (e: Exception) {
        Log.d(TAG, e.toString())
    }
}

private fun downloadNewVersion(context: Context, downloadUrl: Uri, version: String) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
        DownloadManager.Request(downloadUrl)
    request.setDescription("Downloading new version of MoneyTrack.")
    request.setTitle("New MoneyTrack Version: $version")
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        "MoneyTrack-${version}.apk"
    )
    refId = manager.enqueue(request)
}