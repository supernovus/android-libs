package com.luminaryn.common

import com.luminaryn.common.extensions.substr

object Text {
    fun truncateEnd(str: String, maxlen: Int, offset: Int = 0, append: String = "…"): String {
        if (str.length <= maxlen) return str
        return str.substr(offset, maxlen-append.length) + append
    }

    fun truncateJoin(str: String, maxlen: Int, offset: Int = 0, join: String = "…"): String {
        if (str.length <= maxlen) return str
        val margin = join.length + 1;
        val length = ((maxlen/2)-margin)
        val str1 = str.substr(offset, length)
        val endof = length * -1
        val str2 = str.substr(endof)
        return str1+join+str2
    }
}