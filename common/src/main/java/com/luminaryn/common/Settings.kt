package com.luminaryn.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Error
import java.lang.Exception

/**
 * A simple wrapper around the SharedPreferences library.
 *
 * @param context The application context we get the SharedPreferences from.
 * @param preferenceName The name of the shared preference store to get from the context.
 */
open class Settings(protected val context: Context, protected val preferenceName: String) {

    var preferences: SharedPreferences? = null
        get() {
            if (field == null) {
                field = context.getSharedPreferences(preferenceName, 0)
            }
            return field
        }
        private set

    protected var editor: SharedPreferences.Editor? = null
        @SuppressLint("CommitPrefEdits")
        get() {
            if (field == null) {
                field = preferences?.edit()
            }
            return field
        }

    /**
     * Cancel the current editing operation.
     */
    fun cancel() {
        editor = null
    }

    /**
     * Save any changes.
     *
     * @param atomic If true, we use commit() and return the result from it. If false we use apply()
     * If using apply() we return true. If there is no active editor we return false.
     */
    fun save(atomic: Boolean): Boolean {
        val editor = editor
        return if (editor != null) {
            if (atomic) {
                val ret = editor.commit()
                this.editor = null
                ret
            } else {
                editor.apply()
                this.editor = null
                true
            }
        } else {
            false
        }
    }

    /**
     * Save any changes.
     *
     * A wrapper for save(false); with no return value.
     */
    fun save() {
        save(false)
    }

