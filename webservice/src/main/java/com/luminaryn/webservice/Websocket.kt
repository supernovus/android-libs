package com.luminaryn.webservice

import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

/**
 * A class providing a simple Websocket Client.
 */
class Websocket private constructor(builder: Builder) : HTTP(builder) {

    private val onOpen: ((WebSocket, Response) -> Unit)? = builder.onOpen
    private val onClosing: ((WebSocket, code: Int, reason: String) -> Unit)? = builder.onClosing
    private val onClosed: ((WebSocket, code: Int, reason: String) -> Unit)? = builder.onClosed
    private val onFailure: ((WebSocket, Throwable, Response?) -> Unit)? = builder.onFailure
    private val onMessageString: ((WebSocket, data: String) -> Unit)? = builder.onMessageString
    private val onMessageBytes: ((WebSocket, data: ByteString) -> Unit)? = builder.onMessageBytes

    private val useAbsoluteUris: Boolean = builder.useAbsoluteUris

    /**
     * Generate a listener for the Session instances using our events set by the builder.
     */
    private inner class Listener : WebSocketListener() {

        var state: SessionState = SessionState.Connecting

        override fun onOpen(webSocket: WebSocket, response: Response) {
            onOpen?.invoke(webSocket, response)
            state = SessionState.Open
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            onClosing?.invoke(webSocket, code, reason)
            state = SessionState.Closing
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            onClosed?.invoke(webSocket, code, reason)
            state = SessionState.Closed
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            onFailure?.invoke(webSocket, t, response)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            onMessageString?.invoke(webSocket, text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            onMessageBytes?.invoke(webSocket, bytes)
        }

    } // makeListener()

    @JvmOverloads
    fun open(url: String = "", absoluteUri: Boolean = useAbsoluteUris): Session {
        return Session(makeRequest(url, null, absoluteUri).build())
    }

    fun open(request: Request.Builder): Session {
        return Session(request.build())
    }

    fun open(request: Request): Session {
        return Session(request)
    }

    enum class SessionState {
        Connecting,
        Open,
        Closing,
        Closed,
        Canceled,
    }

    enum class StatusCode(val code: Int, val msg: String) {
        Finished(1000, "Finished"),
        GoingAway(1001, "Going away"),
        ProtocolError(1002, "Protocol error"),
        InvalidData(1003, "Invalid data type"),
        InconsistentData(1007, "Inconsistent data"),
        PolicyViolation(1008, "Policy violation"),
        OversizedMessage(1009, "Message size is too large"),
        MissingExtension(1010, "Server did not negotiate required extension"),
        UnexpectedCondition(1011, "Unexpected condition encountered"),
    }

    inner class Session (val request: Request) {
        private val listener = Listener()
        val webSocket = client.newWebSocket(request, listener)

        val state: SessionState
            get() = listener.state

        val queueSize: Long
            get() = webSocket.queueSize()

        fun send(json: JSONObject): Boolean {
            return send(json.toString())
        }

        fun send(text: String): Boolean {
            return webSocket.send(text)
        }

        fun send(bytes: ByteString): Boolean {
            return webSocket.send(bytes)
        }

        fun close(code: Int, reason: String): Boolean {
            return webSocket.close(code, reason)
        }

        @JvmOverloads
        fun close(status: StatusCode = StatusCode.Finished, reason: String? = null): Boolean {
            val msg = reason ?: status.msg
            return close(status.code, msg)
        }

        fun missingExtension(extension: String): Boolean {
            val msg = "${StatusCode.MissingExtension}: $extension"
            return close(StatusCode.MissingExtension, msg)
        }

        fun cancel() {
            webSocket.cancel()
            listener.state = SessionState.Canceled
        }

    } // inner class Session

    /**
     * The builder for Websocket instances.
     *
     * Unlike other `HTTP` sub-classes, the builder for this is the only way to create
     * `Websocket` instances. Use it to assign the necessary callback methods.
     */
    class Builder : HTTP.Builder<Builder>() {
        override val `this`: Builder
            get() = this

        var onOpen: ((WebSocket, Response) -> Unit)? = null
        var onClosing: ((WebSocket, code: Int, reason: String) -> Unit)? = null
        var onClosed: ((WebSocket, code: Int, reason: String) -> Unit)? = null
        var onFailure: ((WebSocket, Throwable, Response?) -> Unit)? = null
        var onMessageString: ((WebSocket, data: String) -> Unit)? = null
        var onMessageBytes: ((WebSocket, data: ByteString) -> Unit)? = null

        var useAbsoluteUris: Boolean = false

        fun setAbsoluteUris(value: Boolean): Builder {
            useAbsoluteUris = value
            return this
        }

        fun onOpen(handler: (WebSocket, Response) -> Unit): Builder {
            onOpen = handler
            return this
        }

        fun onClosing(handler: (WebSocket, code: Int, reason: String) -> Unit): Builder {
            onClosing = handler
            return this
        }

        fun onClosed(handler: (WebSocket, code: Int, reason: String) -> Unit): Builder {
            onClosed = handler
            return this
        }

        fun onFailure(handler: (WebSocket, Throwable, Response?) -> Unit): Builder {
            onFailure = handler
            return this
        }

        fun onMessageString(handler: (WebSocket, data: String) -> Unit): Builder {
            onMessageString = handler
            return this
        }

        fun onMessageBytes(handler: (WebSocket, data: ByteString) -> Unit): Builder {
            onMessageBytes = handler
            return this
        }

        fun build(): Websocket {
            return Websocket(this)
        }

    } // class Builder

} // class Websocket