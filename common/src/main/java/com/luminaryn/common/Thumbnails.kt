package com.luminaryn.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.IOException

/**
 * Helper class for making Base64 encoded thumbnails.
 */
object Thumbnails {

    const val DEFAULT_WIDTH = 96
    const val DEFAULT_HEIGHT = 96
    const val DEFAULT_VIDTYPE = MediaStore.Video.Thumbnails.MICRO_KIND
    const val DEFAULT_QUALITY = 90

    @JvmStatic
    @JvmOverloads
    fun thumbnail(filepath: String,
                  width: Int = DEFAULT_WIDTH,
                  height: Int = DEFAULT_HEIGHT,
                  vidType: Int = DEFAULT_VIDTYPE): Bitmap? {
        val ext = Files.getExtension(filepath).toUpperCase()
        if (ext == "") {
            return null
        }
        return when (ext) {
            "JPG", "PNG", "JPEG", "WEBP", "HEIC", "GIF" -> fromImage(filepath, width, height)
            "MP4", "MKV", "AVI", "WEBM", "M4V", "MPG", "MPEG", "MPEG4", "MOV" -> fromVideo(filepath, vidType)
            else -> null
        }
    }
    
    @JvmStatic
    @JvmOverloads
    fun base64Thumbnail(filepath: String,
                        width: Int = DEFAULT_WIDTH,
                        height: Int = DEFAULT_HEIGHT,
                        vidType: Int = DEFAULT_VIDTYPE,
                        quality: Int = DEFAULT_QUALITY): String {
        val thumbnail = thumbnail(filepath, width, height, vidType)
        return if (thumbnail != null) bitmapToBase64(thumbnail, quality) else ""
    }

    @JvmStatic
    @JvmOverloads
    fun bitmapToBase64(bitmap: Bitmap?, quality: Int = DEFAULT_QUALITY): String {
        if (bitmap != null) {
            try {
                return Images.base64Encode(bitmap, quality)
            } catch (e: IOException) {
                // This doesn't do anything, just ensures that IOExceptions return ""
            }
        }
        return ""
    }

    @JvmStatic
    @JvmOverloads
    fun fromImage(filePath: String,
                        width: Int = DEFAULT_WIDTH,
                        height: Int = DEFAULT_HEIGHT): Bitmap {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath), width, height)
    }
    
    @JvmStatic
    @JvmOverloads
    fun base64FromImage(filePath: String,
                        width: Int = DEFAULT_WIDTH,
                        height: Int = DEFAULT_HEIGHT,
                        quality: Int = DEFAULT_QUALITY): String {
        return bitmapToBase64(fromImage(filePath, width, height), quality)
    }

    @JvmStatic
    @JvmOverloads
    fun fromVideo(filePath: String,
                        vidType: Int = DEFAULT_VIDTYPE): Bitmap {
        return ThumbnailUtils.createVideoThumbnail(filePath, vidType)
    }
    
    @JvmStatic
    @JvmOverloads
    fun base64FromVideo(filePath: String,
                        vidType: Int = DEFAULT_VIDTYPE,
                        quality: Int = DEFAULT_QUALITY): String {
        return bitmapToBase64(fromVideo(filePath, vidType), quality)
    }
}