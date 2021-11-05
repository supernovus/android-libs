package com.luminaryn.common

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.lang.NumberFormatException

object Json {

    fun getOpt (opts: JSONObject?, name: String, default: Any?) : Any? {
        if (opts == null) return default

        if (default == null) {
            return opts.opt(name)
        }

        if (default is Boolean) return opts.optBoolean(name, default)
        if (default is Long) return opts.optLong(name, default)
        if (default is Int) return opts.optInt(name, default)
        if (default is Double) return opts.optDouble(name, default)
        if (default is Float) return opts.optDouble(name, default.toDouble()).toFloat()
        if (default is String) return opts.optString(name, default)
        if (default is JSONObject) return opts.optJSONObject(name) ?: default
        if (default is JSONArray) return opts.optJSONArray(name) ?: default
        if (default is ArrayList<*>) {
            val jsonArray = opts.optJSONArray(name)
            return if (jsonArray != null) toArrayList(jsonArray, true) else default
        }
        if (default is HashMap<*,*>) {
            val jsonObj = opts.optJSONObject(name)
            return if (jsonObj != null) toHashMap(jsonObj, true) else default
        }

        throw Exception("Invalid type in Json.getOpt")
    }

    fun getOpt (opts: JSONArray?, index: Int, default: Any?) : Any? {
        if (opts == null) return default

        if (default == null) {
            return opts.opt(index)
        }

        if (default is Boolean) return opts.optBoolean(index, default)
        if (default is Long) return opts.optLong(index, default)
        if (default is Int) return opts.optInt(index, default)
        if (default is Double) return opts.optDouble(index, default)
        if (default is Float) return opts.optDouble(index, default.toDouble()).toFloat()
        if (default is String) return opts.optString(index, default)
        if (default is JSONObject) return opts.optJSONObject(index) ?: default
        if (default is JSONArray) return opts.optJSONArray(index) ?: default
        if (default is ArrayList<*>) {
            val jsonArray = opts.optJSONArray(index)
            return if (jsonArray != null) toArrayList(jsonArray, true) else default
        }
        if (default is HashMap<*,*>) {
            val jsonObj = opts.optJSONObject(index)
            return if (jsonObj != null) toHashMap(jsonObj, true) else default
        }

        throw Exception("Invalid type in Json.getOpt")
    }

    private fun splitPath (path: String) : List<String> {
        return path.split('.','/')
    }

    fun findPath (opts: JSONArray?, path: String, default: Any?) : Any? {
        if (opts == null) return default
        return findPath(opts, splitPath(path), default)
    }

    fun findPath (opts: JSONObject?, path: List<*>, default: Any?) : Any? {
        if (opts == null) return default

        val name = path[0]

        if (name == null || name !is String) return default // Invalid name
        if (path.size == 1)  return getOpt(opts, name, default) // Last property.

        val subOpts = opts.opt(name)
        val subPath = path.subList(1, path.size)
        if (subOpts is JSONObject) { // Recursing into nested object.
            return findPath(subOpts, subPath, default)
        } else if (subOpts is JSONArray) { // Recursing into a nested array.
            return findPath(subOpts, subPath, default)
        } else {
            return default
        }
    }

    fun findPath (opts: JSONObject?, path: String, default: Any?) : Any? {
        if (opts == null) return default
        return findPath(opts, splitPath(path), default)
    }

    fun findPath (opts: JSONArray?, path: List<*>, default: Any?) : Any? {
        if (opts == null) return default

        var index = 0 // This will be set properly below.
        val name = path[0]

        if (name is String) { // Parse the string as a base10 integer.
            try {
                index = name.toInt()
            } catch (e: NumberFormatException) {
                return default
            }
        } else if (name is Int) { // The name was an integer.
            index = name
        } else { // Anything else is not supported.
            return default
        }

        if (path.size == 1)  return getOpt(opts, index, default) // Last property.

        val subOpts = opts.opt(index)
        val subPath = path.subList(1, path.size)
        if (subOpts is JSONObject) { // Recursing into nested object.
            return findPath(subOpts, subPath, default)
        } else if (subOpts is JSONArray) { // Recursing into a nested array.
            return findPath(subOpts, subPath, default)
        } else {
            return default
        }
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

    fun <T> toArrayListOf(jsonArray: JSONArray): ArrayList<T> {
        val list = ArrayList<T>()
        for (i in 0 until jsonArray.length()) {
            try {
                val item = jsonArray.get(i) as T
                list.add(item)
            } catch (e: Exception) {
                Log.v("LumJson", "Exception thrown while converting array", e)
            }
        }
        return list
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