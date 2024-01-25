package com.luminaryn.common

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import java.io.*
import java.lang.Exception
import java.util.*

object Images {
    @JvmOverloads
    @JvmStatic
    @Throws(IOException::class)
    fun saveImage(bitmap: Bitmap,
                  file: File,
                  format: CompressFormat = CompressFormat.JPEG,
                  quality: Int = 100) {
        saveImage(bitmap, FileOutputStream(file), format, quality)
    }

    @JvmOverloads
    @JvmStatic
    fun saveImage(bitmap: Bitmap,
                  output: OutputStream,
                  format: CompressFormat = CompressFormat.JPEG,
                  quality: Int = 100) {
        bitmap.compress(format, quality, output)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveImage(context: Context, bitmap: Bitmap,
                  format: CompressFormat, mimeType: String,
                  quality: Int, filename: String) {
        val fos: OutputStream?
        fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, filename)
            FileOutputStream(image)
        }
        bitmap.compress(format, quality, fos)
        Objects.requireNonNull(fos)?.close()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveImage(context: Context, bitmap: Bitmap, quality: Int, filename: String) {
        val format = CompressFormat.JPEG
        val mimeType = "image/jpeg"
        saveImage(context, bitmap, format, mimeType, quality, filename)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveImage(context: Context, bitmap: Bitmap, filename: String) {
        saveImage(context, bitmap, 100, filename)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun base64Encode(bitmap: Bitmap, format: CompressFormat, quality: Int): String {
        val bos = ByteArrayOutputStream()
        bitmap.compress(format, quality, bos)
        val base64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
        bos.close()
        return base64
    }

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class)
    fun base64Encode(bitmap: Bitmap, quality: Int = 100): String {
        return base64Encode(bitmap, CompressFormat.JPEG, quality)
    }

    @JvmStatic
    fun toJPEGStream(bitmap: Bitmap, quality: Int): ByteArrayOutputStream {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, quality, stream)
        return stream
    }

    @JvmStatic
    @JvmOverloads
    fun toJPEG(bitmap: Bitmap, quality: Int = 100): ByteArray {
        return toJPEGStream(bitmap, quality).toByteArray()
    }

    fun fromBase64(string: String): Bitmap? {
        val bytes = Base64.decode(string, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}