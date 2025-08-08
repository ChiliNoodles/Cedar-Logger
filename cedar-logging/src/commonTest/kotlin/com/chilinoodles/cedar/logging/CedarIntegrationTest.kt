package com.chilinoodles.cedar.logging

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CedarIntegrationTest {

    private lateinit var debugTree: MockLogTree
    private lateinit var productionTree: MockLogTree
    private lateinit var errorOnlyTree: MockLogTree

    @BeforeTest
    fun setup() {
        Cedar.clearForest()

        debugTree = MockLogTree()
        debugTree.setMinPriority(LogPriority.VERBOSE)

        productionTree = MockLogTree()
        productionTree.setMinPriority(LogPriority.INFO)

        errorOnlyTree = MockLogTree()
        errorOnlyTree.setMinPriority(LogPriority.ERROR)
    }

    @AfterTest
    fun tearDown() {
        Cedar.clearForest()
    }

    @Test
    fun testCompleteLoggingWorkflow() = runTest {
        Cedar.plant(debugTree, productionTree, errorOnlyTree)

        val logger = Cedar.tag("WorkflowTest")

        logger.v("Verbose message")
        logger.d("Debug message")
        logger.i("Info message")
        logger.w("Warning message")
        logger.e("Error message")

        assertEquals(5, debugTree.logEntries().size)
        assertEquals(3, productionTree.logEntries().size)
        assertEquals(1, errorOnlyTree.logEntries().size)

        val debugPriorities = debugTree.logEntries().map { it.priority }
        assertEquals(
            listOf(
                LogPriority.VERBOSE,
                LogPriority.DEBUG,
                LogPriority.INFO,
                LogPriority.WARNING,
                LogPriority.ERROR
            ),
            debugPriorities
        )

        val productionPriorities = productionTree.logEntries().map { it.priority }
        assertEquals(
            listOf(LogPriority.INFO, LogPriority.WARNING, LogPriority.ERROR),
            productionPriorities
        )

        val errorPriorities = errorOnlyTree.logEntries().map { it.priority }
        assertEquals(
            listOf(LogPriority.ERROR),
            errorPriorities
        )
    }

    @Test
    fun testDynamicTreeManagement() = runTest {
        Cedar.plant(debugTree)

        Cedar.d("Message 1")
        assertEquals(1, debugTree.logEntries().size)

        Cedar.plant(productionTree)

        Cedar.i("Message 2")
        assertEquals(2, debugTree.logEntries().size)
        assertEquals(1, productionTree.logEntries().size)

        Cedar.uproot(debugTree)

        Cedar.w("Message 3")
        assertEquals(0, debugTree.logEntries().size)
        assertEquals(2, productionTree.logEntries().size)

        Cedar.plant(errorOnlyTree)

        Cedar.e("Message 4")
        assertEquals(0, debugTree.logEntries().size)
        assertEquals(3, productionTree.logEntries().size)
        assertEquals(1, errorOnlyTree.logEntries().size)
    }

    @Test
    fun testFilteringWithMultipleTrees() = runTest {
        debugTree.setLoggable(false)

        Cedar.plant(debugTree, productionTree, errorOnlyTree)

        Cedar.v("Verbose")
        Cedar.d("Debug")
        Cedar.i("Info")
        Cedar.w("Warning")
        Cedar.e("Error")

        assertEquals(0, debugTree.logEntries().size)
        assertEquals(3, productionTree.logEntries().size)
        assertEquals(1, errorOnlyTree.logEntries().size)
    }

    @Test
    fun testMultipleLoggersWithDifferentTags() = runTest {
        Cedar.plant(debugTree)

        val networkLogger = Cedar.tag("Network")
        val dbLogger = Cedar.tag("Database")
        val uiLogger = Cedar.tag("UI")

        networkLogger.i("HTTP request started")
        dbLogger.d("Executing query")
        uiLogger.w("Deprecated method used")

        assertEquals(3, debugTree.logEntries().size)

        val tags = debugTree.logEntries().map { it.tag }
        assertEquals(listOf("Network", "Database", "UI"), tags)

        val networkEntries = debugTree.getEntriesWithTag("Network")
        assertEquals(1, networkEntries.size)
        assertEquals("HTTP request started", networkEntries.first().message)
    }

    @Test
    fun testExceptionLogging() = runTest {
        Cedar.plant(debugTree, productionTree, errorOnlyTree)

        val networkException = RuntimeException("Network timeout")
        val dbException = IllegalStateException("Database connection failed")

        val logger = Cedar.tag("ExceptionTest")

        logger.w("Network issue detected", networkException)
        logger.e(dbException, "Critical database error")

        val debugExceptions = debugTree.getEntriesWithThrowable()
        assertEquals(2, debugExceptions.size)
        assertEquals(networkException, debugExceptions[0].throwable)
        assertEquals(dbException, debugExceptions[1].throwable)

        val productionExceptions = productionTree.getEntriesWithThrowable()
        assertEquals(2, productionExceptions.size)

        val errorOnlyExceptions = errorOnlyTree.getEntriesWithThrowable()
        assertEquals(1, errorOnlyExceptions.size)
        assertEquals(dbException, errorOnlyExceptions[0].throwable)
    }

    @Test
    fun testLogScopingWithMultipleTrees() = runTest {
        Cedar.plant(debugTree, productionTree)

        val logger = Cedar.tag("ScopeTest")

        logger.scope(LogPriority.INFO, "User authentication").use {
            delay(10)
            logger.d("Validating credentials")
            logger.i("Authentication successful")
        }

        assertEquals(4, debugTree.logEntries().size)
        assertEquals(3, productionTree.logEntries().size)

        val debugMessages = debugTree.logEntries().map { it.message }
        assertTrue(debugMessages[0].contains("⟹ User authentication"))
        assertEquals("Validating credentials", debugMessages[1])
        assertEquals("Authentication successful", debugMessages[2])
        assertTrue(debugMessages[3].contains("⟸ User authentication"))

        val productionMessages = productionTree.logEntries().map { it.message }
        assertTrue(productionMessages[0].contains("⟹ User authentication"))
        assertEquals("Authentication successful", productionMessages[1])
        assertTrue(productionMessages[2].contains("⟸ User authentication"))
    }

    @Test
    fun testNestedLogScopes() = runTest {
        Cedar.plant(debugTree)

        val logger = Cedar.tag("NestedScope")

        logger.scope(LogPriority.INFO, "Outer operation").use {
            delay(5)
            logger.d("Outer step 1")

            logger.scope(LogPriority.DEBUG, "Inner operation").use {
                delay(5)
                logger.d("Inner step 1")
                logger.d("Inner step 2")
            }

            logger.d("Outer step 2")
        }

        assertEquals(8, debugTree.logEntries().size)

        val messages = debugTree.logEntries().map { it.message }
        assertTrue(messages[0].contains("⟹ Outer operation"))
        assertEquals("Outer step 1", messages[1])
        assertTrue(messages[2].contains("⟹ Inner operation"))
        assertEquals("Inner step 1", messages[3])
        assertEquals("Inner step 2", messages[4])
        assertTrue(messages[5].contains("⟸ Inner operation"))
        assertEquals("Outer step 2", messages[6])
        assertTrue(messages[7].contains("⟸ Outer operation"))
    }

    @Test
    fun testStaticLoggingWithMultipleTrees() = runTest {
        Cedar.plant(debugTree, productionTree, errorOnlyTree)

        Cedar.v("Static verbose")
        Cedar.d("Static debug")
        Cedar.i("Static info")
        Cedar.w("Static warning")
        Cedar.e("Static error")

        assertEquals(5, debugTree.logEntries().size)
        assertEquals(3, productionTree.logEntries().size)
        assertEquals(1, errorOnlyTree.logEntries().size)

        debugTree.logEntries().forEach { entry ->
            assertEquals("AppLogger", entry.tag)
        }
    }

    @Test
    fun testRealWorldScenario() = runTest {
        val developmentTree = MockLogTree()
        developmentTree.setMinPriority(LogPriority.VERBOSE)

        val analyticsTree = MockLogTree()
        analyticsTree.setMinPriority(LogPriority.WARNING)


        Cedar.plant(developmentTree, analyticsTree)

        val apiLogger = Cedar.tag("ApiClient")
        val dbLogger = Cedar.tag("Database")
        val authLogger = Cedar.tag("Authentication")

        apiLogger.scope(LogPriority.INFO, "User profile request").use {
            delay(10)

            authLogger.d("Checking token validity")

            try {
                apiLogger.d("Sending HTTP request")
                apiLogger.i("Received response: 200 OK")

                dbLogger.scope(LogPriority.DEBUG, "Caching user data").use {
                    delay(5)
                    dbLogger.v("Preparing SQL statement")
                    dbLogger.d("Executing INSERT")
                    dbLogger.i("User data cached successfully")
                }

            } catch (e: Exception) {
                apiLogger.e(e, "Request failed")
            }
        }

        assertTrue(developmentTree.logEntries().size >= 8)
        assertTrue(analyticsTree.logEntries().isEmpty())

        authLogger.w("Token expiring soon")
        apiLogger.e("Rate limit exceeded")

        assertEquals(2, analyticsTree.logEntries().size)
        assertEquals(LogPriority.WARNING, analyticsTree.logEntries()[0].priority)
        assertEquals(LogPriority.ERROR, analyticsTree.logEntries()[1].priority)

        val apiEntries = developmentTree.getEntriesWithTag("ApiClient")
        assertTrue(apiEntries.isNotEmpty())

        val dbEntries = developmentTree.getEntriesWithTag("Database")
        assertTrue(dbEntries.isNotEmpty())
    }

    @Test
    fun testTreeSetupTeardownInWorkflow() = runTest {
        Cedar.plant(debugTree, productionTree)

        assertTrue(debugTree.isSetup)
        assertTrue(productionTree.isSetup)

        Cedar.i("Test message")
        assertEquals(1, debugTree.logEntries().size)
        assertEquals(1, productionTree.logEntries().size)

        Cedar.uproot(debugTree)

        assertFalse(debugTree.isSetup)
        assertTrue(productionTree.isSetup)
        assertTrue(debugTree.logEntries().isEmpty())

        Cedar.i("Another message")
        assertEquals(0, debugTree.logEntries().size)
        assertEquals(2, productionTree.logEntries().size)

        Cedar.clearForest()

        assertFalse(debugTree.isSetup)
        assertFalse(productionTree.isSetup)
        assertTrue(debugTree.logEntries().isEmpty())
        assertTrue(productionTree.logEntries().isEmpty())
    }

    @Test
    fun testErrorHandlingInComplexScenario() = runTest {
        val tolerantTree = MockLogTree()
        val strictTree = object : MockLogTree() {
            override fun log(
                priority: LogPriority,
                tag: String,
                message: String,
                throwable: Throwable?
            ) {
                if (tag == "FORBIDDEN") {
                    throw RuntimeException("Forbidden tag")
                }
                super.log(priority, tag, message, throwable)
            }
        }

        Cedar.plant(tolerantTree, strictTree)

        try {
            Cedar.tag("ALLOWED").i("This should work")
            assertEquals(1, tolerantTree.logEntries().size)
            assertEquals(1, strictTree.logEntries().size)

            Cedar.tag("FORBIDDEN").e("This should cause an error in strict tree")
        } catch (e: RuntimeException) {
        }

        assertEquals(2, tolerantTree.logEntries().size)
    }

    @Test
    fun testPerformanceWithManyTrees() =
        runTest {
            val trees = List(20) { MockLogTree() }
            Cedar.plant(*trees.toTypedArray())

            val logger = Cedar.tag("PerformanceTest")

            repeat(100) { index ->
                logger.i("Message $index")
            }

            trees.forEach { tree ->
                assertEquals(100, tree.logEntries().size)
            }

            assertEquals(20, Cedar.treeCount)
        }

    @Test
    fun testComplexFilteringScenario() =
        runTest {

            val debugOnlyTree = MockLogTree()
            debugOnlyTree.setMinPriority(LogPriority.DEBUG)
            debugOnlyTree.setLoggable(true)

            val conditionalTree = object : MockLogTree() {
                override fun isLoggable(tag: String?, priority: LogPriority): Boolean {
                    return tag?.startsWith("App") == true && priority >= LogPriority.INFO
                }
            }

            Cedar.plant(debugOnlyTree, conditionalTree)

            Cedar.tag("AppService").v("Verbose")
            Cedar.tag("AppService").d("Debug")
            Cedar.tag("AppService").i("Info")
            Cedar.tag("NetworkService").i("Info")
            Cedar.tag("DatabaseService").w("Warning")

            assertEquals(4, debugOnlyTree.logEntries().size)
            assertEquals(1, conditionalTree.logEntries().size)
            assertEquals("AppService", conditionalTree.logEntries().first().tag)
            assertEquals(LogPriority.INFO, conditionalTree.logEntries().first().priority)
        }
} 