package com.luminaryn.scripting

import java.io.Reader
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine

open class Runner(val env: Environment = Environment()) {

    protected open val languageExtension: String? = Environment.DEFAULT_EXT
    protected open val languageName: String? = null
    protected open val languageMimeType: String? = null

    val engine: ScriptEngine by lazy {
        env.makeEngine(languageExtension, languageName, languageMimeType)
    }

    var context: ScriptContext
        get() = engine.context
        set(value) { engine.context = value }

    val globalScope: Bindings
        get() = env.scope

    var scope: Bindings
        get() = engine.getBindings(ScriptContext.ENGINE_SCOPE)!!
        set(value) { engine.setBindings(value, ScriptContext.ENGINE_SCOPE) }

    fun makeScope(): Bindings = engine.createBindings()

    fun makeScript(): Script = Script(this)
    fun makeScript(src: String): Script = makeScript().apply { stringSource = src }
    fun makeScript(src: Reader): Script = makeScript().apply { readerSource = src }

    fun run(script: String): Any {
        return engine.eval(script)
    }

    fun run(script: String, scope: Bindings): Any {
        return engine.eval(script, scope)
    }

    fun run(script: String, context: ScriptContext): Any {
        return engine.eval(script, context)
    }

    fun run(reader: Reader): Any {
        return engine.eval(reader)
    }

    fun run(reader: Reader, scope: Bindings): Any {
        return engine.eval(reader, scope)
    }

    fun run(reader: Reader, context: ScriptContext): Any {
        return engine.eval(reader, context)
    }

    // The following APIs taken from and/or inspired by github.com/s1monw1/KtsRunner

    inline fun <reified T> Any?.castOrError(): T = takeIf { it is T }?.let { it as T }
        ?: throw IllegalArgumentException("Cannot cast $this to expected type ${T::class}")

    inline fun <reified T> eval(script: String): T =
        kotlin.runCatching { engine.eval(script) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> eval(script: String, scope: Bindings): T =
        kotlin.runCatching { engine.eval(script, scope) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> eval(script: String, context: ScriptContext): T =
        kotlin.runCatching { engine.eval(script, context) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> eval(reader: Reader): T =
        kotlin.runCatching { engine.eval(reader) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> eval(reader: Reader, scope: Bindings): T =
        kotlin.runCatching { engine.eval(reader, scope) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

    inline fun <reified T> eval(reader: Reader, context: ScriptContext): T =
        kotlin.runCatching { engine.eval(reader, context) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()

}