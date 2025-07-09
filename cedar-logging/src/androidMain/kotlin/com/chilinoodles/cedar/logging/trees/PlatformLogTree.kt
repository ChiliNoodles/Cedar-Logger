package com.chilinoodles.cedar.logging.trees

import android.util.Log
import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

actual class PlatformLogTree : LogTree {

    private val MAX_LOG_LENGTH = 4_000

    private fun String.logChunks(prio: Int, tag: String) =
        chunked(MAX_LOG_LENGTH).forEach { Log.println(prio, tag, it) }

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
        val safeTag = tag.take(23)

        val full = buildString {
            append(message)
            throwable?.let {
                appendLine()
                append(Log.getStackTraceString(it))
            }
        }

        full.logChunks(prio, safeTag)
    }
}
