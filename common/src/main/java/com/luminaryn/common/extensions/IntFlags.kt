package com.luminaryn.common.extensions

fun Int.isFlag(flag: Int): Boolean {
    return (this and flag == flag)
}

fun Int.hasFlags(flags: Int): Boolean {
    return (this and flags != 0)
}

fun Int.setFlags(flags: Int, value: Boolean): Int {
    return if (value) {
        this or flags
    } else {
        this - (this and flags)
    }
}

fun Long.isFlag(flag: Long): Boolean {
    return (this and flag == flag)
}

fun Long.hasFlags(flags: Long): Boolean {
    return (this and flags != 0L)
}

fun Long.setFlags(flags: Long, value: Boolean): Long {
    return if (value) {
        this or flags
    } else {
        this - (this and flags)
    }
}
