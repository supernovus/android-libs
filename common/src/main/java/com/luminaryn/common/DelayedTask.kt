package com.luminaryn.common

import android.content.Context
import android.os.Handler

open class DelayedTask(context: Context,
                  private val runnable: Runnable,
                  private val runDirect: Boolean = false) {

    private val handler = Handler(context.mainLooper)

    fun run (delay: Long): Boolean {
        return if (delay <= 0L) {
            if (runDirect) {
                runnable.run()
                true
            } else {
                handler.post(runnable)
            }
        } else {
            handler.postDelayed(runnable, delay)
        }
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }
}
