package com.luminaryn.webservice

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * A class for communicating with JSON-based web services.
 *
 * Can be used standalone, or (preferably) use a sub-class to add your method calls.
 */
open class JSON : HTTP {

    constructor() : super() {
        defaultRequestBody = null
        useAbsoluteUris = false
    }

    constructor(url: String, absoluteUris: Boolean = false, defaultBody: RequestBody? = null) : super(url) {
        defaultRequestBody = defaultBody
        useAbsoluteUris = absoluteUris
    }

    protected constructor(builder: Builder) : super(builder) {
        useAbsoluteUris = builder.useAbsoluteUris
        defaultRequestBody = builder.defaultRequestBody
    }

    private val defaultRequestBody: RequestBody?
    private val useAbsoluteUris: Boolean

    interface JSONResponseHandler {
        fun handle(data: JSONObject)
    }

    inner class JSONCallback : Callback {

        constructor(handler: JSONResponseHandler) {
            this.handler = handler
            this.closure = null
        }

        constructor(closure: JSONClosure) {
            this.closure = closure
            this.handler = null
        }

        private val handler: JSONResponseHandler?
        private val closure: JSONClosure?

        override fun onFailure(call: Call, e: IOException) {
            val err = errorMsg(arrayOf("internal", "http_failure", e.message))
            if (closure != null)
                closure.invoke(err)
            else if (handler != null)
                handler.handle(err)
            else {
                if (logLevel >= LOG_ERRORS) {
                    Log.e(TAG, "JSONCallback without closure or handler handled error: $err")
                }
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val jsr = jsonResponse(response)
            if (closure != null)
                closure.invoke(jsr)
            else if (handler != null)
                handler.handle(jsr)
            else
            {
                if (logLevel >= LOG_WARNINGS) {
                    Log.w(TAG, "JSONCallback without closure or handler received response: $jsr")
                }
            }
        }
    }

    @JvmOverloads
    fun detectData (data: Any?, default: RequestBody? = defaultRequestBody): RequestBody? {
        if (data is RequestBody) return data
        if (data is JSONObject) return jsonBody(data)
        if (data is Map<*,*>) return jsonBody(JSONObject(data))
        return default
    }

    fun jsonBody(data: JSONObject): RequestBody {
        return data.toString().toRequestBody(TYPE_JSON)
    }

    fun GET(uri: String, absoluteUris: Boolean, callback: JSONCallback) {
        sendRequest(makeRequest(uri, headers, absoluteUris).get().build(), callback)
    }

    fun GET(uri: String, handler: JSONResponseHandler) {
        GET(uri, useAbsoluteUris, JSONCallback(handler))
    }

    fun GET(uri: String, closure: JSONClosure) {
        GET(uri, useAbsoluteUris, JSONCallback(closure))
    }

    fun POST(uri: String, data: RequestBody, absoluteUris: Boolean, callback: JSONCallback) {
        sendRequest(makeRequest(uri, headers, absoluteUris).post(data).build(), callback)
    }

    fun POST(uri: String, data: Any, handler: JSONResponseHandler) {
        val body = detectData(data)
        if (body != null) {
            POST(uri, body, useAbsoluteUris, JSONCallback(handler))
        } else nullBodyErr()
    }

    fun POST(uri: String, data: Any, closure: JSONClosure) {
        val body = detectData(data)
        if (body != null) {
            POST(uri, body, useAbsoluteUris, JSONCallback(closure))
        } else nullBodyErr()
    }

    fun PUT(uri: String, data: RequestBody, absoluteUris: Boolean, callback: JSONCallback) {
        sendRequest(makeRequest(uri, headers, absoluteUris).put(data).build(), callback)
    }

    fun PUT(uri: String, data: Any, handler: JSONResponseHandler) {
        val body = detectData(data)
        if (body != null) {
            PUT(uri, body, useAbsoluteUris, JSONCallback(handler))
        } else nullBodyErr()
    }

    fun PUT(uri: String, data: Any, closure: (JSONObject) -> Unit) {
        val body = detectData(data)
        if (body != null) {
            PUT(uri, body, useAbsoluteUris, JSONCallback(closure))
        } else nullBodyErr()
    }

    fun PATCH(uri: String, data: RequestBody, absoluteUris: Boolean, callback: JSONCallback) {
        sendRequest(makeRequest(uri, headers, absoluteUris).patch(data).build(), callback)
    }

    fun PATCH(uri: String, data: Any, handler: JSONResponseHandler) {
        val body = detectData(data)
        if (body != null) {
            PATCH(uri, body, useAbsoluteUris, JSONCallback(handler))
        } else nullBodyErr()
    }

    fun PATCH(uri: String, data: Any, closure: (JSONObject) -> Unit) {
        val body = detectData(data)
        if (body != null) {
            PATCH(uri, body, useAbsoluteUris, JSONCallback(closure))
        } else nullBodyErr()
    }

    fun DELETE(uri: String, absoluteUris: Boolean, callback: JSONCallback) {
        sendRequest(makeRequest(uri, headers, absoluteUris).delete().build(), callback)
    }

    fun DELETE(uri: String, handler: JSONResponseHandler) {
        DELETE(uri, useAbsoluteUris, JSONCallback(handler))
    }

    fun DELETE(uri: String, closure: (JSONObject) -> Unit) {
        DELETE(uri, useAbsoluteUris, JSONCallback(closure))
    }

    fun HTTP(method: String, uri: String, data: Any?, absoluteUris: Boolean, callback: JSONCallback) {
        val body = detectData(data)
        sendRequest(makeRequest(uri, headers, absoluteUris).method(method, body).build(), callback)
    }

    fun HTTP(method: String, uri: String, data: Any?, handler: JSONResponseHandler) {
        HTTP(method, uri, data, useAbsoluteUris, JSONCallback(handler))
    }

    fun HTTP(method: String, uri: String, data: Any?, closure: (JSONObject) -> Unit) {
        HTTP(method, uri, data, useAbsoluteUris, JSONCallback(closure))
    }

    fun OPTIONS(uri: String, data: Any?, absoluteUris: Boolean, callback: JSONCallback) {
        HTTP(OPTIONS, uri, data, absoluteUris, callback)
    }

    fun OPTIONS(uri: String, data: Any?, handler: JSONResponseHandler) {
        OPTIONS(uri, data, useAbsoluteUris, JSONCallback(handler))
    }

    fun OPTIONS(uri: String, data: Any?, closure: (JSONObject) -> Unit) {
        OPTIONS(uri, data, useAbsoluteUris, JSONCallback(closure))
    }

    private fun jsonBuildErr(e: JSONException) {
        if (logLevel >= LOG_ERRORS) {
            Log.e(TAG, "JSON error when building error object: " + e.message)
        }
    }

    private fun nullBodyErr() {
        if (logLevel >= LOG_ERRORS) {
            Log.e(TAG, "HTTP Body sent to webservice method was empty")
        }
    }

    fun errorMsg(): JSONObject {
        return JSONObject(mapOf("success" to false))
    }

    fun errorMsg(msgs: Array<String?>?): JSONObject {
        val json = errorMsg()
        try {
            val errors = JSONArray(msgs)
            json.put("errors", errors)
        } catch (e: JSONException) {
            jsonBuildErr(e)
        }
        return json
    }

    fun errorMsg(msgs: Collection<*>?): JSONObject {
        val json = errorMsg()
        try {
            val errors = JSONArray(msgs)
            json.put("errors", errors)
        } catch (e: JSONException) {
            jsonBuildErr(e)
        }
        return json
    }

    fun errorMsg(msg: String): JSONObject {
        val json = errorMsg()
        try {
            val errors = JSONArray()
            if (msg.isEmpty()) {
                if (logLevel >= LOG_WARNINGS) Log.w(TAG, "Empty message passed to errorMsg")
                errors.put("unknown_error")
            } else {
                errors.put(msg)
            }
            json.put("errors", errors)
        } catch (e: JSONException) {
            jsonBuildErr(e)
        }
        return json
    }

    fun exceptionMsg(e: Exception, ident: String? = null): JSONObject {
        val json = errorMsg(arrayOf("internal", "exception", ident))
        val hash = JSONObject(hashException(e))
        json.put("exception", hash)
        return json
    }

    fun jsonResponse(response: Response): JSONObject {
        return if (!response.isSuccessful) {
            errorMsg(arrayOf("internal", "http_status", response.code.toString()))
        } else try {
            val body = response.body!!.string()
            if (logLevel >= LOG_DEBUG) Log.d(TAG, "Response body: $body")
            JSONObject(body)
        } catch (e: IOException) {
            exceptionMsg(e,"response_body_parsing")
        } catch (e: JSONException) {
            exceptionMsg(e, "json_parsing")
        }
    }

    fun hashException(e: Throwable): HashMap<String, Any?> {
        val errHash = HashMap<String, Any?>()
        errHash["message"] = e.message
        val errList = ArrayList<HashMap<String, Any>>()
        val stack = e.stackTrace
        for (stackTraceElement in stack) {
            val stackItem = HashMap<String, Any>()
            if (stackTraceElement.className != null)
                stackItem["class"] = stackTraceElement.className
            if (stackTraceElement.fileName != null)
                stackItem["file"] = stackTraceElement.fileName
            stackItem["line"] = stackTraceElement.lineNumber
            if (stackTraceElement.methodName != null)
                stackItem["method"] = stackTraceElement.methodName
            errList.add(stackItem)
        }
        errHash["stack"] = errList
        return errHash
    }

    class Builder : HTTP.Builder<Builder>() {
        override val `this`: Builder
            get() = this

        var useAbsoluteUris: Boolean = false
        var defaultRequestBody: RequestBody? = null

        fun setAbsoluteUris(value: Boolean): Builder {
            useAbsoluteUris = value
            return this
        }

        fun setDefaultRequestBody(body: RequestBody): Builder {
            defaultRequestBody = body
            return this
        }

        fun build(): JSON {
            return JSON(this)
        }
    }

    companion object {
        val TYPE_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        const val OPTIONS = "OPTIONS"
    }
}