package com.luminaryn.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import java.io.IOException
import java.lang.Exception

/**
 * Helper class for making Base64 encoded thumbnails.
 *
 * This was a part of one of my private apps since 2020, but I'm in the process
 * of migrating any useful stuff from that outdated app into this library set.
 *
 * @TODO: Add support for modern APIs.
 *        A lot of this is using older (some deprecated) APIs.
 *        While some of them may have to remain if the feature they offer
 *        isn't supported by the newer APIs, offering alternative methods
 *        using the newer APIs would be a great addition to this library.
 *        I may end up changing this from a static object into a class,
 *        or making a separate class inside this. Whatever works best to
 *        modernize the codebase while keeping some form of compatibility.
 *
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
        return try {
            if (Files.isImage(filepath)) {
                fromImage(filepath, width, height)
            } else if (Files.isVideo(filepath)) {
                fromVideo(filepath, vidType)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @JvmStatic
    @JvmOverloads
    fun base64Thumbnail(filepath: String,
                        width: Int = DEFAULT_WIDTH,
                        height: Int = DEFAULT_HEIGHT,
                        vidType: Int = DEFAULT_VIDTYPE,
                        quality: Int = DEFAULT_QUALITY): String {
        return bitmapToBase64(thumbnail(filepath, width, height, vidType),quality)
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
                  height: Int = DEFAULT_HEIGHT): Bitmap? {
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
                        vidType: Int = DEFAULT_VIDTYPE): Bitmap? {
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
