package com.luminaryn.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.ArrayMap
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * A simple wrapper around the SharedPreferences library.
 *
 * @param context The application context we get the SharedPreferences from.
 * @param preferenceName The name of the shared preference store to get from the context.
 * @param separator The character that should be used to separate nested namespaces.
 * @param prefix The current prefix including separator, you shouldn't have to ever use this directly.
 */
class Settings(val context: Context,
                    val preferenceName: String,
                    val separator: String = DEFAULT_SEP,
                    val prefix: String = "",
                    val nestedProp: String = NESTED_PROP
) {

    constructor(prefix: String, parent: Settings) : this(parent.context, parent.preferenceName,
        parent.separator, prefix, parent.nestedProp) {
        this.preferences = parent.preferences
        this.editor = parent.editor
    }

    var preferences: SharedPreferences? = null
        get() {
            if (field == null) {
                field = context.getSharedPreferences(preferenceName, 0)
            }
            return field
        }

    var editor: SharedPreferences.Editor? = null
        @SuppressLint("CommitPrefEdits")
        get() {
            if (field == null) {
                field = preferences?.edit()
            }
            return field
        }

    protected val nestedNamespaces = ArrayMap<String, Settings>()

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
            return preferences!!.getBoolean(prefix+key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getFloat(key: String, defValue: Float = 0.0f): Float {
        try {
            return preferences!!.getFloat(prefix+key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getDouble(key: String, defValue: Double = 0.0): Double {
        try {
            return Double.fromBits(preferences!!.getLong(prefix+key, defValue.toRawBits()))
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getInt(key: String, defValue: Int = 0): Int {
        try {
            return preferences!!.getInt(prefix+key, defValue)
        } catch (e: ClassCastException) {
            try {
                return preferences!!.getLong(prefix+key, defValue.toLong()).toInt()
            } catch (e2: ClassCastException) {
                return defValue
            }
        }
    }

    @JvmOverloads
    fun getLong(key: String, defValue: Long = 0L): Long {
        try {
            return preferences!!.getLong(prefix+key, defValue)
        } catch (e: ClassCastException) {
            try {
                return preferences!!.getInt(prefix+key, defValue.toInt()).toLong()
            } catch (e: ClassCastException) {
                return defValue
            }
        }
    }

    @JvmOverloads
    fun getString(key: String, defValue: String? = ""): String? {
        try {
            return preferences!!.getString(prefix+key, defValue)
        } catch (e: ClassCastException) {
            return defValue
        }
    }

    @JvmOverloads
    fun getStringSet(key: String, defValue: MutableSet<String>? = null): MutableSet<String>? {
        try {
            return preferences!!.getStringSet(prefix+key, defValue)
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
     * Get a nested Settings instance using a prefix.
     */
    fun getNested(key: String): Settings {
        if (nestedNamespaces.containsKey(key))
            return nestedNamespaces[key]!!

        val nested = Settings(prefix+key+separator, this)
        nestedNamespaces[key] = nested
        return nested
    }

    fun contains(key: String): Boolean {
        return preferences!!.contains(prefix+key)
    }

    val allPreferences: Map<String, *> = preferences!!.all

    @JvmOverloads
    fun getAll(expandNested: Boolean = true, expandJSON: Boolean = true): Map<String, *> {
        Log.v(TAG, "getAll[ns=$prefix]")
        val expMap = HashMap<String, Any?>()
        for ((skey, value) in allPreferences) {
            val tkey: String
            if (prefix.isNotEmpty()) { // We have a prefix, let's do this people!
                if (!skey.startsWith(prefix)) continue // Key doesn't have what we need.
                tkey = skey.removePrefix(prefix)
            } else {
                tkey = skey
            }

            if (expandNested && tkey.contains(separator)) {
                val nsparts = tkey.split(separator, limit = 2)
                val ns = nsparts[0]
                if (!expMap.containsKey(ns)) {
                    expMap[ns] = getNested(ns).getAll(expandNested, expandJSON)
                }
            } else if (expandJSON && value is String) { // It's not a namespace.
                if (value.startsWith('[') && value.endsWith(']')) {
                    expMap[tkey] = JSONArray(value)
                } else if (value.startsWith('{') && value.endsWith('}')) {
                    expMap[tkey] = JSONObject(value)
                } else { // Doesn't match a JSON value.
                    expMap[tkey] = value
                }
            } else { // Is not a string, or we're not using expandJSON.
                expMap[tkey] = value
            }

        } // for prefMap
        return expMap
    }

    /**
     * Convert all the data from this Settings instance into a JSON object.
     */
    fun toJSON(): JSONObject {
        return JSONObject(getAll())
    }

    fun putBoolean(key: String, value: Boolean): Settings {
        editor?.putBoolean(prefix+key, value)
        return this;
    }

    fun putFloat(key: String, value: Float): Settings {
        editor?.putFloat(prefix+key, value)
        return this
    }

    fun putDouble(key: String, value: Double): Settings {
        editor?.putLong(prefix+key, value.toRawBits())
        return this
    }

    fun putInt(key: String, value: Int): Settings {
        editor?.putInt(prefix+key, value);
        return this
    }

    fun putLong(key: String, value: Long): Settings {
        editor?.putLong(prefix+key, value)
        return this
    }

    fun putString(key: String, value: String): Settings {
        editor?.putString(prefix+key, value)
        return this
    }

    fun putStringSet(key: String, value: Set<String>): Settings {
        editor?.putStringSet(prefix+key, value)
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
     *
     * This does support setting nested namespaces using nested JSON objects.
     * To tell it to do that instead of the fallback behaviour of serializing the JSON,
     * add an extra property to the nested JSON object called "__" and set it to true.
     *
     * @param spec The JSON document that we're updating from.
     */
    fun updateFromJSON(spec: JSONObject): Boolean {
        Log.v(TAG, "updateFromJSON[ns=$prefix]")
        val keys: Iterator<*> = spec.keys()
        var updated = false;

        while (keys.hasNext()) {
            val key = keys.next() as String

            //Log.v(TAG, "Updating $key")

            when (val value = spec.get(key)) {
                JSONObject.NULL -> {
                    if (contains(key)) {
                        remove(key)
                        updated = true;
                    }
                }
                is Boolean -> {
                    if (!contains(key) || getBoolean(key) != value) {
                        putBoolean(key, value)
                        updated = true
                    }
                }
                is Float -> {
                    if (!contains(key) || getFloat(key) != value) {
                        putFloat(key, value)
                        updated = true
                    }
                }
                is Double -> {
                    if (!contains(key) || getDouble(key) != value) {
                        putDouble(key, value)
                        updated = true
                    }
                }
                is Int -> {
                    if (!contains(key) || getInt(key) != value) {
                        putInt(key, value)
                        updated = true
                    }
                }
                is Long -> {
                    if (!contains(key) || getLong(key) != value) {
                        putLong(key, value)
                        updated = true
                    }
                }
                is String -> {
                    if (!contains(key) || getString(key) != value) {
                        putString(key, value)
                        updated = true
                    }
                }

                is JSONObject -> {
                    //Log.v(TAG,"-- Is JSONObject")
                    if (value.optBoolean(nestedProp)) {
                        //Log.v(TAG,"-- Is a Nested namespace")
                        val nested = getNested(key)
                        if (nested.updateFromJSON(value)) {
                            updated = true
                        }
                    } else {
                        //Log.v(TAG, "-- Is embedded JSON")
                        val curJson = getJSONObject(key)
                        if (curJson == null || !curJson.equals(value)) {
                            putJSONObject(key, value)
                            updated = true
                        }
                    }
                }
                is JSONArray -> {
                    //Log.v(TAG, "-- Is JSONArray")
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

    fun removeAll(keys: List<String>): Settings {
        for (key in keys) {
            remove(key)
        }
        return this
    }

    fun removeAll(keys: Array<String>): Settings {
        for (key in keys) {
            remove(key)
        }
        return this
    }

    fun removeAll() {
        removeAll(allPreferences.keys.toList())
    }

    companion object {
        const val TAG = "com.luminaryn.common.Settings"
        const val NESTED_PROP = "__"
        const val DEFAULT_SEP = "."
    }

}