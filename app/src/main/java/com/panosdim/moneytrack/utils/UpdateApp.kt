package com.panosdim.moneytrack.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.TAG
import com.panosdim.moneytrack.models.FileMetadata
import kotlinx.serialization.json.Json

var refId: Long = -1

@Suppress("DEPRECATION")
fun checkForNewVersion(context: Context) {
    val storage = Firebase.storage
    val metadataFileName = "output-metadata.json"
    val apkFileName = "app-release.apk"

    // Create a storage reference from our app
    val storageRef = storage.reference

    // Create a metadata reference
    val metadataRef: StorageReference = storageRef.child(metadataFileName)

    metadataRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
        // Use the bytes to display the image
        val data = String(it)
        val fileMetadata = Json.decodeFromString<FileMetadata>(data)
        val version = fileMetadata.elements[0].versionCode
        
        val appVersion = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        )

        if (version > appVersion) {
            Toast.makeText(
                context,
                context.getString(R.string.new_version),
                Toast.LENGTH_LONG
            ).show()

            val versionName = fileMetadata.elements[0].versionName

            // Create an apk reference
            val apkRef = storageRef.child(apkFileName)

            apkRef.downloadUrl.addOnSuccessListener { uri ->
                downloadNewVersion(context, uri, versionName)
            }.addOnFailureListener {
                // Handle any errors
                Log.w(TAG, "Fail to download file $apkFileName")
            }
        }
    }.addOnFailureListener {
        // Handle any errors
        Log.w(TAG, "Fail to retrieve $metadataFileName")
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