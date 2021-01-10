package com.luminaryn.common

import org.json.JSONObject

object Json {
    fun getOpt (opts: JSONObject?, name: String, default: Any) : Any {
        if (opts == null) return default
        if (default is Boolean) return opts.optBoolean(name, default)
        if (default is Long) return opts.optLong(name, default)
        if (default is Int) return opts.optInt(name, default)
        if (default is Double) return opts.optDouble(name, default)
        if (default is Float) return opts.optDouble(name, default.toDouble()).toFloat()
        if (default is String) return opts.optString(name, default)
        throw Exception("Invalid type in Json.getOpt")
    }
}