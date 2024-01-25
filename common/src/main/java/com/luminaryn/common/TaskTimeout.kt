package com.luminaryn.common

import android.content.Context
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.time.Duration

open class TaskTimeout(
    private val context: Context,
    timeout: Long,
    unit: TimeUnit,
    private val runnable: Runnable) {

    constructor(context: Context, runnable: Runnable) : this(context, DT, DU, runnable)
    constructor(builder: Builder)
            : this(builder.context!!, builder.timeout, builder.unit, builder.runnable!!)

    var timeout: Long by Delegates.observable(timeout) { _, _, _ -> onChange?.invoke(this) }
    var unit: TimeUnit by Delegates.observable(unit) { _, _, _ -> onChange?.invoke(this) }

    private var task: DelayedTask? = null

    var onChange: ((TaskTimeout)->Unit)? = null

    fun onChange(handler: ((TaskTimeout)->Unit)?): TaskTimeout {
        onChange = handler
        return this
    }

    fun ping() {
        if (timeout > 0) {
            task?.stop()
            task = DelayedTask(context, runnable)
            task?.run(unit.toMillis(timeout))
        }
    }

    fun stop() {
        task?.stop()
        task = null
    }

    class Builder(var context: Context? = null, var runnable: Runnable? = null) {
        var timeout: Long = DT
        var unit: TimeUnit = DU

        var onChange: ((TaskTimeout)->Unit)? = null

        fun context(c: Context): Builder {
            context = c
            return this
        }

        fun timeout(t: Long): Builder {
            timeout = t
            return this
        }

        fun timeout(t: Int): Builder {
            return timeout(t.toLong())
        }

        fun timeout(d: Duration): Builder {
            return duration(d)
        }

        fun duration(d: Duration, u: TimeUnit = TimeUnit.MILLISECONDS): Builder {
            unit = u
            timeout = when(u) {
                TimeUnit.NANOSECONDS -> d.inWholeNanoseconds
                TimeUnit.MICROSECONDS -> d.inWholeMicroseconds
                TimeUnit.MILLISECONDS -> d.inWholeMilliseconds
                TimeUnit.SECONDS -> d.inWholeSeconds
                TimeUnit.MINUTES -> d.inWholeMinutes
                TimeUnit.HOURS -> d.inWholeHours
                TimeUnit.DAYS -> d.inWholeDays
            }
            return this
        }

        fun unit(u: TimeUnit): Builder {
            unit = u
            return this
        }

        fun unit(u: String): Builder {
            return unit(getTimeUnit(u))
        }

        fun unit(u: Char): Builder {
            return unit(getTimeUnit(u))
        }

        fun run(r: Runnable): Builder {
            runnable = r
            return this
        }
    }

    companion object {
        const val DT = 60L
        val DU = TimeUnit.SECONDS

        /**
         * Get a time unit via a single character code:
         *
         * @param symbol The code:
         *
         *  `D`  Days
         *  `M`  Minutes
         *  `m`  Milliseconds
         *  `μ`  Microseconds
         *  `n`  Nanoseconds
         *
         *  Anything else defaults to Seconds.
         */
        fun getTimeUnit(symbol: Char): TimeUnit {
            return when (symbol) {
                'D' -> TimeUnit.DAYS
                'M' -> TimeUnit.MINUTES
                'm' -> TimeUnit.MILLISECONDS
                'μ' -> TimeUnit.MICROSECONDS
                'n' -> TimeUnit.NANOSECONDS
                else -> DU
            }
        }

        /**
         * Try to parse a string and figure out the desired time unit.
         *
         * @param name  The name of the time unit.
         *
         * If the time unit cannot be determined by the name, Seconds will be assumed.
         */
        fun getTimeUnit(name: String): TimeUnit {
            // Can't work with an empty string.
            if (name.isEmpty()) return DU

            if (name.length == 1) {
                // Single character is case-sensitive.
                return getTimeUnit(name[0])
            }

            // Anything else is case-insensitive.
            return when (name.lowercase()) {
                "day", "days" -> TimeUnit.DAYS
                "hour", "hours", "hr", "hrs" -> TimeUnit.HOURS
                "minute", "minutes", "min", "mins" -> TimeUnit.MINUTES
                "milliseconds", "millisecond", "millis", "milli", "ms" -> TimeUnit.MILLISECONDS
                "microseconds", "microsecond", "micros", "micro", "μs" -> TimeUnit.MICROSECONDS
                "nanoseconds", "nanosecond", "nanos", "nano", "ns" -> TimeUnit.NANOSECONDS
                else -> DU
            }
        }

    }

}