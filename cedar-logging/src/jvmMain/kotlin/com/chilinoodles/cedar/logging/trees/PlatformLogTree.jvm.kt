package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree
import java.util.logging.Level
import java.util.logging.Logger

actual class PlatformLogTree actual constructor() : LogTree {
    private var logger: Logger = Logger.getLogger(PlatformLogTree::class.java.name)
    private var enableEmojis: Boolean = true

    actual fun configureForPlatform(config: PlatformLogConfig.() -> Unit): PlatformLogTree {
        val configuration = PlatformLogConfig().apply(config)
        
        configuration.jvmLoggerName?.let {
            logger = Logger.getLogger(it)
        }
        enableEmojis = configuration.enableEmojis
        
        return this
    }

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

        val header = "[$symbol $tag]"
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
