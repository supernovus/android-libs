package com.luminaryn.common

import org.json.JSONArray
import org.json.JSONObject

class Cache (val settings: Settings, val useKey: String?, val dataKey: String?) {

    val use: Boolean
        get () {
            if (!useKey.isNullOrBlank())  { // Enabled ability via key.
                return settings.getBoolean(useKey)
            }
            else return !dataKey.isNullOrBlank()
        }

    var data: JSONObject?
        get () {
            return if (!dataKey.isNullOrBlank()) { // We have a datakey, go get the data!
                settings.getJSONObject(dataKey, JSONObject())
            } else { // No datakey, it's always empty.
                null
            }
        }
        set (value) {
            if (!dataKey.isNullOrBlank()) {
                if (value == null) {
                    settings.remove(dataKey).save()
                }
                else {
                    settings.putJSONObject(dataKey, value).save()
                }
            }
        }

    fun update (key: String, value: Any?): Boolean {
        if (use) {
            val cdata = data;
            if (cdata != null) {
                cdata.put(key, value)
                data = cdata
                return true
            }
        }
        return false;
    }

    fun remove (key: String) {
        if (use) {
            val cdata = data;
            if (cdata != null && cdata.has(key)) {
                cdata.remove(key)
                data = cdata
            }
        }
    }

    fun getOpt (key: String, default: Any): Any {
        return Json.getOpt(data, key, default)
    }

    @JvmOverloads
    fun optJSONObject (key: String, default: JSONObject? = null): JSONObject? {
        if (use) {
            val json = data?.optJSONObject(key);
            if (json != null) {
                return json;
            }
        }
        return default
    }

    fun getJSONObject (key: String): JSONObject {
        return optJSONObject(key, JSONObject())!!
    }

    @JvmOverloads
    fun optJSONArray (key: String, default: JSONArray? = null): JSONArray? {
        if (use) {
            val json = data?.optJSONArray(key);
            if (json != null) {
                return json;
            }
        }
        return null
    }

    fun getJSONArray (key: String): JSONArray {
        return optJSONArray(key, JSONArray())!!
    }

}