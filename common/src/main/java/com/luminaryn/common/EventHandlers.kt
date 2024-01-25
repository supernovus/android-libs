package com.luminaryn.common

import org.json.JSONObject
import kotlin.math.sin

typealias EventCallbackOpts = Map<String,Any>?
typealias EventCallback = (EventCallbackOpts) -> Unit

/**
 * A generic class for registering "events" which can occur which other
 * classes can register event callbacks (simple closures) on, and all registered
 * closures will be called when the event is triggered.
 *
 * It's probably better to build model-specific event handlers rather than using a generic
 * class like this, but there may be times when something like this is useful, so here it is.
 *
 * @constructor
 * @param implicitRegistration If true, calling 'on' or 'once' with an event not already registered will register one.
 * @param implicitSingleTrigger When registering an implicit handler, the singleTrigger property will be set to this.
 */
class EventHandlers(private val implicitRegistration: Boolean,
                    private val implicitSingleTrigger: Boolean = false) {

    private val eventHandlers = HashMap<String, EventHandler>()

    /**
     * Register an event handler.
     *
     * If the event handler is already registered, that instance will be returned.
     *
     * @param name The name of the event this handler is for.
     * @param singleTrigger See EventHandler for details on how this works.
     */
    fun register(event: String, singleTrigger: Boolean): EventHandler {
        if (!eventHandlers.containsKey(event))
            eventHandlers[event] = EventHandler(event, singleTrigger)
        return eventHandlers[event]!!
    }

    /**
     * Get the named event handler.
     *
     * This will auto-register a new event handler if {implicitRegistration} is true.
     * Otherwise it will return null if there is no matching handler for the event.
     */
    fun get(event: String): EventHandler? {
        return if (implicitRegistration) register(event, implicitSingleTrigger) else eventHandlers[event]
    }

    /**
     * Add an event callback.
     *
     * @param event The event name you want to add a callback for.
     * @param callback The callback closure to call when this event is triggered.
     */
    fun on(event: String, callback: EventCallback) {
        get(event)?.on(callback)
    }

    /**
     * Add an event callback to run only once then remove itself.
     *
     * @param event The event name you want to add a callback for.
     * @param callback The callback closure to call when this event is triggered.
     */
    fun once(event: String, callback: EventCallback) {
        get(event)?.once(callback)
    }

    /**
     * Remove a registered event callback.
     *
     * @param event The event name to remove the callback from.
     * @param callback The callback to remove.
     */
    fun remove(event: String, callback: EventCallback) {
        eventHandlers[event]?.remove(callback)
    }

    /**
     * Remove an event handler entirely.
     *
     * @param event The name of the event handler to remove.
     */
    fun unregister(event: String) {
        eventHandlers.remove(event)
    }

    /**
     * Remove an event handler entirely.
     */
    fun unregister(handler: EventHandler) {
        eventHandlers.remove(handler.name)
    }

    @JvmOverloads
    fun trigger(event: String, options: EventCallbackOpts = null) {
        eventHandlers[event]?.trigger(options)
    }

    fun trigger(event: String, options: JSONObject) {
        eventHandlers[event]?.trigger(options)
    }

    /**
     * A class representing an event handler.
     *
     * You can use this directly, or implicitly via the parent EventHandlers class.
     *
     * @property name The name of the event.
     * @property singleTrigger If true, this will only be triggered once and callbacks added after will be invoked immediately.
     * @property triggered The number of times this handler has been triggered.
     *
     * @constructor
     * @param name The name of the event.
     * @param singleTrigger If true, this will only be triggered once and callbacks added after will be invoked immediately.
     */
    class EventHandler(val name: String, val singleTrigger: Boolean = false) {

        private val callbacks = HashSet<EventCallback>()
        var triggered = 0
            private set
        private var singleTriggerOpts: EventCallbackOpts = null

        @JvmOverloads
        fun on(callback: EventCallback, addEvenOnSingle: Boolean = true) {

            val add: Boolean // Should we add the callback?

            if (singleTrigger && triggered > 0) {
                callback.invoke(singleTriggerOpts)
                add = addEvenOnSingle
            } else add = true

            if (add)
                callbacks.add(callback)
        }

        fun once(callback: EventCallback) {
            lateinit var wrapperCallback: EventCallback // MUST be specified separately.
            wrapperCallback = { opts: EventCallbackOpts ->
                callback.invoke(opts)
                this.remove(wrapperCallback)
            }
            on(wrapperCallback, false)
        }

        fun remove(callback: EventCallback) = callbacks.remove(callback)

        @JvmOverloads
        fun trigger(options: EventCallbackOpts = null) {
            if (singleTrigger && triggered > 0) return // Already triggered

            for (callback in callbacks) {
                callback.invoke(options)
            }

            triggered++
            if (singleTrigger) singleTriggerOpts = options
        }

        fun trigger(options: JSONObject) {
            trigger(Json.toHashMap(options, true))
        }
    }
}