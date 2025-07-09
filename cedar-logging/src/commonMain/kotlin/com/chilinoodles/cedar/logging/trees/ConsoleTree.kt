package com.chilinoodles.cedar.logging.trees

import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree

class ConsoleTree : LogTree {
    private var minPriority = LogPriority.VERBOSE
    
    fun withMinPriority(priority: LogPriority): ConsoleTree {
        minPriority = priority
        return this
    }
    
    override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
        return priority >= minPriority
    }
    
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        if (!isLoggable(tag, priority)) {
            return
        }
        
        val priorityIcon = when (priority) {
            LogPriority.VERBOSE -> "🔍"
            LogPriority.DEBUG -> "🐛"
            LogPriority.INFO -> "ℹ️"
            LogPriority.WARNING -> "⚠️"
            LogPriority.ERROR -> "❌"
        }
        
        val priorityName = priority.name.padEnd(7)
        val formattedMessage = "$priorityIcon $priorityName [$tag] $message"
        
        println(formattedMessage)
        
        throwable?.let {
            println("Exception: ${it.message}")
            it.printStackTrace()
        }
    }
} 