package com.luminaryn.common

import android.util.Log
import java.util.*

/**
 * A simple wrapper around Log which can split really long logs that would normally
 * be truncated into multiple parts and output the parts individually.
 *
 * This has no support for passing Throwables, and doesn't have the wtf() level logs.
 * Use the main Log class if you need those.
 */
object LongLog {
    const val MAXLEN = 4000
    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    const val ASSERT = Log.ASSERT
    var SHOW_PAGER = true
    var SHOW_LENGTH = false

    @JvmStatic
    fun splitLog(msg: String, showPager: Boolean, showLength: Boolean): ArrayList<String> {
        val msgs = ArrayList<String>()
        val len = msg.length
        if (len > MAXLEN) {
            if (showLength) {
                msgs.add("<<Log.length=$len>>")
            }
            val chunkCount = len / MAXLEN
            for (i in 0..chunkCount) {
                val prefix = if (showPager) "<$i/$chunkCount> " else ""
                val max = MAXLEN * (i + 1)
                val offset = MAXLEN * i
                if (max >= len) {
                    msgs.add(prefix + msg.substring(offset))
                } else {
                    msgs.add(prefix + msg.substring(offset, max))
                }
            }
        } else {
            msgs.add(msg)
        }
        return msgs
    }

    @JvmOverloads
    @JvmStatic
    fun log(prio: Int, tag: String?, msg: String, showPager: Boolean = SHOW_PAGER, showLength: Boolean = SHOW_LENGTH): Int {
        return if (msg.length > MAXLEN) {
            val msgs = splitLog(msg, showPager, showLength)
            var count = 0
            for (msgi in msgs) {
                count += Log.println(prio, tag, msgi)
            }
            count
        } else {
            Log.println(prio, tag, msg)
        }
    }

    @JvmStatic
    fun v(tag: String?, msg: String): Int {
        return log(VERBOSE, tag, msg)
    }

    @JvmStatic
    fun d(tag: String?, msg: String): Int {
        return log(DEBUG, tag, msg)
    }

    @JvmStatic
    fun i(tag: String?, msg: String): Int {
        return log(INFO, tag, msg)
    }

    @JvmStatic
    fun w(tag: String?, msg: String): Int {
        return log(WARN, tag, msg)
    }

    @JvmStatic
    fun e(tag: String?, msg: String): Int {
        return log(ERROR, tag, msg)
    }
}