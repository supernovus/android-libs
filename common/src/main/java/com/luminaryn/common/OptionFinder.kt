package com.luminaryn.common

import org.json.JSONArray
import org.json.JSONObject

/**
 * Look for an option in a few places.
 *
 * Try a JSONObject of passed options first.
 * Try a Settings object (may be a nested Settings object) next.
 * If neither has the option, return a default value.
 *
 * Doesn't support ALL of the methods or child types of the sources,
 * but enough to be used for common options in methods where there may be
 * specified options in a request document, or global settings to fall back on.
 *
 */
class OptionFinder (
    val settings: Settings?,
    val options: JSONObject?,
    private val defaultNesting: Boolean = true) {

    @JvmOverloads
    fun getBoolean(key: String, default: Boolean = false, nested: Boolean = defaultNesting): Boolean
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return options.optBoolean(key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is Boolean) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getBoolean(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getInt(key: String, default: Int = 0, nested: Boolean = defaultNesting): Int
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return options.optInt(key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is Int) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getInt(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getLong(key: String, default: Long = 0L, nested: Boolean = defaultNesting): Long
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return options.optLong(key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is Long) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getLong(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getFloat(key: String, default: Float = 0.0f, nested: Boolean = defaultNesting): Float
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return Json.optFloat(options, key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is Float) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getFloat(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getDouble(key: String, default: Double = 0.0, nested: Boolean = defaultNesting): Double
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return options.optDouble(key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is Double) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getDouble(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getString(key: String, default: String = "", nested: Boolean = defaultNesting): String
    {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                return options.optString(key, default)
            } else if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is String) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getString(key, default) ?: default
        }

        return default
    }

    @JvmOverloads
    fun getJSONArray(key: String, default: JSONArray? = null, nested: Boolean = defaultNesting): JSONArray?
    {
        if (options != null) { // We have options
            val nval = if (options.has(key)) options.optJSONObject(key)
            else if (nested) Json.findPath(options, key, default, null)
            else null
            if (nval is JSONArray) { // A valid boolean value was found.
                return nval
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getJSONArray(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getJSONObject(key: String, default: JSONObject? = null, nested: Boolean = defaultNesting): JSONObject?
    {
        if (options != null) { // We have options
            val nval = if (options.has(key)) options.optJSONObject(key)
            else if (nested) Json.findPath(options, key, default, null)
            else null
            if (nval is JSONObject) { // A valid boolean value was found.
                return nval
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getJSONObject(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getHashMap(key: String, default: HashMap<*,*>? = null, nested: Boolean = defaultNesting): HashMap<*,*>? {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                val job = options.optJSONObject(key)
                if (job != null)
                    return Json.toHashMap(job, true)
            }

            if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is HashMap<*,*>) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getHashMap(key, default)
        }

        return default
    }

    @JvmOverloads
    fun getArrayList(key: String, default: ArrayList<*>? = null, nested: Boolean = defaultNesting): ArrayList<*>? {
        if (options != null) { // We have options
            if (options.has(key)) { // Explicitly named property found.
                val job = options.optJSONArray(key)
                if (job != null)
                    return Json.toArrayList(job, true)
            }

            if (nested) { // Look for a nested property.
                val nval = Json.findPath(options, key, default, null)
                if (nval is ArrayList<*>) { // A valid boolean value was found.
                    return nval
                }
            }
        }

        if (settings != null && settings.contains(key)) { // Look in Settings.
            return settings.getArrayList(key, default)
        }

        return default
    }

    fun getNested(key: String, nested: Boolean = defaultNesting): OptionFinder
    {
        val nestedJson = getJSONObject(key, null, nested)
        val nestedSettings = settings?.getNested(key)
        return OptionFinder(nestedSettings, nestedJson)
    }

}