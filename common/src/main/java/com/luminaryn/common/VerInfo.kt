package com.luminaryn.common

object VerInfo {
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
        var mi: String
        var pa: String

        when {
            vl >= 7 -> { // Major version over 0
                ma = verCode.substring(0, vl-6)
                mi = verCode.substring(vl-6, 3).trim('0')
                pa = verCode.substring(vl-3, 3).trim('0')
            }
            vl >= 4 -> { // Major version 0, Minor version over 0
                ma = "0"
                mi = verCode.substring(0, vl-3)
                pa = verCode.substring(vl-3, 3).trim('0')
            }
            else -> { // Both major and minor 0.
                ma = "0"
                mi = "0"
                pa = verCode;
            }
        }

        if (mi.isEmpty()) mi = "0"
        if (pa.isEmpty()) pa = "0"

        return "${ma}.${mi}.${pa}"
    }
}