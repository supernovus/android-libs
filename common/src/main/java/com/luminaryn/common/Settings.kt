package com.luminaryn.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * A simple wrapper around the SharedPreferences library.
 *
 * @param context The application context we get the SharedPreferences from.
 * @param preferenceName The name of the shared preference store to get from the context.
 */
open class Settings(protected val context: Context, preferenceName: String?) {
    val preferences: SharedPreferences
    protected var editor: SharedPreferences.Editor? = null

    @SuppressLint("CommitPrefEdits")
    @JvmName("getEditorInstance")
    fun getEditor(): SharedPreferences.Editor? {
        if (editor == null) {
            editor = preferences.edit()
        }
        return editor
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
        return if (editor != null) {
            if (atomic) {
                editor!!.commit()
            } else {
                editor!!.apply()
                true
            }
        } else false
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
        return preferences.getBoolean(key, defValue)
    }


    @JvmOverloads
    fun getFloat(key: String, defValue: Float = 0f): Float {
        return preferences.getFloat(key, defValue)
    }

    @JvmOverloads
    fun getInt(key: String, defValue: Int = 0): Int {
        return preferences.getInt(key, defValue)
    }

    @JvmOverloads
    fun getLong(key: String, defValue: Long = 0): Long {
        return preferences.getLong(key, defValue)
    }

    @JvmOverloads
    fun getString(key: String, defValue: String? = ""): String? {
        return preferences.getString(key, defValue)
    }

    @JvmOverloads
    fun getStringSet(key: String, defValue: MutableSet<String>? = null): MutableSet<String>? {
        return preferences.getStringSet(key, defValue);
    }

    @JvmOverloads
    fun getJSONObject(key: String, defValue: JSONObject? = null): JSONObject? {
        val jsonText = getString(key);
        return if (jsonText.isNullOrEmpty()) defValue else JSONObject(jsonText);
    }

    @JvmOverloads
    fun getJSONArray(key: String, defValue: JSONArray? = null): JSONArray? {
        val jsonText = getString(key);
        return if (jsonText.isNullOrEmpty()) defValue else JSONArray(jsonText);
    }

    fun contains(key: String): Boolean {
        return preferences.contains(key);
    }

    fun getAll(): Map<String, *> {
        return preferences.getAll();
    }

    fun putBoolean(key: String, value: Boolean): Settings {
        getEditor()?.putBoolean(key, value);
        return this;
    }

    fun putFloat(key: String, value: Float): Settings {
        getEditor()?.putFloat(key, value);
        return this
    }

    fun putInt(key: String, value: Int): Settings {
        getEditor()?.putInt(key, value);
        return this
    }

    fun putLong(key: String, value: Long): Settings {
        getEditor()?.putLong(key, value);
        return this
    }

    fun putString(key: String, value: String): Settings {
        getEditor()?.putString(key, value);
        return this
    }

    fun putStringSet(key: String, value: Set<String>): Settings {
        getEditor()?.putStringSet(key, value);
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
            if (value is Boolean) {
                putBoolean(key, value)
            }
            else if (value is Float) {
                putFloat(key, value)
            }
            else if (value is Int) {
                putInt(key, value)
            }
            else if (value is Long) {
                putLong(key, value)
            }
            else if (value is String) {
                putString(key, value)
            }
            else if (value is JSONObject) {
                putJSONObject(key, value)
            }
            else if (value is JSONArray) {
                putJSONArray(key, value)
            }
            else if (value is Map<*,*>) {
                putJSONObject(key, value)
            }
            else if (value is Collection<*>) {
                putJSONArray(key, value)
            }
            else
            {
                throw Exception("Invalid value sent to Settings.putAll()")
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
            val value = spec.get(key);
            if (value is Boolean) {
                if (this.getBoolean(key) != value) {
                    this.putBoolean(key, value);
                    updated = true;
                }
            }
            else if (value is Float) {
                if (this.getFloat(key) != value) {
                    this.putFloat(key, value);
                    updated = true;
                }
            }
            else if (value is Long) {
                if (this.getLong(key) != value) {
                    this.putLong(key, value);
                    updated = true;
                }
            }
            else if (value is String) {
                if (this.getString(key) != value) {
                    this.putString(key, value);
                    updated = true;
                }
            }
            else if (value is JSONObject) {
                val curJson = this.getJSONObject(key)
                if (curJson == null || !curJson.equals(value)) {
                    this.putJSONObject(key, value);
                    updated = true;
                }
            }
            else if (value is JSONArray) {
                val curJson = this.getJSONArray(key);
                if (curJson == null || !curJson.equals(value)) {
                    this.putJSONArray(key, value);
                    updated = true;
                }
            }
        }
        return updated;
    }

    fun remove(key: String): Settings {
        getEditor()?.remove(key);
        return this
    }

    init {
        preferences = context.getSharedPreferences(preferenceName, 0)
    }
}