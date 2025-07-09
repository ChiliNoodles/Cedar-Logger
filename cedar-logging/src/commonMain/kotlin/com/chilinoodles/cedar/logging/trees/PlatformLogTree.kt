package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

/**
 * Debug tree that provides platform-specific optimized logging.
 * Each platform has a specific implementation that takes advantage of native capabilities.
 */
expect class PlatformLogTree() : LogTree {
    override fun isLoggable(tag: String?, priority: LogPriority): Boolean
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?)
}