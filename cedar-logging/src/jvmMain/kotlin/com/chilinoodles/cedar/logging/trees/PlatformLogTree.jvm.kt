package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree
import java.util.logging.Level
import java.util.logging.Logger

actual class PlatformLogTree actual constructor() : LogTree {
    private val logger: Logger = Logger.getLogger(PlatformLogTree::class.java.name)

    actual override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
        val level = when (priority) {
            LogPriority.VERBOSE, LogPriority.DEBUG -> Level.FINEST
            LogPriority.INFO -> Level.INFO
            LogPriority.WARNING -> Level.WARNING
            LogPriority.ERROR -> Level.SEVERE
        }
        return logger.isLoggable(level)
    }

    actual override fun log(
        priority: LogPriority,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val header = "[$tag]"
        val fullMessage = buildString {
            append(header).append(" ").append(message)
            throwable?.let {
                append("\n").append(it.stackTraceToString())
            }
        }

        val level = when (priority) {
            LogPriority.VERBOSE, LogPriority.DEBUG -> Level.FINEST
            LogPriority.INFO -> Level.INFO
            LogPriority.WARNING -> Level.WARNING
            LogPriority.ERROR -> Level.SEVERE
        }

        if (throwable != null) {
            logger.log(level, fullMessage, throwable)
        } else {
            logger.log(level, fullMessage)
        }
    }
}
