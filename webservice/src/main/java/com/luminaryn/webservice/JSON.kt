package com.luminaryn.webservice

import android.os.Handler
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
    constructor() : super()
    constructor(url: String) : super(url)
    protected constructor(builder: Builder) : super(builder)

    var defaultRequestBody: RequestBody? = null

    interface JSONResponseHandler {
        fun handle(data: JSONObject)
    }

    inner class JSONCallback internal constructor(private val handler: JSONResponseHandler) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            handler.handle(errorMsg(arrayOf("internal", "http_failure", e.message)))
        }

        override fun onResponse(call: Call, response: Response) {
            handler.handle(jsonResponse(response))
        }
    }

    fun fromClosure (callback: JSONClosure): JSONResponseHandler {
        return object : JSONResponseHandler {
            override fun handle(data: JSONObject) {
               callback(data)
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

    fun GET(uri: String, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri, headers).get().build(), JSONCallback(handler))
    }

    fun GET(uri: String, closure: JSONClosure) {
        GET(uri, fromClosure(closure))
    }

    fun POST(uri: String, data: RequestBody, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri, headers).post(data).build(), JSONCallback(handler))
    }

    fun POST(uri: String, data: JSONObject, handler: JSONResponseHandler) {
        POST(uri, jsonBody(data), handler)
    }

    fun POST(uri: String, data: JSONishMap, handler: JSONResponseHandler) {
        POST(uri, JSONObject(data), handler)
    }

    fun POST(uri: String, data: Any, closure: JSONClosure) {
        val body = detectData(data)
        if (body != null) {
            POST(uri, body, fromClosure(closure))
        }
    }

    fun PUT(uri: String, data: RequestBody, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri, headers).put(data).build(), JSONCallback(handler))
    }

    fun PUT(uri: String, data: JSONObject, handler: JSONResponseHandler) {
        PUT(uri, jsonBody(data), handler)
    }

    fun PUT(uri: String, data: Map<String?, Any?>, handler: JSONResponseHandler) {
        PUT(uri, JSONObject(data), handler)
    }

    fun PUT(uri: String, data: Any, closure: (JSONObject) -> Unit) {
        val body = detectData(data)
        if (body != null) {
            PUT(uri, body, fromClosure(closure))
        }
    }

    fun PATCH(uri: String, data: RequestBody, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri, headers).patch(data).build(), JSONCallback(handler))
    }

    fun PATCH(uri: String, data: JSONObject, handler: JSONResponseHandler) {
        PATCH(uri, jsonBody(data), handler)
    }

    fun PATCH(uri: String, data: Map<String?, Any?>, handler: JSONResponseHandler) {
        PATCH(uri, JSONObject(data), handler)
    }

    fun PATCH(uri: String, data: Any, closure: (JSONObject) -> Unit) {
        val body = detectData(data)
        if (body != null) {
            PATCH(uri, body, fromClosure(closure))
        }
    }

    fun DELETE(uri: String, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri, headers).delete().build(), JSONCallback(handler))
    }

    fun DELETE(uri: String, closure: (JSONObject) -> Unit) {
        DELETE(uri, fromClosure(closure))
    }

    fun HTTP(method: String, uri: String, data: Any?, handler: JSONResponseHandler) {
        val body = detectData(data)
        sendRequest(makeRequest(uri, headers).method(method, body).build(), JSONCallback(handler))
    }

    fun HTTP(method: String, uri: String, data: Any?, closure: (JSONObject) -> Unit) {
        HTTP(method, uri, data, fromClosure(closure))
    }

    fun OPTIONS(uri: String, data: Any?, handler: JSONResponseHandler) {
        HTTP("OPTIONS", uri, data, handler)
    }

    fun OPTIONS(uri: String, data: Any?, closure: (JSONObject) -> Unit) {
        OPTIONS(uri, data, fromClosure(closure))
    }

    private fun jsonBuildErr(e: JSONException) {
        if (logLevel >= LOG_ERRORS) {
            Log.e(TAG, "JSON error when building error object: " + e.message)
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

    fun hashException(e: Exception): HashMap<String, Any?> {
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

        fun build(): JSON {
            return JSON(this)
        }
    }

    companion object {
        val TYPE_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
}