package com.luminaryn.common.extensions

fun String.substr (offset: Int, length: Int? = null): String {
    val start = if (offset < 0) this.length+offset else offset
    if (length == null) return this.substring(start)
    val end = if (start+length > this.length) this.length else start+length
    return this.substring(start, end)
}
