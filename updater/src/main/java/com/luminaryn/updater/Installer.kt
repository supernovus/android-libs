package com.luminaryn.updater

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.luminaryn.webservice.JSON
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

open class Installer (protected var context: Context, protected var provider: String? = null) : JSON () {
    /**
     * Download an update from a URL.
     *
     * @param url The URL to download the update from.
     */
    fun downloadUpdate(url: String, filename: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        val parent = this
        sendRequest(request, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v(TAG,"Error downloading update APK file", e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val download = File(context.cacheDir, filename)
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val sink = download.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.close()
                    if (download.exists()) {
                        parent.installApk(download)
                    }
                } catch (e: IOException) {
                    Log.v(TAG,"IOException thrown while downloading update", e)
                }
            }
        })
    }

    // TODO: move to PackageInstaller API.
    fun installApk(file: File) {
        if (provider == null) {
            Log.e(TAG, "No provider initialized, this is not valid!")
            return
        }
        val uri = FileProvider.getUriForFile(context.applicationContext, provider!!, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.data = uri
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}