package com.luminaryn.scripting

import java.io.Reader
import javax.script.Bindings

class Script(val runner: Runner = Runner()) {
    var stringSource: String? = null
    var readerSource: Reader? = null
    var scope: Bindings? = null
    var result: Any? = null

    fun useLocalScope(answer: Boolean = true) {
        if (answer) {
            scope = runner.makeScope()
        } else {
            scope = null
        }
    }

    fun run(): Any? {
        if (stringSource !== null) {
            if (scope != null) {
                runner.run(stringSource!!, scope!!)
            } else {
                runner.run(stringSource!!)
            }
        } else if (readerSource != null) {
            if (scope != null) {
                runner.run(readerSource!!, scope!!)
            } else {
                runner.run(readerSource!!)
            }
        }
        return null
    }

}