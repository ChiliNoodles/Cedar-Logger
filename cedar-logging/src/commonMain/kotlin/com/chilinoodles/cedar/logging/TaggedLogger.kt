package com.chilinoodles.cedar.logging

import com.chilinoodles.cedar.logging.Cedar.Forest.logToAllTrees
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TaggedLogger internal constructor(private val logTag: String) {
    fun v(message: String, throwable: Throwable? = null) {
        logToAllTrees(LogPriority.VERBOSE, logTag, message, throwable)
    }

    fun d(throwable: Throwable? = null, message: String) {
        logToAllTrees(LogPriority.DEBUG, logTag, message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null) {
        logToAllTrees(LogPriority.DEBUG, logTag, message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null) {
        logToAllTrees(LogPriority.INFO, logTag, message, throwable)
    }

    fun w(throwable: Throwable?, message: String = "") {
        logToAllTrees(LogPriority.WARNING, logTag, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        logToAllTrees(LogPriority.WARNING, logTag, message, throwable)
    }

    fun e(throwable: Throwable? = null, message: String = "") {
        logToAllTrees(LogPriority.ERROR, logTag, message, throwable)
    }

    fun e(message: String = "", throwable: Throwable? = null) {
        logToAllTrees(LogPriority.ERROR, logTag, message, throwable)
    }

    fun log(priority: LogPriority, message: String, throwable: Throwable? = null) {
        logToAllTrees(priority, logTag, message, throwable)
    }

    @OptIn(ExperimentalTime::class)
    fun scope(priority: LogPriority = LogPriority.DEBUG, message: String): LogScope {
        val startTime = Clock.System.now().toEpochMilliseconds()
        log(priority, "⟹ $message")
        return LogScope(this, priority, message, startTime)
    }
}
