package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

/** External JS console object for Kotlin/Wasm interop */
@JsModule("console")
external object Console {
    fun debug(vararg args: String)
    fun log(vararg args: String)
    fun info(vararg args: String)
    fun warn(vararg args: String)
    fun error(vararg args: String)
}

/** Wasm-JS implementation of the logging tree */
actual class PlatformLogTree actual constructor() : LogTree {
    actual override fun isLoggable(tag: String?, priority: LogPriority) = true

    actual override fun log(
        priority: LogPriority,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val symbol = when (priority) {
            LogPriority.VERBOSE -> "🔍"
            LogPriority.DEBUG -> "🐞"
            LogPriority.INFO -> "ℹ️"
            LogPriority.WARNING -> "⚠️"
            LogPriority.ERROR -> "❌"
        }

        val header = "[$symbol $tag]"
        val errorDump = throwable?.stackTraceToString()
        val fullMessage = buildList {
            add(header)
            add(message)
            errorDump?.let { add(it) }
        }.toTypedArray()

        when (priority) {
            LogPriority.VERBOSE,
            LogPriority.DEBUG -> Console.debug(*fullMessage)
            LogPriority.INFO -> Console.info(*fullMessage)
            LogPriority.WARNING -> Console.warn(*fullMessage)
            LogPriority.ERROR -> Console.error(*fullMessage)
        }
    }
}
