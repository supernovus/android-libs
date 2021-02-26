package com.luminaryn.common

import org.json.JSONArray
import org.json.JSONObject

class MongoPatch {
    val data = JSONObject()

    fun reset () {
        data.remove("\$set")
        data.remove("\$unset")
        data.remove("\$rename")
        data.remove("\$push")
        data.remove("\$pop")
        data.remove("\$pull")
        data.remove("\$pullAll")
        data.remove("\$addToSet")
    }

    private fun patchValue (key: String, property: String, value: Any) {
        val existing = data.has(key)
        val pdata = if (existing) data.getJSONObject(key) else JSONObject()
        pdata.put(property, value)
        if (!existing)
            data.put(key, pdata)
    }

    fun set (property: String, value: Any) {
        patchValue("\$set", property, value)
    }

    fun unset (property: String) {
        patchValue("\$unset", property, "")
    }

    fun rename (oldname: String, newname: String) {
        patchValue("\$rename", oldname, newname)
    }

    fun push (property: String, value: Any) {
        patchValue("\$push", property, value)
    }

    @JvmOverloads
    fun pushAll (property: String, values: JSONArray, position: Int? = null) {
        val modifier = JSONObject()
        modifier.put("\$each", values)
        if (position != null) {
            modifier.put("\$position", position)
        }
        push(property, modifier)
    }

    @JvmOverloads
    fun pushAll (property: String, values: Collection<*>, position: Int? = null) {
        pushAll(property, JSONArray(values), position)
    }

    fun pop (property: String) {
        patchValue("\$pop", property, 1)
    }

    fun shift (property: String) {
        patchValue("\$pop", property, -1)
    }

    fun pull (property: String, conditions: JSONObject) {
        patchValue("\$pull", property, conditions)
    }

    fun pull (property: String, conditions: Map<*,*>) {
        pull(property, JSONObject(conditions))
    }

    fun pullAll (property: String, conditions: JSONArray) {
        patchValue("\$pullAll", property, conditions)
    }

    fun pullAll (property: String, conditions: Collection<*>) {
        pullAll(property, JSONArray(conditions))
    }

    fun addToSet (property: String, value: Any) {
        patchValue("\$addToSet", property, value)
    }
}