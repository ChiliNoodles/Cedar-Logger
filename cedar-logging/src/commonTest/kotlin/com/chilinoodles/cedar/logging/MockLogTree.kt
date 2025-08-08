package com.chilinoodles.cedar.logging

data class LogEntry(
    val priority: LogPriority,
    val tag: String,
    val message: String,
    val throwable: Throwable?
)

open class MockLogTree : LogTree {
    private val _logEntries = mutableListOf<LogEntry>()

    suspend fun logEntries(): List<LogEntry> = _logEntries.toList()

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
        _logEntries.clear()
    }

    override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
        return _isLoggable && priority >= minPriority
    }

    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        if (isLoggable(tag, priority)) {
            _logEntries.add(LogEntry(priority, tag, message, throwable))
        }
    }

    suspend fun clear() {
        _logEntries.clear()
    }

    suspend fun getEntriesWithTag(tag: String): List<LogEntry> {
        return _logEntries.filter { it.tag == tag }
    }

    suspend fun getEntriesWithPriority(priority: LogPriority): List<LogEntry> {
        return _logEntries.filter { it.priority == priority }
    }

    suspend fun getEntriesWithThrowable(): List<LogEntry> {
        return _logEntries.filter { it.throwable != null }
    }
} 