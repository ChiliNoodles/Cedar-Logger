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

/**
 * iOS-specific debug tree implementation.
 * Uses Apple's NSLog for better integration with Xcode console.
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformLogTree : LogTree {

    actual override fun isLoggable(tag: String?, priority: LogPriority) = true

    @OptIn(BetaInteropApi::class)
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
        val body = message
        val errorDump = throwable?.stackTraceToString()
        val allText = buildList {
            add(header)
            add(body)
            if (errorDump != null) add(errorDump)
        }.joinToString(" ")

        autoreleasepool {
            allText.chunked(1000).forEach { chunk ->
                _os_log_internal(
                    __dso_handle.ptr,
                    OS_LOG_DEFAULT,
                    mapToOsLogType(priority),
                    "%s",
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