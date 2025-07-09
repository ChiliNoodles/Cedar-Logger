package com.chilinoodles.cedar.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogTreeTest {

    @Test
    fun testLogTreeDefaultBehavior() {
        val tree = object : LogTree {
            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
            }
        }

        assertTrue(tree.isLoggable("TestTag", LogPriority.VERBOSE))
        assertTrue(tree.isLoggable("TestTag", LogPriority.DEBUG))
        assertTrue(tree.isLoggable("TestTag", LogPriority.INFO))
        assertTrue(tree.isLoggable("TestTag", LogPriority.WARNING))
        assertTrue(tree.isLoggable("TestTag", LogPriority.ERROR))
        assertTrue(tree.isLoggable(null, LogPriority.DEBUG))
    }

    @Test
    fun testLogTreeSetupAndTeardown() {
        var setupCalled = false
        var teardownCalled = false

        val tree = object : LogTree {
            override fun setup() {
                setupCalled = true
            }

            override fun tearDown() {
                teardownCalled = true
            }

            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
            }
        }

        assertFalse(setupCalled)
        assertFalse(teardownCalled)

        tree.setup()
        assertTrue(setupCalled)
        assertFalse(teardownCalled)

        tree.tearDown()
        assertTrue(teardownCalled)
    }

    @Test
    fun testLogTreeCustomIsLoggable() {
        val tree = object : LogTree {
            override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
                return priority >= LogPriority.WARNING
            }

            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
            }
        }

        assertFalse(tree.isLoggable("TestTag", LogPriority.VERBOSE))
        assertFalse(tree.isLoggable("TestTag", LogPriority.DEBUG))
        assertFalse(tree.isLoggable("TestTag", LogPriority.INFO))
        assertTrue(tree.isLoggable("TestTag", LogPriority.WARNING))
        assertTrue(tree.isLoggable("TestTag", LogPriority.ERROR))
    }

    @Test
    fun testLogTreeWithNullTag() {
        val loggedEntries = mutableListOf<LogEntry>()

        val tree = object : LogTree {
            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
                loggedEntries.add(LogEntry(priority, tag, message, throwable))
            }
        }

        tree.log(LogPriority.INFO, "TestTag", "Message", null)

        assertEquals(1, loggedEntries.size)
        assertEquals("TestTag", loggedEntries[0].tag)
    }

    @Test
    fun testLogTreeWithEmptyTag() {
        val loggedEntries = mutableListOf<LogEntry>()

        val tree = object : LogTree {
            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
                loggedEntries.add(LogEntry(priority, tag, message, throwable))
            }
        }

        tree.log(LogPriority.INFO, "", "Message", null)

        assertEquals(1, loggedEntries.size)
        assertEquals("", loggedEntries[0].tag)
    }

    @Test
    fun testLogTreeWithEmptyMessage() {
        val loggedEntries = mutableListOf<LogEntry>()

        val tree = object : LogTree {
            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
                loggedEntries.add(LogEntry(priority, tag, message, throwable))
            }
        }

        tree.log(LogPriority.INFO, "TestTag", "", null)

        assertEquals(1, loggedEntries.size)
        assertEquals("", loggedEntries[0].message)
    }

    @Test
    fun testCustomLogTreeImplementation() {
        var setupCallCount = 0
        var teardownCallCount = 0
        var logCallCount = 0

        val customTree = object : LogTree {
            override fun setup() {
                setupCallCount++
            }

            override fun tearDown() {
                teardownCallCount++
            }

            override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
                return tag != "BLOCKED" && priority >= LogPriority.INFO
            }

            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
                logCallCount++
            }
        }

        customTree.setup()
        assertEquals(1, setupCallCount)

        customTree.log(LogPriority.DEBUG, "TAG", "Debug message", null)
        assertEquals(1, logCallCount)

        assertFalse(customTree.isLoggable("BLOCKED", LogPriority.ERROR))
        assertFalse(customTree.isLoggable("TAG", LogPriority.DEBUG))
        assertTrue(customTree.isLoggable("TAG", LogPriority.INFO))

        customTree.tearDown()
        assertEquals(1, teardownCallCount)
    }
} 