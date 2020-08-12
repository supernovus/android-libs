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
 * Can be used standalone, or (preferrably) use a sub-class to add your method calls.
 */
open class JSON : HTTP {
    constructor() : super() {}
    constructor(url: String?) : super(url!!) {}
    protected constructor(builder: Builder?) : super(builder!!) {}

    var headers: Headers? = null;

    fun setHeaders(headers: Headers.Builder) {
        this.headers = headers.build()
    }

    interface JSONResponseHandler {
        fun handle(data: JSONObject)
    }

    abstract class JSONUIResponseHandler : JSONResponseHandler {
        abstract fun setup(data: JSONObject): Runnable
        val uIHandler: Handler
            get() = HTTP.uIHandler

        override fun handle(data: JSONObject) {
            uIHandler.post(setup(data))
        }
    }

    class JSONCallback internal constructor(private val handler: JSONResponseHandler, var ws: JSON) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            handler.handle(ws.errorMsg(arrayOf("internal", "http_failure", e.message)))
        }

        override fun onResponse(call: Call, response: Response) {
            handler.handle(ws.jsonResponse(response))
        }
    }

    fun jsonBody(data: JSONObject): RequestBody {
        return data.toString().toRequestBody(TYPE_JSON)
    }

    fun GET(uri: String?, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri!!, headers).get().build(), JSONCallback(handler, this))
    }

    fun POST(uri: String?, data: RequestBody?, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri!!, headers).post(data!!).build(), JSONCallback(handler, this))
    }

    fun POST(uri: String?, data: JSONObject, handler: JSONResponseHandler) {
        POST(uri, jsonBody(data), handler)
    }

    fun POST(uri: String?, data: Map<String?, Any?>?, handler: JSONResponseHandler) {
        POST(uri, JSONObject(data), handler)
    }

    fun PUT(uri: String?, data: RequestBody?, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri!!, headers).put(data!!).build(), JSONCallback(handler, this))
    }

    fun PUT(uri: String?, data: JSONObject, handler: JSONResponseHandler) {
        PUT(uri, jsonBody(data), handler)
    }

    fun PUT(uri: String?, data: Map<String?, Any?>?, handler: JSONResponseHandler) {
        PUT(uri, JSONObject(data), handler)
    }

    fun PATCH(uri: String?, data: RequestBody?, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri!!, headers).patch(data!!).build(), JSONCallback(handler, this))
    }

    fun PATCH(uri: String?, data: JSONObject, handler: JSONResponseHandler) {
        PATCH(uri, jsonBody(data), handler)
    }

    fun PATCH(uri: String?, data: Map<String?, Any?>?, handler: JSONResponseHandler) {
        PATCH(uri, JSONObject(data), handler)
    }

    fun DELETE(uri: String?, handler: JSONResponseHandler) {
        sendRequest(makeRequest(uri!!, headers).delete().build(), JSONCallback(handler, this))
    }

    private fun jsonBuildErr(e: JSONException) {
        if (logLevel >= LOG_ERRORS) {
            Log.e(TAG, "JSON error when building error object: " + e.message)
        }
    }

    fun errorMsg(msgs: Array<String?>?): JSONObject {
        val json = JSONObject()
        try {
            json.put("success", false)
            val errors = JSONArray(msgs)
            json.put("errors", errors)
        } catch (e: JSONException) {
            jsonBuildErr(e)
        }
        return json
    }

    fun errorMsg(msgs: Collection<*>?): JSONObject {
        val json = JSONObject()
        try {
            json.put("success", false)
            val errors = JSONArray(msgs)
            json.put("errors", errors)
        } catch (e: JSONException) {
            jsonBuildErr(e)
        }
        return json
    }

    fun errorMsg(msg: String): JSONObject {
        val json = JSONObject()
        try {
            json.put("success", false)
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

    fun jsonResponse(response: Response): JSONObject {
        return if (!response.isSuccessful) {
            errorMsg(arrayOf("internal", "http_status", response.code.toString()))
        } else try {
            val body = response.body!!.string()
            if (logLevel >= LOG_DEBUG) Log.d(TAG, "Response body: $body")
            JSONObject(body)
        } catch (e: IOException) {
            errorMsg(arrayOf("internal", "exception", "response_body_parsing", e.message))
        } catch (e: JSONException) {
            errorMsg(arrayOf("internal", "exception", "json_parsing", e.message))
        }
    }

    fun hashException(e: Exception): HashMap<String, Any?> {
        val errHash = HashMap<String, Any?>()
        errHash["message"] = e.message
        val errList = ArrayList<HashMap<String, Any>>()
        val stack = e.stackTrace
        for (stackTraceElement in stack) {
            val stackItem = HashMap<String, Any>()
            stackItem["class"] = stackTraceElement.className
            stackItem["file"] = stackTraceElement.fileName
            stackItem["line"] = stackTraceElement.lineNumber
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