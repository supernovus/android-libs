package com.luminaryn.webservice

import java.util.ArrayList
import java.util.HashMap

object Hashify {
    fun hashException(e: Throwable): java.util.HashMap<String, Any?> {
        val errHash = HashMap<String, Any?>()
        errHash["class"] = e.javaClass.canonicalName
        errHash["message"] = e.message
        val errList = ArrayList<HashMap<String, Any>>()
        val stack = e.stackTrace
        for (stackTraceElement in stack) {
            val stackItem = HashMap<String, Any>()
            if (stackTraceElement.className != null)
                stackItem["class"] = stackTraceElement.className
            if (stackTraceElement.fileName != null)
                stackItem["file"] = stackTraceElement.fileName
            stackItem["line"] = stackTraceElement.lineNumber
            if (stackTraceElement.methodName != null)
                stackItem["method"] = stackTraceElement.methodName
            errList.add(stackItem)
        }
        errHash["stack"] = errList
        return errHash
    }
}