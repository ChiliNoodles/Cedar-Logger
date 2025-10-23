package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.ptr
import platform.darwin.OS_LOG_DEFAULT
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_FAULT
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_t



/**
 * iOS-specific debug tree implementation.
 * Uses Apple's os_log for better integration with Xcode console and Console.app.
 * Supports custom subsystems and categories for organized logging.
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformLogTree : LogTree {
    
    private var customLogObject: os_log_t? = null
    private var config: PlatformLogConfig? = null

    actual override fun isLoggable(tag: String?, priority: LogPriority) = true

    actual fun configureForPlatform(config: PlatformLogConfig.() -> Unit): PlatformLogTree {
        val configuration = PlatformLogConfig().apply(config)
        this.config = configuration
        
        if (configuration.iosSubsystem != null) {
            customLogObject = os_log_create(
                configuration.iosSubsystem,
                configuration.iosCategory ?: "General"
            )
        }
        
        return this
    }

    @OptIn(BetaInteropApi::class)
    actual override fun log(
        priority: LogPriority,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val useEmojis = config?.enableEmojis ?: true
        val symbol = if (useEmojis) {
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

        val header = "$symbol [$tag]"
        val body = message
        val errorDump = throwable?.stackTraceToString()
        val allText = buildList {
            add(header)
            add(body)
            if (errorDump != null) add(errorDump)
        }.joinToString(" ")

        val logObject = customLogObject ?: OS_LOG_DEFAULT

        autoreleasepool {
            allText.chunked(1000).forEach { chunk ->
                _os_log_internal(
                    __dso_handle.ptr,
                    logObject,
                    mapToOsLogType(priority),
                    "%{public}s",
                    chunk
                )
            }
        }
    }

    private fun mapToOsLogType(priority: LogPriority): UByte = when (priority) {
        LogPriority.VERBOSE -> OS_LOG_TYPE_DEFAULT
        LogPriority.DEBUG -> OS_LOG_TYPE_DEBUG
        LogPriority.INFO -> OS_LOG_TYPE_INFO
        LogPriority.WARNING -> OS_LOG_TYPE_ERROR
        LogPriority.ERROR -> OS_LOG_TYPE_FAULT
    }
}