    @JvmOverloads
    fun getBoolean(key: String, defValue: Boolean = false): Boolean {
        try {
            return preferences!!.getBoolean(key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getFloat(key: String, defValue: Float = 0.0f): Float {
        try {
            return preferences!!.getFloat(key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getDouble(key: String, defValue: Double = 0.0): Double {
        try {
            return Double.fromBits(preferences!!.getLong(key, defValue.toRawBits()))
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getInt(key: String, defValue: Int = 0): Int {
        try {
            return preferences!!.getInt(key, defValue)
        } catch (e: ClassCastException) {
            try {
                return preferences!!.getLong(key, defValue.toLong()).toInt()
            } catch (e2: ClassCastException) {
                return defValue
            }
        }
    }

    @JvmOverloads
    fun getLong(key: String, defValue: Long = 0L): Long {
        try {
            return preferences!!.getLong(key, defValue)
        } catch (e: ClassCastException) {
            try {
                return preferences!!.getInt(key, defValue.toInt()).toLong()
            } catch (e: ClassCastException) {
                return defValue
            }
        }
    }

    @JvmOverloads
    fun getString(key: String, defValue: String? = ""): String? {
        try {
            return preferences!!.getString(key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getStringSet(key: String, defValue: MutableSet<String>? = null): MutableSet<String>? {
        try {
            return preferences!!.getStringSet(key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getJSONObject(key: String, defValue: JSONObject? = null): JSONObject? {
        val jsonText = getString(key)
        return if (jsonText.isNullOrEmpty()) defValue else JSONObject(jsonText)
    }

    @JvmOverloads
    fun getJSONArray(key: String, defValue: JSONArray? = null): JSONArray? {
        val jsonText = getString(key)
        return if (jsonText.isNullOrEmpty()) defValue else JSONArray(jsonText)
    }

    @JvmOverloads
    fun getHashMap(key: String, defValue: HashMap<*,*>? = null): HashMap<*,*>? {
        val jsonText = getString(key)
        return if (jsonText.isNullOrEmpty())
            defValue
        else
            Json.toHashMap(JSONObject(jsonText), true)
    }

    @JvmOverloads
    fun getArrayList(key: String, defValue: ArrayList<*>? = null): ArrayList<*>? {
        val jsonText = getString(key)
        return if (jsonText.isNullOrEmpty())
            defValue
        else
            Json.toArrayList(JSONArray(jsonText), true);
    }

    /**
     * Get a Nested object that can look for further settings inside it, or fall back
     * to top-level properties in the main Settings object.
     *
     * This is a weird bastard system created for one of my projects migrating to a new nested
     * configuration model from an older flat model, while preserving backwards compatibility.
     */
    fun getNested(key: String): Nested {
        return Nested(getJSONObject(key))
    }

    fun contains(key: String): Boolean {
        return preferences!!.contains(key)
    }

    fun getAll(expandJSON: Boolean): Map<String, *> {
        val prefMap = preferences!!.all;
        if (expandJSON) {
            val expMap = HashMap<String, Any?>()
            for ((key, value) in prefMap) {
                if (value is String) {
                    if (value.startsWith('[') && value.endsWith(']')) {
                        expMap[key] = JSONArray(value)
                    }
                    else if (value.startsWith('{') && value.endsWith('}')) {
                        expMap[key] = JSONObject(value)
                    }
                    else { // Doesn't match a JSON value.
                        expMap[key] = value
                    }
                } else { // Is not a string.
                    expMap[key] = value
                }
            } // for prefMap
            return expMap
        } else {
            return prefMap
        }
    }

    val keys: Set<String>
        get () = getAll(false).keys

    fun putBoolean(key: String, value: Boolean): Settings {
        editor?.putBoolean(key, value)
        return this;
    }

    fun putFloat(key: String, value: Float): Settings {
        editor?.putFloat(key, value)
        return this
    }

    fun putDouble(key: String, value: Double): Settings {
        editor?.putLong(key, value.toRawBits())
        return this
    }

    fun putInt(key: String, value: Int): Settings {
        editor?.putInt(key, value);
        return this
    }

    fun putLong(key: String, value: Long): Settings {
        editor?.putLong(key, value)
        return this
    }

    fun putString(key: String, value: String): Settings {
        editor?.putString(key, value)
        return this
    }

    fun putStringSet(key: String, value: Set<String>): Settings {
        editor?.putStringSet(key, value)
        return this
    }

    fun putJSONObject(key: String, value: JSONObject): Settings {
        return putString(key, value.toString())
    }

    fun putJSONArray(key: String, value: JSONArray): Settings {
        return putString(key, value.toString())
    }

    fun putJSONObject(key: String, value: Map<*,*>): Settings {
        return putJSONObject(key, JSONObject(value))
    }

    fun putJSONArray(key: String, value: Collection<*>): Settings {
        return putJSONArray(key, JSONArray(value))
    }

    /**
     * A way to set a bunch of things at once.
     * This relies on type detection, and doesn't support the StringSet structure.
     * Pass it any kind of map where the key is a string, and the value is anything that
     * can be stored in a preference, and it'll do the rest!
     */
    fun putAll(values: Map<String, *>): Settings {
        for ((key, value) in values) {
            when (value) {
                null -> {
                    remove(key)
                }
                is Boolean -> {
                    putBoolean(key, value)
                }
                is Float -> {
                    putFloat(key, value)
                }
                is Double -> {
                    putDouble(key, value)
                }
                is Int -> {
                    putInt(key, value)
                }
                is Long -> {
                    putLong(key, value)
                }
                is String -> {
                    putString(key, value)
                }
                is JSONObject -> {
                    putJSONObject(key, value)
                }
                is JSONArray -> {
                    putJSONArray(key, value)
                }
                is Map<*,*> -> {
                    putJSONObject(key, value)
                }
                is Collection<*> -> {
                    putJSONArray(key, value)
                }
                else -> {
                    Log.v(TAG,"Unsupported data value: $value")
                }
            }
        }
        return this
    }

    /**
     * A different way to update multiple settings at once.
     * This one using a JSONObject as the source.
     *
     * Like putAll(), this does not support StringSet.
     */
    fun updateFromJSON(spec: JSONObject): Boolean {
        val keys: Iterator<*> = spec.keys()
        var updated = false;
        while (keys.hasNext()) {
            val key = keys.next() as String
            when (val value = spec.get(key)) {
                JSONObject.NULL -> {
                    if (contains(key)) {
                        remove(key)
                        updated = true;
                    }
                }
                is Boolean -> {
                    if (getBoolean(key) != value) {
                        putBoolean(key, value)
                        updated = true
                    }
                }
                is Float -> {
                    if (this.getFloat(key) != value) {
                        putFloat(key, value)
                        updated = true
                    }
                }
                is Double -> {
                    if (this.getDouble(key) != value) {
                        putDouble(key, value)
                        updated = true
                    }
                }
                is Int -> {
                    if (getInt(key) != value) {
                        putInt(key, value)
                        updated = true
                    }
                }
                is Long -> {
                    if (getLong(key) != value) {
                        putLong(key, value)
                        updated = true
                    }
                }
                is String -> {
                    if (getString(key) != value) {
                        putString(key, value)
                        updated = true
                    }
                }
                is JSONObject -> {
                    val curJson = getJSONObject(key)
                    if (curJson == null || !curJson.equals(value)) {
                        putJSONObject(key, value)
                        updated = true
                    }
                }
                is JSONArray -> {
                    val curJson = getJSONArray(key)
                    if (curJson == null || !curJson.equals(value)) {
                        putJSONArray(key, value)
                        updated = true
                    }
                }
                else -> {
                    Log.v(TAG, "Unsupported JSON value: $value")
                }
            }
        }
        return updated
    }

    fun remove(key: String): Settings {
        editor?.remove(key);
        return this
    }

    /**
     * A read-only nested object that can look for a property in a JSONObject, or find a fallback
     * property in the parent Settings object.
     *
     * Supports most of the same get functions as the parent, but not all of them.
     */
    inner class Nested(private val data: JSONObject?) {
        @JvmOverloads
        fun getBoolean(key: String, fbkey: String?, defValue: Boolean = false): Boolean {
            return if (data != null && data.has(key)) data.optBoolean(key, defValue)
            else getBoolean(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getFloat(key: String, fbkey: String?, defValue: Float = 0.0f): Float {
            return if (data != null && data.has(key))  data.optDouble(key, defValue.toDouble()).toFloat()
            else getFloat(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getDouble(key: String, fbkey: String?, defValue: Double = 0.0): Double {
            return if (data != null && data.has(key)) data.optDouble(key, defValue)
            else getDouble(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getInt(key: String, fbkey: String?, defValue: Int = 0): Int {
            return if (data != null && data.has(key)) data.optInt(key, defValue)
            else getInt(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getLong(key: String, fbkey: String?, defValue: Long = 0L): Long {
            return if (data != null && data.has(key)) data.optLong(key, defValue)
            else getLong(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getString(key: String, fbkey: String?, defValue: String? = ""): String? {
            return if (data != null && data.has(key)) data.optString(key, defValue)
            else this@Settings.getString(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getJSONObject(key: String, fbkey: String?, defValue: JSONObject? = null): JSONObject? {
            return if (data != null && data.has(key)) data.optJSONObject(key)
            else getJSONObject(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getJSONArray(key: String, fbkey: String?, defValue: JSONArray? = null): JSONArray? {
            return if (data != null && data.has(key)) data.optJSONArray(key)
            else getJSONArray(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getHashMap(key: String, fbkey: String?, defValue: HashMap<*,*>? = null): HashMap<*,*>? {
            if (data != null && data.has(key)) {
                val json = data.optJSONObject(key)
                if (json != null) {
                    return Json.toHashMap(json, true)
                }
            }

            return getHashMap(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun getArrayList(key: String, fbkey: String?, defValue: ArrayList<*>? = null): ArrayList<*>? {
            if (data != null && data.has(key)) {
                val json = data.optJSONArray(key)
                if (json != null) {
                    return Json.toArrayList(json, true)
                }
            }

            return getArrayList(fbkey ?: key, defValue)
        }

        @JvmOverloads
        fun contains(key: String, fbkey: String? = null): Boolean {
            return if (data != null && data.has(key)) true
            else this@Settings.contains(fbkey ?: key)
        }
    }

    companion object {
        const val TAG = "com.luminaryn.common.Settings"
    }

}