package com.chilinoodles.cedar.logging

/**
 * A tree-like logging system that allows planting different log handlers.
 */
interface LogTree {
    /**
     * Log a message with the specified priority and optional throwable
     */
    fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable? = null)
    
    /**
     * For handling initialization
     */
    fun setup() {}
    
    /**
     * For handling cleanup
     */
    fun tearDown() {}
    
    /**
     * Called when determining if a certain priority is loggable
     */
    fun isLoggable(tag: String?, priority: LogPriority): Boolean = true
}