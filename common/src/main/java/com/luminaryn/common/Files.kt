package com.luminaryn.common

import java.io.File
import java.util.*

object Files {
    @JvmStatic
    fun getExtension(filename: String): String {
        return File(filename).extension;
    }

    fun isImage(filename: String): Boolean {
        val ext = getExtension(filename).uppercase(Locale.getDefault())
        return when (ext) {
            "JPG", "PNG", "JPEG", "WEBP", "HEIC", "GIF" -> true
            else -> false
        }
    }

    fun isVideo(filename: String): Boolean {
        val ext = getExtension(filename).uppercase(Locale.getDefault())
        return when (ext) {
            "MP4", "MKV", "AVI", "WEBM", "M4V", "MPG", "MPEG", "MPEG4", "MOV" -> true
            else -> false
        }
    }
}
