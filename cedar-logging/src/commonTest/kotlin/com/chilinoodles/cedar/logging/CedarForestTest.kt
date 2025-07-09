package com.chilinoodles.cedar.logging

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CedarForestTest {

    private lateinit var mockTree1: MockLogTree
    private lateinit var mockTree2: MockLogTree
    private lateinit var mockTree3: MockLogTree

    @BeforeTest
    fun setup() {
        Cedar.clearForest()
        mockTree1 = MockLogTree()
        mockTree2 = MockLogTree()
        mockTree3 = MockLogTree()
    }

    @AfterTest
    fun tearDown() {
        Cedar.clearForest()
    }

    @Test
    fun testInitialState() {
        assertEquals(0, Cedar.treeCount)
        assertTrue(Cedar.forest().isEmpty())
    }

    @Test
    fun testPlantSingleTree() {
        Cedar.plant(mockTree1)
        
        assertEquals(1, Cedar.treeCount)
        assertEquals(1, Cedar.forest().size)
        assertTrue(Cedar.forest().contains(mockTree1))
        assertTrue(mockTree1.isSetup)
    }

    @Test
    fun testPlantMultipleTreesIndividually() {
        Cedar.plant(mockTree1)
        Cedar.plant(mockTree2)
        Cedar.plant(mockTree3)
        
        assertEquals(3, Cedar.treeCount)
        assertEquals(3, Cedar.forest().size)
        assertTrue(Cedar.forest().containsAll(listOf(mockTree1, mockTree2, mockTree3)))
        assertTrue(mockTree1.isSetup)
        assertTrue(mockTree2.isSetup)
        assertTrue(mockTree3.isSetup)
    }

    @Test
    fun testPlantMultipleTreesAtOnce() {
        Cedar.plant(mockTree1, mockTree2, mockTree3)
        
        assertEquals(3, Cedar.treeCount)
        assertEquals(3, Cedar.forest().size)
        assertTrue(Cedar.forest().containsAll(listOf(mockTree1, mockTree2, mockTree3)))
        assertTrue(mockTree1.isSetup)
        assertTrue(mockTree2.isSetup)
        assertTrue(mockTree3.isSetup)
    }

    @Test
    fun testUprootTree() {
        Cedar.plant(mockTree1, mockTree2, mockTree3)
        
        Cedar.uproot(mockTree2)
        
        assertEquals(2, Cedar.treeCount)
        assertEquals(2, Cedar.forest().size)
        assertTrue(Cedar.forest().contains(mockTree1))
        assertFalse(Cedar.forest().contains(mockTree2))
        assertTrue(Cedar.forest().contains(mockTree3))
        assertFalse(mockTree2.isSetup)
        assertTrue(mockTree2.logEntries.isEmpty())
    }

    @Test
    fun testUprootNonExistentTree() {
        val nonExistentTree = MockLogTree()
        Cedar.plant(mockTree1)
        
        Cedar.uproot(nonExistentTree)
        
        assertEquals(1, Cedar.treeCount)
        assertTrue(Cedar.forest().contains(mockTree1))
    }

    @Test
    fun testClearForest() {
        Cedar.plant(mockTree1, mockTree2, mockTree3)
        
        Cedar.clearForest()
        
        assertEquals(0, Cedar.treeCount)
        assertTrue(Cedar.forest().isEmpty())
        assertFalse(mockTree1.isSetup)
        assertFalse(mockTree2.isSetup)
        assertFalse(mockTree3.isSetup)
        assertTrue(mockTree1.logEntries.isEmpty())
        assertTrue(mockTree2.logEntries.isEmpty())
        assertTrue(mockTree3.logEntries.isEmpty())
    }

    @Test
    fun testLogDistributionToAllTrees() {
        Cedar.plant(mockTree1, mockTree2, mockTree3)
        
        Cedar.d("Test message")
        
        assertEquals(1, mockTree1.logEntries.size)
        assertEquals(1, mockTree2.logEntries.size)
        assertEquals(1, mockTree3.logEntries.size)
        
        mockTree1.logEntries.forEach { entry ->
            assertEquals(LogPriority.DEBUG, entry.priority)
            assertEquals("AppLogger", entry.tag)
            assertEquals("Test message", entry.message)
        }
    }

    @Test
    fun testLogDistributionWithFilteredTrees() {
        mockTree1.setMinPriority(LogPriority.INFO)
        mockTree2.setMinPriority(LogPriority.DEBUG)
        mockTree3.setLoggable(false)
        
        Cedar.plant(mockTree1, mockTree2, mockTree3)
        
        Cedar.d("Debug message")
        
        assertEquals(0, mockTree1.logEntries.size)
        assertEquals(1, mockTree2.logEntries.size)
        assertEquals(0, mockTree3.logEntries.size)
        
        Cedar.i("Info message")
        
        assertEquals(1, mockTree1.logEntries.size)
        assertEquals(2, mockTree2.logEntries.size)
        assertEquals(0, mockTree3.logEntries.size)
    }

    @Test
    fun testLogWithoutPlantedTrees() {
        Cedar.clearForest()
        
        Cedar.d("Test message")
    }

    @Test
    fun testForestListImmutability() {
        Cedar.plant(mockTree1, mockTree2)
        
        val forest = Cedar.forest()
        assertEquals(2, forest.size)
        
        Cedar.plant(mockTree3)
        
        assertEquals(2, forest.size)
        assertEquals(3, Cedar.forest().size)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testThreadSafetyPlanting() = runBlocking {
        val trees = List(10) { MockLogTree() }
        val mutex = Mutex()
        val plantedTrees = mutableListOf<MockLogTree>()
        
        val jobs = trees.map { tree ->
            GlobalScope.launch(Dispatchers.Default) {
                Cedar.plant(tree)
                mutex.withLock {
                    plantedTrees.add(tree)
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        assertEquals(10, Cedar.treeCount)
        assertEquals(10, plantedTrees.size)
        assertTrue(Cedar.forest().containsAll(trees))
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testThreadSafetyUprooting() = runBlocking {
        val trees = List(10) { MockLogTree() }
        Cedar.plant(*trees.toTypedArray())
        
        val jobs = trees.map { tree ->
            GlobalScope.launch(Dispatchers.Default) {
                Cedar.uproot(tree)
            }
        }
        
        jobs.forEach { it.join() }
        
        assertEquals(0, Cedar.treeCount)
        assertTrue(Cedar.forest().isEmpty())
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testThreadSafetyLogging() = runBlocking {
        Cedar.plant(mockTree1)
        
        val jobs = (1..100).map { index ->
            GlobalScope.launch(Dispatchers.Default) {
                Cedar.d("Message $index")
            }
        }
        
        jobs.forEach { it.join() }
        
        assertEquals(100, mockTree1.logEntries.size)
        val messages = mockTree1.logEntries.map { it.message }.toSet()
        assertEquals(100, messages.size)
    }

    @Test
    fun testTreeSetupAndTeardownCalls() {
        assertFalse(mockTree1.isSetup)
        assertFalse(mockTree2.isSetup)
        
        Cedar.plant(mockTree1, mockTree2)
        
        assertTrue(mockTree1.isSetup)
        assertTrue(mockTree2.isSetup)
        
        Cedar.uproot(mockTree1)
        
        assertFalse(mockTree1.isSetup)
        assertTrue(mockTree2.isSetup)
        
        Cedar.clearForest()
        
        assertFalse(mockTree1.isSetup)
        assertFalse(mockTree2.isSetup)
    }

    @Test
    fun testPlantSameTreeMultipleTimes() {
        Cedar.plant(mockTree1)
        Cedar.plant(mockTree1)
        Cedar.plant(mockTree1)
        
        assertEquals(3, Cedar.treeCount)
        assertEquals(3, Cedar.forest().size)
        
        Cedar.d("Test message")
        
        assertEquals(3, mockTree1.logEntries.size)
    }

    @Test
    fun testUprootSameTreeMultipleTimes() {
        Cedar.plant(mockTree1, mockTree1, mockTree1)
        
        Cedar.uproot(mockTree1)
        
        assertEquals(2, Cedar.treeCount)
        assertEquals(2, Cedar.forest().size)
        
        Cedar.uproot(mockTree1)
        
        assertEquals(1, Cedar.treeCount)
        assertEquals(1, Cedar.forest().size)
        
        Cedar.uproot(mockTree1)
        
        assertEquals(0, Cedar.treeCount)
        assertEquals(0, Cedar.forest().size)
    }

    @Test
    fun testLogWithException() {
        Cedar.plant(mockTree1, mockTree2)
        
        val exception = RuntimeException("Test exception")
        Cedar.e(exception, "Error occurred")
        
        assertEquals(1, mockTree1.logEntries.size)
        assertEquals(1, mockTree2.logEntries.size)
        
        assertEquals(exception, mockTree1.logEntries.first().throwable)
        assertEquals(exception, mockTree2.logEntries.first().throwable)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testMixedOperationsConcurrency() = runBlocking {
        val jobs = mutableListOf<Job>()
        
        repeat(20) { index ->
            jobs.add(GlobalScope.launch(Dispatchers.Default) {
                when (index % 4) {
                    0 -> Cedar.plant(MockLogTree())
                    1 -> Cedar.d("Message $index")
                    2 -> if (Cedar.forest().isNotEmpty()) Cedar.uproot(Cedar.forest().first())
                    3 -> Cedar.i("Info $index")
                }
            })
        }
        
        jobs.forEach { it.join() }
        
        assertTrue(Cedar.treeCount == 0)
    }
} 