package com.luminaryn.common.extensions

fun Int.isFlag(flag: Int): Boolean {
    return (this and flag == flag)
}
