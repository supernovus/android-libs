package com.luminaryn.scripting

import java.io.Reader
import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * A thin wrapper around the ScriptEngineManager class.
 */
class Environment(classLoader: ClassLoader? = Thread.currentThread().contextClassLoader) {
    val manager = ScriptEngineManager(classLoader)
    var scope: Bindings
        get() = manager.bindings
        set(value) { manager.bindings = value }

    fun get(key: String): Any = manager.get(key)
    fun put(key: String, value: Any) = manager.put(key, value)

    fun makeEngine(ext: String? = DEFAULT_EXT, name: String? = null, mimeType: String? = null): ScriptEngine {
        if (name != null) return manager.getEngineByName(name)
        if (mimeType != null) return manager.getEngineByMimeType(mimeType)
        if (ext != null) return manager.getEngineByExtension(ext)
        // If we reached here, nothing was specified, that's not valid...
        throw InvalidParameters("All parameters were null")
    }

    fun makeRunner(): Runner = Runner(this)
    fun makeScript(): Script = makeRunner().makeScript()
    fun makeScript(src: String): Script = makeRunner().makeScript(src)
    fun makeScript(src: Reader): Script = makeRunner().makeScript(src)

    class InvalidParameters(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

    companion object {
        const val DEFAULT_EXT = "kts"
    }
}