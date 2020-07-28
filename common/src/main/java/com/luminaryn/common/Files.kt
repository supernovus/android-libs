package com.luminaryn.common

import java.io.File

object Files {
    @JvmStatic
    fun getExtension(filename: String): String {
        return File(filename).extension;
    }
}
