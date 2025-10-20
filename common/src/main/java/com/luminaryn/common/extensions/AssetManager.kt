package com.luminaryn.common.extensions

import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream

fun AssetManager.copyRecursively(assetPath: String, targetFile: File, overwrite: Boolean = false) {
  if (targetFile.isFile && !overwrite) return // Nothing more to do here

  val list = list(assetPath)!!

  if (list.isEmpty()) { // assetPath is file
    open(assetPath).use { input ->
      FileOutputStream(targetFile.absolutePath).use { output ->
        input.copyTo(output)
        output.flush()
      }
    }

  } else { // assetPath is folder
    targetFile.delete()
    targetFile.mkdir()

    list.forEach {
      copyRecursively(
        "$assetPath/$it",
        File(targetFile, it),
        overwrite
      )
    }
  }
}
