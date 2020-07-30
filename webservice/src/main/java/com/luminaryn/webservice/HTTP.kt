package com.luminaryn.webservice

import android.os.Handler
import android.os.Looper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http2.Header
import java.io.File

/**
 * Simple wrapper for OkHttp.
 *
 * Not meant to be used on it's own, see JSON for a real implementation.
 */
abstract class HTTP {
    /**
     * Get the OkHttpClient instance.
     *
     * @return
     */
    val client: OkHttpClient
    protected val baseURL: String
    open val TAG: String
    @JvmField
    protected val logLevel: Int

    /**
     * Create an HTTP client with defaults.
     * @param baseURL
     */
    @JvmOverloads
    constructor(baseURL: String = "") {
        this.baseURL = baseURL
        TAG = DEFAULT_TAG
        logLevel = DEFAULT_LOG_LEVEL
        client = OkHttpClient()
    }

    /**
     * Used by HTTP.Builder.build() to create an HTTP client with customized settings.
     *
     * @param httpBuilder
     */
    protected constructor(httpBuilder: Builder<*>) {
        baseURL = httpBuilder.baseURL
        TAG = httpBuilder.TAG
        logLevel = httpBuilder.logLevel
        if (httpBuilder.okClientBuilder == null) {
            client = OkHttpClient()
        } else {
            client = httpBuilder.okClientBuilder!!.build()
        }
    }

    /**
     * Return an okhttp3.Request.Builder with a URL set.
     * Will automatically prepend the baseURL.
     *
     * @param uri
     * @return
     */
    @JvmOverloads
    fun makeRequest(uri: String, headers: Headers? = null): Request.Builder {
        val url = baseURL + uri
        val builder = Request.Builder().url(url)
        if (headers != null) {
            builder.headers(headers);
        }
        return builder;
    }

    /**
     * A wrapper for makeRequest that automatically builds a Headers.Builders object passed in.
     */
    fun makeRequest(uri: String, headers: Headers.Builder): Request.Builder {
        return makeRequest(uri, headers.build());
    }

    /**
     * Get a new headers builder.
     */
    fun makeHeaders(): Headers.Builder {
        return Headers.Builder();
    }

    /**
     * Get a okhttp3.MultipartBody.Builder instance set to the
     * multipart/form-data MIME type.
     *
     * @return
     */
    fun formBody(): MultipartBody.Builder {
        return MultipartBody.Builder()
                .setType(MultipartBody.FORM)
    }

    @JvmOverloads
    fun formBody(fileFormName: String, file: File, contentType: MediaType = TYPE_DEFAULT_FILE): MultipartBody.Builder {
        return formBody().addFormDataPart(fileFormName, file.name, file.asRequestBody(contentType))
    }

    fun formBody(fileFormName: String, file: File, contentType: String): MultipartBody.Builder {
        return formBody(fileFormName, file, contentType.toMediaType())
    }

    @JvmOverloads
    fun formBody(fileFormName: String, fileName: String, byteArray: ByteArray, contentType: MediaType = TYPE_DEFAULT_FILE): MultipartBody.Builder {
        return formBody().addFormDataPart(fileFormName, fileName, byteArray.toRequestBody(contentType, 0, byteArray.size))
    }

    fun formBody(fileFormName: String, fileName: String, byteArray: ByteArray, contentType: String): MultipartBody.Builder {
        return formBody(fileFormName, fileName, byteArray, contentType.toMediaType())
    }

    /**
     * Send a request and enqueue a callback to handle the response.
     *
     * @param request
     * @param callback
     */
    fun sendRequest(request: Request?, callback: Callback?) {
        client.newCall(request!!).enqueue(callback!!)
    }

    /**
     * An abstract class for a Builder, add a build() method specific to your child class.
     */
    abstract class Builder<T : Builder<T>?> {
        var baseURL = ""
        var TAG = DEFAULT_TAG
        var logLevel = DEFAULT_LOG_LEVEL
        var okClientBuilder: OkHttpClient.Builder? = null
        protected abstract val `this`: T
        fun setBaseUrl(url: String): T {
            baseURL = url
            return `this`
        }

        fun setTag(tag: String): T {
            TAG = tag
            return `this`
        }

        fun setLogLevel(level: Int): T {
            logLevel = level
            return `this`
        }

        fun setClientBuilder(clientBuilder: OkHttpClient.Builder?): T {
            okClientBuilder = clientBuilder
            return `this`
        }

        fun clientBuilder(): OkHttpClient.Builder? {
            if (okClientBuilder == null) {
                setClientBuilder(okBuilder())
            }
            return okClientBuilder
        }

        companion object {
            @JvmStatic
            fun okBuilder(): OkHttpClient.Builder {
                return OkHttpClient.Builder()
            }
        }
    }

    companion object {
        const val LOG_NONE = 0
        const val LOG_ERRORS = 1
        const val LOG_WARNINGS = 2
        const val LOG_DEBUG = 3
        val TYPE_DEFAULT_FILE: MediaType = "application/octet-stream".toMediaType()
        protected const val DEFAULT_TAG = "com.luminaryn.webservice"
        protected const val DEFAULT_LOG_LEVEL = LOG_NONE

        /**
         * Get a handler in the main UI looper.
         *
         * @return
         */
        @JvmStatic
        val uIHandler: Handler
            get() = Handler(Looper.getMainLooper())
    }
}