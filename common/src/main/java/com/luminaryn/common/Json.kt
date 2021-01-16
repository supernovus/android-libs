package com.luminaryn.common

import org.json.JSONArray
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
        if (default is JSONObject) {
            val json = opts.optJSONObject(name)
            return if (json == null) default else json
        }
        if (default is JSONArray) {
            val json = opts.optJSONArray(name)
            return if (json == null) default else json
        }

        throw Exception("Invalid type in Json.getOpt")
    }

    fun toArrayList (jsonArray: JSONArray, recursive: Boolean): ArrayList<Any> {
        val arrayList = ArrayList<Any>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray[i];
            if (recursive && item is JSONArray) {
                arrayList.add(toArrayList(item, true))
            }
            else if (recursive && item is JSONObject) {
                arrayList.add(toHashMap(item, true))
            }
            else {
                arrayList.add(item)
            }
        }
        return arrayList
    }

    fun toHashMap (jsonObject: JSONObject, recursive: Boolean): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        for (key in jsonObject.keys()) {
            val item = jsonObject[key]
            if (recursive && item is JSONArray) {
                hashMap[key] = toArrayList(item, true)
            }
            else if (recursive && item is JSONObject) {
                hashMap[key] = toHashMap(item, true)
            }
            else {
                hashMap[key] = item
            }
        }
        return hashMap
    }

}