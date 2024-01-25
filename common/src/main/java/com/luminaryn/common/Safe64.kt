package com.luminaryn.common

import android.util.Base64

/**
 * TODO: support V3 Data extension with at least JSON and possibly UBJSON serialization.
 */
object Safe64 {
    /**
     * Convert a Base64 string into a Safe64 string.
     * Safe64 is a version of Base64 with all URL-safe characters.
     *
     * As per my other similar libraries in PHP and Javascript:
     *
     * '+' becomes '-'
     * '/' becomes '_'
     *
     * If useTildes is true, then:
     *
     * '=' becomes '~'
     *
     * If useTildes is false then '=' characters will simply be stripped.
     *
     * @param text
     * @param useTildes
     * @return
     */
    fun urlize(text: String, useTildes: Boolean = false): String {
        var text = text
        text = text
                .replace('+', '-')
                .replace('/', '_')
        text = if (useTildes) text.replace('=', '~') else text.replace("=", "")
        return text
    }

    fun deurlize(text: String): String {
        var text = text
        text = text
                .replace('-', '+')
                .replace('_', '/')
                .replace('~', '=')
        val append = "===".substring((text.length + 3) % 4)
        if (!append.isEmpty()) {
            text = text + append
        }
        return text
    }

    @JvmOverloads
    fun encode(data: ByteArray?, useTildes: Boolean, b64flags: Int = Base64.NO_WRAP): String {
        return urlize(Base64.encodeToString(data, b64flags), useTildes)
    }

    fun decode(data: String?, b64flags: Int): ByteArray {
        return Base64.decode(data, b64flags)
    }
}