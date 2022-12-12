package com.luminaryn.common

import kotlin.math.absoluteValue

object LatLong {

    fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()

    @JvmOverloads
    fun toDMS (decimal: Double, dim: Dimension? = null): String {
        val degrees = decimal.toLong().absoluteValue
        val minsSecs = (decimal % 1) * 60
        val mins = minsSecs.toLong().absoluteValue
        val secs = ((minsSecs % 1) * 60).round(3)

        val direction = if (dim == null) "" else if (decimal >= 0) " ${dim.pos}" else " ${dim.neg}"

        return "$degrees° $mins' $secs\"$direction"
    }


    @JvmOverloads
    fun toDMS (latLong: Pair<Double, Double>, sep: CharSequence = ", ") : String {
        val lat = toDMS(latLong.first, Dimension.Latitude)
        val long = toDMS(latLong.second, Dimension.Longitude)

        return "$lat$sep$long"
    }

    @JvmOverloads
    fun toString (latLong: Pair<Double, Double>, sep: CharSequence = ", ") : String {
        return "${latLong.first}$sep${latLong.second}"
    }

    enum class Dimension(val pos: Char, val neg: Char) {
        Latitude('N', 'S'),
        Longitude('E', 'W'),
    }
}