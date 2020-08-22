package com.luminaryn.webservice

import android.os.Handler
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

/**
 * This is a static utility class. It does nothing on it's own
 * except provide some extra classes providing a simple callback
 * interface for handling file downloads. This requires that we have
 * WRITE access to the target location!
 *
 * Example usage in your web service class:
 *
 * public void downloadFile(String url, String filepath, Download.FilesResponseHandler handler) {
 *   sendRequest(new Request.Builder().url(url).build(),
 *   new Download.FileCallback(filepath, this, handler));
 * }
 *
 * public void downloadFile(String url, String filepath) {
 *   sendRequest(new Request.Builder().url(url).build(),
 *   new Download.FileCallback(filepath, this));
 * }
 *
 */
class Download {
    open class FileCallback(private var targetPath: String, var ws: HTTP, var handler: FileResponseHandler? = null) : Callback {

        override fun onFailure(call: Call, e: IOException) {
            Log.e(ws.TAG, "Failure downloading file: " + e.message)
            handler?.handleConnectionFailure(call, e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                handler?.handleUnsuccessfulResponse(call, response)
                return
            }
            try {
                handler?.handleSuccessfulResponse(call, response)
                val download = File(targetPath)
                val sink = download.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()
                handler?.handleDownload(download)
            } catch (e: Exception) {
                Log.v(ws.TAG, "Exception occurred trying to save downloaded file: " + e.message)
                handler?.handleException(call, response, e)
            }
        }
    }

    interface FileResponseHandler {
        fun handleDownload(file: File) {}
        fun handleSuccessfulResponse(call: Call, response: Response) {}
        fun handleConnectionFailure(call: Call, e: IOException) {}
        fun handleUnsuccessfulResponse(call: Call, response: Response) {}
        fun handleException(call: Call, response: Response, e: Exception) {}
    }

    abstract class FileUIResponseHandler : FileResponseHandler {
        abstract fun setup(file: File): Runnable

        val uIHandler: Handler
            get() = HTTP.uIHandler

        override fun handleDownload(file: File) {
            uIHandler.post(setup(file))
        }
    }
}