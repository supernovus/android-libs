package com.luminaryn.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
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

open class Installer (protected var context: Context,
                      protected var provider: String? = null,
                      protected var packageName: String? = null,
                      protected var streamName: String = DEFAULT_PKG_STREAM_NAME,
                      protected var completeIntent: String = DEFAULT_COMPLETE_INTENT) : JSON () {

    /**
     * Download an update from a URL.
     *
     * @param url The URL to download the update from.
     */
    @JvmOverloads
    fun downloadUpdate(url: String, filename: String, installFlags: Int = 0) {
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
                        parent.installApk(download, installFlags)
                    }
                } catch (e: IOException) {
                    Log.v(TAG,"IOException thrown while downloading update", e)
                }
            }
        })
    }

    @JvmOverloads
    fun installApk(file: File, installFlags: Int = 0) {
        if (installFlags and USE_PM == USE_PM) {
            val trySilent = (installFlags and TRY_SILENT == TRY_SILENT)
            installApkWithPM(file, trySilent)
        } else {
            installApkWithIntent(file)
        }
    }

    fun installApkWithIntent(file: File) {
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

    fun installApkWithPM(file: File, trySilent: Boolean) {
        val pi = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (packageName != null) {
            params.setAppPackageName(packageName)
        }
        if (trySilent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }

        val sid = pi.createSession(params)
        val session = pi.openSession(sid)

        val outStream = session.openWrite(streamName, 0, file.length())
        val inStream = file.inputStream()
        val buffer = ByteArray(65536)
        var c: Int

        while (run {
                c = inStream.read(buffer)
                c
            } != -1) {
            outStream.write(buffer, 0, c)
        }

        session.fsync(outStream)
        inStream.close()
        outStream.close()

        val intent = PendingIntent.getBroadcast(
            context,
            sid,
            Intent(completeIntent),
            0
        )
        session.commit(intent.intentSender)
    }

    companion object {
        const val USE_PM     = 1 // Use pm.PackageInstaller instead of ACTION_INSTALL_PACKAGE
        const val TRY_SILENT = 2 // Enable silent installation if available.

        const val DEFAULT_PKG_STREAM_NAME = "LUM_INSTALLER"
        const val DEFAULT_COMPLETE_INTENT = "com.luminaryn.installer.ACTION_INSTALL_COMPLETE"
    }

}