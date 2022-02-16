package com.luminaryn.webservice

import com.luminaryn.webservice.extensions.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*

/**
 * Simple wrapper for OkHttp.
 *
 * Not meant to be used on it's own, see JSON for a real implementation.
 */
abstract class HTTP {
    /**
     * The OkHttpClient instance.
     */
    val client: OkHttpClient
    protected val baseURL: String
    open val TAG: String
    @JvmField
    protected val logLevel: Int

    /**
     * OkHttp Headers to pass to a makeRequest wrapper.
     * It's left up to your subclass how to handle this.
     */
    var headers: Headers? = null;

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
     * Set the headers using a Headers.Builder object.
     *
     * @param headers
     */
    fun setHeaders(headers: Headers.Builder) {
        this.headers = headers.build()
    }

    /**
     * Return an {okhttp3.Request.Builder} with a URL set.
     *
     * @param uri The URI we're requesting.
     * @param headers Optional headers instance we want to add.
     * @param absoluteUri If true, the URI is the full URL, otherwise we prepend the baseURL.
     * @return The Builder object that can be further updated as needed.
     */
    @JvmOverloads
    fun makeRequest(uri: String, headers: Headers? = null, absoluteUri: Boolean = false): Request.Builder {
        val url = if (absoluteUri) uri else baseURL + uri
        val builder = Request.Builder().url(url)
        if (headers != null) {
            builder.headers(headers);
        }
        return builder;
    }

    /**
     * A wrapper for makeRequest that automatically builds a Headers.Builders object passed in.
     *
     * @param uri The URI we're requesting.
     * @param headers The headers, but as a Headers.Builder instance that we'll build() first.
     * @param absoluteUri If true, the URI is the full URL, otherwise we prepend the baseURL.
     * @return The Builder object that can be further updated as needed.
     */
    @JvmOverloads
    fun makeRequest(uri: String, headers: Headers.Builder, absoluteUri: Boolean = false): Request.Builder {
        return makeRequest(uri, headers.build(), absoluteUri);
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
    fun formBodyFromChunk(fileFormName: String, file: File, chunkSize: Long, wantPart: Long,
                          contentType: MediaType = TYPE_DEFAULT_FILE,
                          extFormat: String = "%03d"): MultipartBody.Builder {
        val ext = extFormat.format(wantPart)
        val fileName = "${file.name}.$ext"
        return formBody().addFormDataPart(fileFormName, fileName, file.getRequestBodyChunk(chunkSize, wantPart, contentType))
    }

    @JvmOverloads
    fun formBodyFromChunk(fileFormName: String, file: File, fileName: String,
                          chunkSize: Long, wantPart: Long,
                          contentType: MediaType = TYPE_DEFAULT_FILE): MultipartBody.Builder {
        return formBody().addFormDataPart(fileFormName, fileName, file.getRequestBodyChunk(chunkSize, wantPart, contentType))
    }

    @JvmOverloads
    fun formBody(fileFormName: String, fileName: String, byteArray: ByteArray, contentType: MediaType = TYPE_DEFAULT_FILE): MultipartBody.Builder {
        return formBody().addFormDataPart(fileFormName, fileName, byteArray.toRequestBody(contentType, 0, byteArray.size))
    }

    fun formBody(fileFormName: String, fileName: String, byteArray: ByteArray, contentType: String): MultipartBody.Builder {
        return formBody(fileFormName, fileName, byteArray, contentType.toMediaType())
    }

    @JvmOverloads
    fun formBody(fileFormName: String, fileName: String, inputStream: InputStream, contentType: MediaType = TYPE_DEFAULT_FILE): MultipartBody.Builder {
        return formBody().addFormDataPart(fileFormName, fileName, inputStream.asRequestBody(contentType))
    }

    fun formBody(fileFormName: String, fileName: String, inputStream: InputStream, contentType: String): MultipartBody.Builder {
        return formBody(fileFormName, fileName, inputStream, contentType.toMediaType())
    }

    /**
     * Send a request and enqueue a callback to handle the response.
     *
     * @param request
     * @param callback
     */
    fun sendRequest(request: Request, callback: Callback) {
        client.newCall(request).enqueue(callback)
    }

    /**
     * An abstract class for a Builder, add a build() method specific to your child class.
     */
    abstract class Builder<T : Builder<T>> {
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
        protected const val DEFAULT_LOG_LEVEL = LOG_ERRORS

        fun qsval (value: Any): String {
            if (value is String) return value
            else if (value is Boolean) return if (value) "1" else "0"
            else return value.toString()
        }

        /**
         * Take a map of options and make a query string out of it.
         */
        fun query (opts: Map<String,Any>): String {
            var query = ""
            for (opt in opts) {
                if (query.isEmpty()) query += "?" else query += "&"
                query += opt.key
                query += "="
                query += qsval(opt.value)
            }
            return query
        }
    }
}