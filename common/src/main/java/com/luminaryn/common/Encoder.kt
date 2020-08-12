package com.luminaryn.common

import android.util.Base64
import android.widget.EditText
import java.math.BigInteger
import java.security.MessageDigest

/**
 * An encoder class, most of which was originally from my Passy app, the generic parts
 * have now been split into this library for ease of use elsewhere.
 */
open class Encoder(algorithm: String?) {
    protected var digest: MessageDigest
    fun encodeDigest(format: Int): String {
        val digestBytes = digest.digest()
        return if (format == BASE91) Base91.encodeToString(digestBytes) else if (format == BASE64) Base64.encodeToString(digestBytes, Base64.NO_WRAP) else toHex(digestBytes)
    }

    fun add(bytes: ByteArray?) {
        digest.update(bytes)
    }

    fun add(string: String) {
        add(string.toByteArray())
    }

    fun add(text: EditText) {
        add(getTextBytes(text))
    }

    /**
     * A version of encode() that uses the Safe64 encoding algorithm.
     *
     * @param useTildes Replace '=' with '~'
     * @return The URL safe encoded string.
     */
    @JvmOverloads
    fun encodeForUrl(useTildes: Boolean = false): String {
        return Safe64.urlize(encodeDigest(BASE64), useTildes)
    }

    companion object {
        const val HEX = 0 // Not actually used, but here for completion.
        const val BASE64 = 1 // Base64 is probably the most common.
        const val BASE91 = 2 // Base91 is more compact, but less HTTP friendly.
        fun toHex(bytes: ByteArray): String {
            val bi = BigInteger(1, bytes)
            return String.format("%0" + (bytes.size shl 1) + "X", bi)
        }

        protected fun getTextBytes(text: EditText): ByteArray {
            return text.text.toString().toByteArray()
        }
    }

    init {
        digest = MessageDigest.getInstance(algorithm)
    }
}