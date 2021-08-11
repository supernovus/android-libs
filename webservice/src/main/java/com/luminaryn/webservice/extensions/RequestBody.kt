package com.luminaryn.webservice.extensions

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.InputStream

@JvmOverloads
fun InputStream.asRequestBody(contentType: MediaType? = null): RequestBody {
    val self = this
    return object : RequestBody() {
        override fun contentType() = contentType

        override fun writeTo(sink: BufferedSink) {
            source().use { sink.writeAll(it) }
        }

    }
}
