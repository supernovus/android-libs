package com.luminaryn.common

import com.luminaryn.common.extensions.substr

object VerInfo {

    fun String.trim0() : String {
        val trimmed = this.replace(Regex("^0+"),"")
        return if (trimmed.isNotEmpty()) trimmed else "0"
    }

    fun verCode (major: Int, minor: Int, patch: Int) : Int {
        return major * 1000000 + minor * 1000 + patch;
    }

    fun verCode (verName: String) : Int {
        val v = verName.split('.')
        val major = v[0].toInt()
        val minor = if (v.size >= 2) v[1].toInt() else 0
        val patch = if (v.size >= 3) v[2].toInt() else 0
        return verCode(major, minor, patch);
    }

    fun verName (verCode: Int) : String {
        return verName(verCode.toString())
    }

    fun verName (verCode: String) : String {
        val vl = verCode.length;

        val ma: String
        val mi: String
        val pa: String

        when {
            vl >= 7 -> { // Major version over 0
                ma = verCode.substr(0, vl-6)
                mi = verCode.substr(vl-6, 3).trim0()
                pa = verCode.substr(vl-3, 3).trim0()
            }
            vl >= 4 -> { // Major version 0, Minor version over 0
                ma = "0"
                mi = verCode.substr(0, vl-3)
                pa = verCode.substr(vl-3, 3).trim0()
            }
            else -> { // Both major and minor 0.
                ma = "0"
                mi = "0"
                pa = verCode;
            }
        }

        return "${ma}.${mi}.${pa}"
    }
}