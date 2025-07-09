package com.chilinoodles.cedar.logging

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LogEntry(
    val priority: LogPriority,
    val tag: String,
    val message: String,
    val throwable: Throwable?
)

open class MockLogTree : LogTree {
    private val _logEntries = mutableListOf<LogEntry>()
    private val mutex = Mutex()

    val logEntries: List<LogEntry>
        get() = runBlocking { mutex.withLock { _logEntries.toList() } }

    private var _isSetup = false
    private var _isLoggable = true
    private var minPriority = LogPriority.VERBOSE

    val isSetup: Boolean get() = _isSetup
    val isLoggable: Boolean get() = _isLoggable

    fun setLoggable(loggable: Boolean) {
        _isLoggable = loggable
    }

    fun setMinPriority(priority: LogPriority) {
        minPriority = priority
    }

    override fun setup() {
        _isSetup = true
    }

    override fun tearDown() {
        _isSetup = false
        runBlocking { mutex.withLock { _logEntries.clear() } }
    }

    override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
        return _isLoggable && priority >= minPriority
    }

    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        if (isLoggable(tag, priority)) {
            runBlocking {
                mutex.withLock {
                    _logEntries.add(LogEntry(priority, tag, message, throwable))
                }
            }
        }
    }

    fun clear() {
        runBlocking { mutex.withLock { _logEntries.clear() } }
    }

    fun getEntriesWithTag(tag: String): List<LogEntry> {
        return runBlocking { mutex.withLock { _logEntries.filter { it.tag == tag } } }
    }

    fun getEntriesWithPriority(priority: LogPriority): List<LogEntry> {
        return runBlocking { mutex.withLock { _logEntries.filter { it.priority == priority } } }
    }

    fun getEntriesWithThrowable(): List<LogEntry> {
        return runBlocking { mutex.withLock { _logEntries.filter { it.throwable != null } } }
    }
} 