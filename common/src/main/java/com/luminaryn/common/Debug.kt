package com.luminaryn.common

/**
 * A helper class  that lets you send different messages depending on a level.
 *
 * @param tag  The tag to use in log entries.
 * @param initialLevel The initial debugging level. If not specified, we use 0.
 *
 * @property tag The tag to use in log entries, set via the constructor param of the same name.
 * @property level The current debugging level.
 */
class Debug (val tag: String, initialLevel: Int = 0) {

    var level = initialLevel

    /**
     * Low-level wrapper around LongLog.log() which only sends the log when the
     * current debugging level is equal to or greater than the specified level.
     *
     * For non-String objects, it will stringify the object before sending it to the
     * log function. It has special handling for Throwable objects only if the verbosity
     * is VERBOSE or DEBUG levels, in those cases it not only stringifies the  object,
     * but it stringifies the stack trace of the throwable as well.
     *
     * Use v(), d(), i(), w(), or e() as front-ends to this method.
     */
    fun msg (ver: Int, lvl: Int, payload: Any) {
        if (level >= lvl) {
            val msg: String

            if (payload is Throwable && (ver == V || ver == D)) {
                msg = "$payload\n" + payload.stackTrace.joinToString("\n")
            } else {
                msg = payload.toString()
            }

            LongLog.log(ver, tag, msg)
        }
    }

    fun v (lvl: Int, payload: Any) {
        msg(V, lvl, payload)
    }

    fun d (lvl: Int, payload: Any) {
        msg(D, lvl, payload)
    }

    fun i (lvl: Int, payload: Any) {
        msg(I, lvl, payload)
    }

    fun w (lvl: Int, payload: Any) {
        msg(W, lvl, payload)
    }

    fun e (lvl: Int, payload: Any) {
        msg(E, lvl, payload)
    }

    fun run (lvl: Int, func: (Debug) -> Unit) {
        if (level >= lvl) {
            func(this)
        }
    }

    companion object {
        const val V = LongLog.VERBOSE
        const val D = LongLog.DEBUG
        const val I = LongLog.INFO
        const val W = LongLog.WARN
        const val E = LongLog.ERROR
    }

}