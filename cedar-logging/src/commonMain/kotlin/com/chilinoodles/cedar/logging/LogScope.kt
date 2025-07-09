package com.chilinoodles.cedar.logging

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class LogScope internal constructor(
    private val logger: TaggedLogger,
    private val priority: LogPriority,
    private val message: String,
    private val startTime: Long
) : AutoCloseable {
    private var isClosed = false

    @OptIn(ExperimentalTime::class)
    override fun close() {
        if (!isClosed) {
            isClosed = true
            val duration = Clock.System.now().toEpochMilliseconds() - startTime
            logger.log(priority, "⟸ $message (took $duration ms)")
        }
    }
}