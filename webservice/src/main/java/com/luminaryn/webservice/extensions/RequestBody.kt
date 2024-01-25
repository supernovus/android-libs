package com.luminaryn.webservice.extensions

import android.util.Log
import com.luminaryn.common.Files
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer
import okio.source
import java.io.File
import java.io.InputStream

@JvmOverloads
fun InputStream.asRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
        override fun contentType() = contentType

        override fun writeTo(sink: BufferedSink) {
            source().use { sink.writeAll(it) }
        }

    }
}

@JvmOverloads
fun InputStream.getRequestBodyRange(offset: Long, length: Long, contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
        override fun contentType() = contentType
        override fun writeTo(sink: BufferedSink) {
            val src = source().buffer()
            if (offset > 0)
                src.skip(offset)
            sink.write(src, length)
        }
    }
}

@JvmOverloads
fun File.asRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
        override fun contentType() = contentType
        override fun writeTo(sink: BufferedSink) {
            source().use { sink.writeAll(it) }
        }
    }
}

@JvmOverloads
fun File.getRequestBodyRange(offset: Long, length: Long, contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {
        override fun contentType() = contentType
        override fun writeTo(sink: BufferedSink) {
            val src = source().buffer()
            if (offset > 0)
                src.skip(offset)
            sink.write(src, length)
        }
    }
}

@JvmOverloads
fun File.getRequestBodyChunk(chunkSize: Long, wantPart: Long, contentType: MediaType? = null): RequestBody {
    val file = this
    return object : RequestBody() {
        override fun contentType() = contentType
        override fun writeTo(sink: BufferedSink) {
            val chunks = Files.getChunks(file, chunkSize)
            val partIndex = if (wantPart < 0) 0L else if (wantPart >= chunks) chunks-1 else wantPart
            val offset = partIndex * chunkSize
            val src = source().buffer()
            val remaining = file.length() - offset
            val length = if (chunkSize > remaining) remaining else chunkSize
            Log.v("Lum", "getRequestBodyChunk($chunkSize, $wantPart) chunks = $chunks, index = $partIndex, offset = $offset, length = $length, remaining = $remaining")
            if (offset > 0)
                src.skip(offset)
            sink.write(src, length)
        }
    }
}
