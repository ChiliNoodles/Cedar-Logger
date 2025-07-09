package com.chilinoodles.cedar.logging

/**
 * Log priority levels
 */
enum class LogPriority {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR;

    operator fun compareTo(other: Int): Int {
        return ordinal - other
    }

    fun isAtLeast(other: LogPriority): Boolean {
        return ordinal >= other.ordinal
    }
}