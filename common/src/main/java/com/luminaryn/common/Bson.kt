package com.luminaryn.common

import android.util.Base64
import org.json.JSONObject
import java.util.*

// TODO: add the rest of the BSON data structures.

object Bson {

    const val FORMAT_RELAXED = 0
    const val FORMAT_CANONICAL = 1
    const val FORMAT_V1 = 2

    const val NUMBER_LONG = "\$numberLong"

    @ExperimentalUnsignedTypes
    fun parseJSON (json: JSONObject) : BsonExtension? {
        return ObjectID.parseJSON(json) ?:
            Binary.parseJSON(json) ?:
            Date.parseJSON(json)
    }

    private fun logErr (e: Throwable) {
        val msg = e.localizedMessage
        if (msg != null) LongLog.v("Bson", msg)
    }

    abstract class BsonExtension (val outputFormat: Int) {
        abstract fun toJSON (outputFormat: Int = this.outputFormat): JSONObject
    }

    /**
     * The simplest and most common of the BsonExtensions
     * All three formats use the exact same representation.
     */
    class ObjectID (val id: String) : BsonExtension(FORMAT_RELAXED) {
        override fun toJSON(outputFormat: Int): JSONObject {
            val json = JSONObject()
            json.put(OBJECTID, id)
            return json
        }

        fun toInt (): Int {
            return id.toInt(16)
        }

        @ExperimentalUnsignedTypes
        fun toUInt() : UInt {
            return id.toUInt(16)
        }

        companion object {
            const val OBJECTID = "\$oid"

            fun parseJSON (json: JSONObject) : ObjectID? {
                if (json.has(OBJECTID)) {
                    val idString = json.optString(OBJECTID)
                    if (idString.isNotEmpty()) {
                        return ObjectID(idString)
                    }
                }
                return null
            }
        }
    }

    /**
     * Represents arbitrary binary data.
     */
    class Binary @ExperimentalUnsignedTypes
    constructor(val payload: ByteArray, val subtype: UByte, outputFormat: Int = FORMAT_RELAXED)
        : BsonExtension(outputFormat) {

        //@ExperimentalUnsignedTypes
        override fun toJSON(outputFormat: Int): JSONObject {
            val json = JSONObject()
            if (outputFormat == FORMAT_V1) {
                json.put(BINARY, Base64.encode(payload, Base64.NO_WRAP))
                json.put(BINARY_V1_TYPE, subtype.toString(16))
            }
            return json
        }

        companion object {
            const val BINARY = "\$binary"
            const val BINARY_PAYLOAD = "base64"
            const val BINARY_SUBTYPE = "subType"
            const val BINARY_V1_TYPE = "\$type"

            @ExperimentalUnsignedTypes
            const val SUBTYPE_GENERIC: UByte = 0u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_FUNCTION: UByte = 1u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_OLD_BINARY: UByte = 2u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_OLD_UUID: UByte = 3u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_UUID: UByte = 4u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_MD5: UByte = 5u
            @ExperimentalUnsignedTypes
            const val SUBTYPE_ENCRYPTED: UByte = 6u

            @ExperimentalUnsignedTypes
            fun parseJSON (json: JSONObject) : Binary? {
                if (json.has(BINARY)) {
                    if (json.has(BINARY_V1_TYPE)) {
                        val payloadStr = json.optString(BINARY)
                        val typeStr = json.optString(BINARY_V1_TYPE)
                        return parseBinaryJson(payloadStr, typeStr, FORMAT_V1)
                    } else {
                        val binData = json.optJSONObject(BINARY)
                        if (binData != null && binData.has(BINARY_PAYLOAD) && binData.has(BINARY_SUBTYPE)) {
                            val payloadStr = binData.optString(BINARY_PAYLOAD)
                            val typeStr = binData.optString(BINARY_SUBTYPE)
                            return parseBinaryJson(payloadStr, typeStr, FORMAT_RELAXED)
                        }
                    }
                }
                return null
            }

            @ExperimentalUnsignedTypes
            private fun parseBinaryJson (payloadStr: String, typeStr: String, format: Int) : Binary? {
                if (payloadStr.isNotEmpty() && typeStr.isNotEmpty()) {
                    try {
                        val payload = Base64.decode(payloadStr, Base64.DEFAULT)
                        val subtype = typeStr.toUByte(16)
                        return Binary(payload, subtype, format)
                    } catch (e: Throwable) {
                        logErr(e)
                    }
                }

                return null
            }
        }
    }

    class Date (val calendar: Calendar, outputFormat: Int = FORMAT_RELAXED) : BsonExtension(outputFormat) {
        override fun toJSON(outputFormat: Int): JSONObject {
            val json = JSONObject()
            val ts = calendar.timeInMillis
            val year = calendar.get(Calendar.YEAR)
            val allowRelaxed = (year in 1970..9999)
            if (outputFormat == FORMAT_V1 || (outputFormat == FORMAT_RELAXED && allowRelaxed)) {
                val isoStr = DateParser.fromMilliseconds(ts, DateParser.ISO_FULL)
                json.put(DATE, isoStr)
            } else {
                val longStr = ts.toString(10)
                val numObj = JSONObject()
                numObj.put(NUMBER_LONG, longStr)
                json.put(DATE, numObj)
            }

            return json
        }

        companion object {
            const val DATE = "\$date"

            fun parseJSON (json: JSONObject) : Date? {
                if (json.has(DATE)) {
                    val dateObj = json.optJSONObject(DATE)
                    if (dateObj != null) { // It was a Date object.
                        if (dateObj.has(NUMBER_LONG)) {
                            val dateNum = dateObj.optLong(NUMBER_LONG)
                            return dateFromLong(dateNum, FORMAT_CANONICAL)
                        }
                    } else { // Not an object, check for a string.
                        val dateStr = json.optString(DATE)
                        if (dateStr.isNotEmpty()) {
                            try {
                                val dateNum = DateParser.toMilliseconds(dateStr, DateParser.ISO_FULL)
                                return dateFromLong(dateNum, FORMAT_RELAXED)
                            } catch (e: Throwable) {
                                logErr(e)
                            }
                        }
                    }
                }
                return null
            }

            private fun dateFromLong (dateNum: Long, format: Int): Date {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateNum
                return Date(calendar, format)
            }
        }
    }

}