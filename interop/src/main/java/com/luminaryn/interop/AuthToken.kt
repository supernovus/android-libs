package com.luminaryn.interop

import com.luminaryn.common.Encoder
import com.luminaryn.webservice.HTTP
import okhttp3.Headers

/**
 * A class for using the lum-framework AuthToken authentication in your apps.
 * Uses the "common.Encoder" and "webservice.HTTP" classes from our sibling modules.
 */
object AuthToken {
    /**
     * The default authentication header.
     */
    const val DEFAULT_AUTH_HEADER = "X-Lum-Auth-Token"

    /**
     * The version string for format 1 (basic tokens)
     */
    const val FORMAT_1 = "01"

    /**
     * The algorithm used by all current formats.
     */
    const val ALGO_1 = "SHA-256"

    /**
     * Given an App Token string, parse it, and return an Auth Token string.
     *
     * @param appToken The App Token string
     * @param tokenOpts Named options that might be required by certain formats.
     * @param tokenVars Positional options that might be required by certain formats.
     * @return The Auth Token string if valid, "" if App Token was empty, null if format was invalid.
     */
    @JvmOverloads
    fun getAuthToken (appToken: String?, tokenOpts: Map<String, Any>? = null, tokenVars: List<Any>? = null) :String? {
        if (appToken.isNullOrEmpty()) return "" // Empty begets empty.
        val format = appToken.substring(0,2)
        var authToken: String? = null
        when (format) {
            FORMAT_1 -> {
                val encoder = Encoder(ALGO_1)
                val lenStr = appToken.substring(2,4)
                val length = lenStr.toInt()
                val offset = 4 + length
                val tid = appToken.substring(4, offset)
                val appHash = appToken.substring(offset)
                encoder.add(tid)
                encoder.add(appHash)
                val authHash = encoder.encode(Encoder.HEX).toLowerCase()
                authToken = format + lenStr + tid + authHash
            }
        }

        return authToken
    }

    /**
     * Set or unset an authToken header.
     */
    @JvmOverloads
    fun setAuthToken (authToken: String?, headers: Headers.Builder, header: String = DEFAULT_AUTH_HEADER) {
        if (authToken.isNullOrEmpty()) {
            headers.removeAll(header) // Not authorized, remove the header.
        } else {
            headers.set(header, authToken)
        }
    }

    /**
     * A convenience wrapper around setAuthToken and getAuthToken.
     */
    @JvmOverloads
    fun setAppToken (appToken: String?, headers: Headers.Builder, header: String = DEFAULT_AUTH_HEADER) {
        setAuthToken(getAuthToken(appToken), headers, header)
    }

    /**
     * If the only custom header you need is the auth token, this is a super simple convenience method.
     */
    @JvmOverloads
    fun setAppToken (appToken: String?, ws: HTTP, header: String = DEFAULT_AUTH_HEADER) {
        val authToken = getAuthToken(appToken)
        if (authToken.isNullOrEmpty()) { // No headers.
            ws.headers = null
        } else { // Set the header.
            val headers = ws.makeHeaders()
            setAuthToken(authToken, headers, header)
            ws.setHeaders(headers)
        }
    }

    /**
     * Finally, the ultimate wrapper taking appToken, webservice, and headers.
     */
    @JvmOverloads
    fun setAppToken (appToken: String?, ws: HTTP, headers: Headers.Builder, header: String = DEFAULT_AUTH_HEADER) {
        setAppToken(appToken, headers, header) // Add or remove the authToken header.
        ws.setHeaders(headers) // Set the headers.
    }

}