package com.chilinoodles.cedar.logging.trees

import android.util.Log
import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

actual class PlatformLogTree : LogTree {

    private var maxLogLength = 4_000
    private var enableEmojis: Boolean = true

    actual fun configureForPlatform(config: PlatformLogConfig.() -> Unit): PlatformLogTree {
        val configuration = PlatformLogConfig().apply(config)
        
        configuration.androidMaxLogLength?.let { maxLogLength = it }
        enableEmojis = configuration.enableEmojis
        
        return this
    }

    private fun String.logChunks(prio: Int, tag: String) =
        chunked(maxLogLength).forEach { Log.println(prio, tag, it) }

    private fun LogPriority.toAndroid(): Int = when (this) {
        LogPriority.VERBOSE -> Log.VERBOSE
        LogPriority.DEBUG -> Log.DEBUG
        LogPriority.INFO -> Log.INFO
        LogPriority.WARNING -> Log.WARN
        LogPriority.ERROR -> Log.ERROR
    }

    actual override fun isLoggable(tag: String?, priority: LogPriority) = true

    actual override fun log(
        priority: LogPriority,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val prio = priority.toAndroid()
        val actualTag = tag
        val safeTag = actualTag.take(23)

        val symbol = if (enableEmojis) {
            when (priority) {
                LogPriority.VERBOSE -> "🔍"
                LogPriority.DEBUG -> "🐞"
                LogPriority.INFO -> "ℹ️"
                LogPriority.WARNING -> "⚠️"
                LogPriority.ERROR -> "❌"
            }
        } else {
            when (priority) {
                LogPriority.VERBOSE -> "V"
                LogPriority.DEBUG -> "D"
                LogPriority.INFO -> "I"
                LogPriority.WARNING -> "W"
                LogPriority.ERROR -> "E"
            }
        }

        val full = buildString {
            append("$symbol $message")
            throwable?.let {
                appendLine()
                append(Log.getStackTraceString(it))
            }
        }

        full.logChunks(prio, safeTag)
    }
}
