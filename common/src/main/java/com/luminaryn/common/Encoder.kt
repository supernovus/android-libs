package com.luminaryn.common

import android.util.Base64
import android.widget.EditText
import java.lang.Error
import java.math.BigInteger
import java.security.MessageDigest

/**
 * An encoder class, most of which was originally from my Passy app, the generic parts
 * have now been split into this library for ease of use elsewhere.
 *
 * It's a wrapper around the MessageDigest class, with Base64, Base91, Safe64, and Hex encoding.
 */
open class Encoder(algorithm: String) {
    protected var digest: MessageDigest = MessageDigest.getInstance(algorithm)

    /**
     * Calculate the digest bytes (this resets the digest state at the same time.)
     *
     * @return The digest bytes
     */
    fun digest(): ByteArray {
        return digest.digest()
    }

    /**
     * Calculate the digest and encode it to a string in a particular format.
     * See encode() for further details.
     */
    @Deprecated("Renamed to encode", ReplaceWith("encode(format)"))
    fun encodeDigest(format: Int): String {
        return encode(format)
    }

    /**
     * Calculate the digest and encode it to a string in a particular format.
     * TODO: Document the formats.
     */
    open fun encode(format: Int): String {
        val digestBytes = digest()
        return when (format) {
            HEX -> toHex(digestBytes)
            BASE91 -> toBase91(digestBytes)
            BASE64 -> toBase64(digestBytes)
            SAFE64 -> toSafe64(digestBytes, false)
            SAFE64_TILDES -> toSafe64(digestBytes, true)
            else -> throw Error("Unrecognized digest format")
        }
    }

    fun add(bytes: ByteArray) {
        digest.update(bytes)
    }

    fun add(string: String) {
        add(string.toByteArray())
    }

    fun add(text: EditText) {
        add(getTextBytes(text))
    }

    /**
     * A version of encodeDigest() that uses the Safe64 encoding algorithm.
     *
     * @param useTildes Replace '=' with '~'
     * @return The URL safe encoded string.
     */
    @JvmOverloads
    @Deprecated("Replaced by encode", ReplaceWith("encode(Encoder.SAFE64 || Encoder.SAFE64_TILDES)"))
    fun encodeForUrl(useTildes: Boolean = false): String {
        return toSafe64(digest(), useTildes)
    }

    companion object {
        const val HEX = 0 // Hex is pretty common, but quite long.
        const val BASE64 = 1 // Base64 is probably the most common.
        const val BASE91 = 2 // Base91 is more compact, but less HTTP friendly.
        const val SAFE64 = 3 // Safe64 without tildes.
        const val SAFE64_TILDES = 4 // Safe64 with tildes.

        fun toHex(bytes: ByteArray): String {
            val bi = BigInteger(1, bytes)
            return String.format("%0" + (bytes.size shl 1) + "X", bi)
        }

        fun toBase91 (bytes: ByteArray): String {
            return Base91.encodeToString(bytes)
        }

        fun toBase64 (bytes: ByteArray): String {
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        fun toSafe64 (bytes: ByteArray, useTildes: Boolean): String {
            return Safe64.urlize(toBase64(bytes), useTildes)
        }

        fun getTextBytes(text: EditText): ByteArray {
            return text.text.toString().toByteArray()
        }
    }

}