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
object Download {
    open class FileCallback(private var targetFile: File, var ws: HTTP) : Callback {

        open var handler: FileResponseHandler? = null

        @JvmOverloads
        constructor(targetPath: String, ws: HTTP, handler: FileResponseHandler? = null) : this(File(targetPath), ws) {
            this.handler = handler
        }

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
                val sink = targetFile.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()
                handler?.handleDownload(targetFile)
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

    /* Like the others, use {runOnUiThread} instead of this.
    abstract class FileUIResponseHandler : FileResponseHandler {
        abstract fun setup(file: File): Runnable

        val uIHandler: Handler
            get() = HTTP.uIHandler

        override fun handleDownload(file: File) {
            uIHandler.post(setup(file))
        }
    }
     */
}