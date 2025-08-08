package com.chilinoodles.cedar.logging

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CedarTest {

    private lateinit var mockTree: MockLogTree

    @BeforeTest
    fun setup() {
        Cedar.clearForest()
        mockTree = MockLogTree()
        Cedar.plant(mockTree)
    }

    @AfterTest
    fun tearDown() {
        Cedar.clearForest()
    }

    @Test
    fun testTaggedLoggerCreation() = runTest {
        val logger = Cedar.tag("TestTag")
        assertNotNull(logger)
    }

    @Test
    fun testGetLoggerWithDefaultTag() = runTest {
        val logger = Cedar.getLogger()
        assertNotNull(logger)
    }

    @Test
    fun testGetLoggerWithCustomTag() = runTest {
        val logger = Cedar.getLogger("CustomTag")
        assertNotNull(logger)
    }

    @Test
    fun testVerboseLogging() = runTest {
        val logger = Cedar.tag("VerboseTest")
        logger.v("Verbose message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.VERBOSE, priority)
            assertEquals("VerboseTest", tag)
            assertEquals("Verbose message", message)
            assertEquals(null, throwable)
        }
    }

    @Test
    fun testVerboseLoggingWithThrowable() = runTest {
        val logger = Cedar.tag("VerboseTest")
        val exception = RuntimeException("Test exception")
        logger.v("Verbose message", exception)
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.VERBOSE, priority)
            assertEquals("VerboseTest", tag)
            assertEquals("Verbose message", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testDebugLogging() = runTest {
        val logger = Cedar.tag("DebugTest")
        logger.d("Debug message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.DEBUG, priority)
            assertEquals("DebugTest", tag)
            assertEquals("Debug message", message)
        }
    }

    @Test
    fun testDebugLoggingWithThrowableFirst() = runTest {
        val logger = Cedar.tag("DebugTest")
        val exception = RuntimeException("Test exception")
        logger.d(exception, "Debug message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.DEBUG, priority)
            assertEquals("DebugTest", tag)
            assertEquals("Debug message", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testInfoLogging() = runTest {
        val logger = Cedar.tag("InfoTest")
        logger.i("Info message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.INFO, priority)
            assertEquals("InfoTest", tag)
            assertEquals("Info message", message)
        }
    }

    @Test
    fun testWarningLogging() = runTest {
        val logger = Cedar.tag("WarningTest")
        logger.w("Warning message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.WARNING, priority)
            assertEquals("WarningTest", tag)
            assertEquals("Warning message", message)
        }
    }

    @Test
    fun testWarningLoggingWithThrowableFirst() = runTest {
        val logger = Cedar.tag("WarningTest")
        val exception = RuntimeException("Test exception")
        logger.w(exception, "Warning message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.WARNING, priority)
            assertEquals("WarningTest", tag)
            assertEquals("Warning message", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testWarningLoggingWithThrowableOnly() = runTest {
        val logger = Cedar.tag("WarningTest")
        val exception = RuntimeException("Test exception")
        logger.w(exception)
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.WARNING, priority)
            assertEquals("WarningTest", tag)
            assertEquals("", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testErrorLogging() = runTest {
        val logger = Cedar.tag("ErrorTest")
        logger.e("Error message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.ERROR, priority)
            assertEquals("ErrorTest", tag)
            assertEquals("Error message", message)
        }
    }

    @Test
    fun testErrorLoggingWithThrowableFirst() = runTest {
        val logger = Cedar.tag("ErrorTest")
        val exception = RuntimeException("Test exception")
        logger.e(exception, "Error message")
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.ERROR, priority)
            assertEquals("ErrorTest", tag)
            assertEquals("Error message", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testErrorLoggingWithThrowableOnly() = runTest {
        val logger = Cedar.tag("ErrorTest")
        val exception = RuntimeException("Test exception")
        logger.e(exception)
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.ERROR, priority)
            assertEquals("ErrorTest", tag)
            assertEquals("", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testGenericLogMethod() = runTest {
        val logger = Cedar.tag("GenericTest")
        val exception = RuntimeException("Test exception")
        logger.log(LogPriority.INFO, "Generic log message", exception)
        
        assertEquals(1, mockTree.logEntries().size)
        with(mockTree.logEntries().first()) {
            assertEquals(LogPriority.INFO, priority)
            assertEquals("GenericTest", tag)
            assertEquals("Generic log message", message)
            assertEquals(exception, throwable)
        }
    }

    @Test
    fun testLogScope() = runTest {
        val logger = Cedar.tag("ScopeTest")
        
        logger.scope(LogPriority.DEBUG, "Test operation").use {
            delay(10)
        }
        
        assertEquals(2, mockTree.logEntries().size)
        
        with(mockTree.logEntries()[0]) {
            assertEquals(LogPriority.DEBUG, priority)
            assertEquals("ScopeTest", tag)
            assertEquals("⟹ Test operation", message)
        }
        
        with(mockTree.logEntries()[1]) {
            assertEquals(LogPriority.DEBUG, priority)
            assertEquals("ScopeTest", tag)
            assertTrue(message.startsWith("⟸ Test operation (took"))
            assertTrue(message.endsWith(" ms)"))
        }
    }

    @Test
    fun testLogScopeWithDefaultPriority() = runTest {
        val logger = Cedar.tag("ScopeTest")
        
        logger.scope(message = "Default priority operation").use {
            delay(5)
        }
        
        assertEquals(2, mockTree.logEntries().size)
        assertEquals(LogPriority.DEBUG, mockTree.logEntries()[0].priority)
        assertEquals(LogPriority.DEBUG, mockTree.logEntries()[1].priority)
    }

    @Test
    fun testLogScopeDoubleClose() = runTest {
        val logger = Cedar.tag("ScopeTest")
        
        val scope = logger.scope(message = "Double close test")
        scope.close()
        scope.close()
        
        assertEquals(2, mockTree.logEntries().size)
    }

    @Test
    fun testStaticLoggingMethods() = runTest {
        Cedar.v("Verbose static")
        Cedar.d("Debug static")
        Cedar.i("Info static")
        Cedar.w("Warning static")
        Cedar.e("Error static")
        
        assertEquals(5, mockTree.logEntries().size)
        assertEquals(LogPriority.VERBOSE, mockTree.logEntries()[0].priority)
        assertEquals(LogPriority.DEBUG, mockTree.logEntries()[1].priority)
        assertEquals(LogPriority.INFO, mockTree.logEntries()[2].priority)
        assertEquals(LogPriority.WARNING, mockTree.logEntries()[3].priority)
        assertEquals(LogPriority.ERROR, mockTree.logEntries()[4].priority)
    }

    @Test
    fun testStaticLoggingMethodsWithThrowable() = runTest {
        val exception = RuntimeException("Test exception")
        
        Cedar.w(exception, "Warning with throwable")
        Cedar.e(exception, "Error with throwable")
        
        assertEquals(2, mockTree.logEntries().size)
        assertEquals(exception, mockTree.logEntries()[0].throwable)
        assertEquals(exception, mockTree.logEntries()[1].throwable)
    }

    @Test
    fun testMultipleTaggedLoggers() = runTest {
        val logger1 = Cedar.tag("Logger1")
        val logger2 = Cedar.tag("Logger2")
        
        logger1.i("Message from logger 1")
        logger2.i("Message from logger 2")
        
        assertEquals(2, mockTree.logEntries().size)
        assertEquals("Logger1", mockTree.logEntries()[0].tag)
        assertEquals("Logger2", mockTree.logEntries()[1].tag)
    }

    @Test
    fun testEmptyMessages() = runTest {
        val logger = Cedar.tag("EmptyTest")
        
        logger.v("")
        logger.d("")
        logger.i("")
        logger.w("")
        logger.e("")
        
        assertEquals(5, mockTree.logEntries().size)
        mockTree.logEntries().forEach { entry ->
            assertEquals("", entry.message)
        }
    }

    @Test
    fun testNullThrowableHandling() = runTest {
        val logger = Cedar.tag("NullTest")
        
        logger.v("Message", null)
        logger.d("Message", null)
        logger.i("Message", null)
        logger.w("Message", null)
        logger.e("Message", null)
        
        assertEquals(5, mockTree.logEntries().size)
        mockTree.logEntries().forEach { entry ->
            assertEquals(null, entry.throwable)
        }
    }
} 