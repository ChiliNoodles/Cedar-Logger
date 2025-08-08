package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

/**
 * Platform-specific configuration for logging behavior.
 * Contains configuration options for all platforms, but each platform will only apply relevant settings.
 */
class PlatformLogConfig {
    var iosSubsystem: String? = null
    var iosCategory: String? = null
    
    var androidMaxLogLength: Int? = null
    
    var jvmLoggerName: String? = null

    var enableEmojis: Boolean = true
}

/**
 * Debug tree that provides platform-specific optimized logging.
 * Each platform has a specific implementation that takes advantage of native capabilities.
 * 
 * Usage:
 * ```
 * // Basic usage (works on all platforms)
 * Cedar.plant(PlatformLogTree())
 * 
 * // Platform-specific configuration
 * Cedar.plant(PlatformLogTree().configureForPlatform {
 *     // Platform-specific options will be available here
 * })
 * ```
 */
expect class PlatformLogTree() : LogTree {
    override fun isLoggable(tag: String?, priority: LogPriority): Boolean
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?)
    
    /**
     * Configure platform-specific logging options.
     * This method allows platform-specific customization without breaking the common API.
     */
    fun configureForPlatform(config: PlatformLogConfig.() -> Unit): PlatformLogTree
}