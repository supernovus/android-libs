package com.luminaryn.common

import org.json.JSONArray

class PathFinder (val pathList: ArrayList<String>, val stripRoot: String?) {

    constructor(jsonList: JSONArray, stripRoot: String?) : this(jsonToArray(jsonList), stripRoot)

    var debugLevel: Int? = null

    @JvmOverloads
    fun contains (targetPath: String, fileName: String? = null, debugLevel: Int? = this.debugLevel): Boolean {
        val path = if (stripRoot.isNullOrEmpty()) targetPath else targetPath.removePrefix(stripRoot) 
        return containsPath(pathList, path, fileName, debugLevel)
    }

    companion object {

        var defaultDebugLevel = 0

        fun jsonToArray (json: JSONArray): ArrayList<String> {
            val array = ArrayList<String>()
            for (i in 0 until json.length()) {
                val item = json.optString(i)
                if (item.isNotEmpty()) {
                    array.add(item)
                }
            }
            return array
        }

        fun containsPath (list: ArrayList<String>, currentPath: String, fileName: String?, debugLevel: Int? = null): Boolean {
            val debug = Debug("LumPathFinder", debugLevel ?: defaultDebugLevel)

            debug.i(1, "PathFinder::containsPath(list, $currentPath, $fileName)")
            debug.v(2, "list = $list")

            if (list.size > 0) {

                val foundPath = fun (msg: String): Boolean {
                    debug.i(1, msg)
                    return true
                }

                // If the filename and path are both empty, we're  done here.
                if (fileName.isNullOrEmpty() && currentPath.isEmpty()) {
                    debug.i(1, "Both currentPath and fileName were empty")
                    return false
                }

                // If the list contains this specific file, it's a go.
                if (fileName != null && list.contains("$currentPath/$fileName")) {
                    return foundPath("currentPath/fileName was a exact match to a rule")
                }

                // If the list contains this specific path, it's a go.
                if (currentPath.isNotEmpty() && list.contains(currentPath)) {
                    return foundPath("currentPath is an exact match to a rule")
                }

                // Next we'll do some more fuzzy searches.
                for (item in list) {
                    debug.v(1, "Looking for $item matches")
                    if (item.startsWith('#')) continue; // Skip comments.
                    if (item.startsWith('*') && item.endsWith('*')) {
                        val subPath = item.substring(1, item.length - 1)
                        debug.d(2, "Seeing if currentPath or fileName contain \"$subPath\"")
                        if (currentPath.contains(subPath)) return foundPath("currentPath contained \"$subPath\"")
                        if (fileName != null && fileName.contains(subPath)) return foundPath("fileName contained \"$subPath\"")
                    }
                    else if (item.endsWith('*')) {
                        val subPath = item.substring(0, item.length - 1)
                        debug.d(2, "Seeing if currentPath or fileName starts with \"$subPath\"")
                        if (currentPath.startsWith(subPath)) return foundPath("currentPath starts with \"$subPath\"")
                        if (fileName != null && fileName.startsWith(subPath)) return foundPath("fileName starts with \"$subPath\"")
                    }
                    else if (item.startsWith("*")) {
                        val subPath = item.substring(1)
                        debug.d(2, "Seeing if currentPath or fileName ends with \"$subPath\"")
                        if (currentPath.endsWith(subPath)) return foundPath("currentPath ends with \"$subPath\"")
                        if (fileName != null && fileName.endsWith(subPath)) return foundPath("fileName ends with \"$subPath\"")
                    }
                }
            }

            debug.i(1, "Nothing matched")
            return false;
        } // isPathInList()

    }
}