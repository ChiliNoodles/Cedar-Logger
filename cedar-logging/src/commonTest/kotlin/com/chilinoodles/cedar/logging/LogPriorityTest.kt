package com.chilinoodles.cedar.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogPriorityTest {

    @Test
    fun testOrdinalValues() {
        assertEquals(0, LogPriority.VERBOSE.ordinal)
        assertEquals(1, LogPriority.DEBUG.ordinal)
        assertEquals(2, LogPriority.INFO.ordinal)
        assertEquals(3, LogPriority.WARNING.ordinal)
        assertEquals(4, LogPriority.ERROR.ordinal)
    }

    @Test
    fun testEnumValues() {
        val expectedValues = arrayOf(
            LogPriority.VERBOSE,
            LogPriority.DEBUG,
            LogPriority.INFO,
            LogPriority.WARNING,
            LogPriority.ERROR
        )

        val actualValues = LogPriority.entries.toTypedArray()
        assertEquals(expectedValues.size, actualValues.size)

        expectedValues.forEachIndexed { index, expected ->
            assertEquals(expected, actualValues[index])
        }
    }

    @Test
    fun testNaturalOrdering() {
        assertTrue(LogPriority.VERBOSE < LogPriority.DEBUG)
        assertTrue(LogPriority.DEBUG < LogPriority.INFO)
        assertTrue(LogPriority.INFO < LogPriority.WARNING)
        assertTrue(LogPriority.WARNING < LogPriority.ERROR)

        assertFalse(LogPriority.ERROR < LogPriority.WARNING)
        assertFalse(LogPriority.WARNING < LogPriority.INFO)
        assertFalse(LogPriority.INFO < LogPriority.DEBUG)
        assertFalse(LogPriority.DEBUG < LogPriority.VERBOSE)
    }

    @Test
    fun testGreaterThanComparison() {
        assertTrue(LogPriority.ERROR > LogPriority.WARNING)
        assertTrue(LogPriority.WARNING > LogPriority.INFO)
        assertTrue(LogPriority.INFO > LogPriority.DEBUG)
        assertTrue(LogPriority.DEBUG > LogPriority.VERBOSE)

        assertFalse(LogPriority.VERBOSE > LogPriority.DEBUG)
        assertFalse(LogPriority.DEBUG > LogPriority.INFO)
        assertFalse(LogPriority.INFO > LogPriority.WARNING)
        assertFalse(LogPriority.WARNING > LogPriority.ERROR)
    }

    @Test
    fun testEqualityComparison() {
        assertTrue(LogPriority.VERBOSE == LogPriority.VERBOSE)
        assertTrue(LogPriority.DEBUG == LogPriority.DEBUG)
        assertTrue(LogPriority.INFO == LogPriority.INFO)
        assertTrue(LogPriority.WARNING == LogPriority.WARNING)
        assertTrue(LogPriority.ERROR == LogPriority.ERROR)

        assertFalse(LogPriority.VERBOSE == LogPriority.DEBUG)
        assertFalse(LogPriority.DEBUG == LogPriority.INFO)
        assertFalse(LogPriority.INFO == LogPriority.WARNING)
        assertFalse(LogPriority.WARNING == LogPriority.ERROR)
    }

    @Test
    fun testGreaterThanOrEqualComparison() {
        assertTrue(LogPriority.ERROR >= LogPriority.ERROR)
        assertTrue(LogPriority.ERROR >= LogPriority.WARNING)
        assertTrue(LogPriority.ERROR >= LogPriority.INFO)
        assertTrue(LogPriority.ERROR >= LogPriority.DEBUG)
        assertTrue(LogPriority.ERROR >= LogPriority.VERBOSE)

        assertTrue(LogPriority.WARNING >= LogPriority.WARNING)
        assertTrue(LogPriority.WARNING >= LogPriority.INFO)
        assertTrue(LogPriority.WARNING >= LogPriority.DEBUG)
        assertTrue(LogPriority.WARNING >= LogPriority.VERBOSE)

        assertFalse(LogPriority.VERBOSE >= LogPriority.DEBUG)
        assertFalse(LogPriority.DEBUG >= LogPriority.INFO)
        assertFalse(LogPriority.INFO >= LogPriority.WARNING)
        assertFalse(LogPriority.WARNING >= LogPriority.ERROR)
    }

    @Test
    fun testLessThanOrEqualComparison() {
        assertTrue(LogPriority.VERBOSE <= LogPriority.VERBOSE)
        assertTrue(LogPriority.VERBOSE <= LogPriority.DEBUG)
        assertTrue(LogPriority.VERBOSE <= LogPriority.INFO)
        assertTrue(LogPriority.VERBOSE <= LogPriority.WARNING)
        assertTrue(LogPriority.VERBOSE <= LogPriority.ERROR)

        assertTrue(LogPriority.DEBUG <= LogPriority.DEBUG)
        assertTrue(LogPriority.DEBUG <= LogPriority.INFO)
        assertTrue(LogPriority.DEBUG <= LogPriority.WARNING)
        assertTrue(LogPriority.DEBUG <= LogPriority.ERROR)

        assertFalse(LogPriority.ERROR <= LogPriority.WARNING)
        assertFalse(LogPriority.WARNING <= LogPriority.INFO)
        assertFalse(LogPriority.INFO <= LogPriority.DEBUG)
        assertFalse(LogPriority.DEBUG <= LogPriority.VERBOSE)
    }

    @Test
    fun testCustomCompareToIntMethod() {
        assertEquals(-1, LogPriority.VERBOSE.compareTo(1))
        assertEquals(0, LogPriority.DEBUG.compareTo(1))
        assertEquals(1, LogPriority.INFO.compareTo(1))
        assertEquals(2, LogPriority.WARNING.compareTo(1))
        assertEquals(3, LogPriority.ERROR.compareTo(1))

        assertEquals(-4, LogPriority.VERBOSE.compareTo(4))
        assertEquals(-3, LogPriority.DEBUG.compareTo(4))
        assertEquals(-2, LogPriority.INFO.compareTo(4))
        assertEquals(-1, LogPriority.WARNING.compareTo(4))
        assertEquals(0, LogPriority.ERROR.compareTo(4))
    }

    @Test
    fun testIsAtLeastMethod() {
        assertTrue(LogPriority.ERROR.isAtLeast(LogPriority.VERBOSE))
        assertTrue(LogPriority.ERROR.isAtLeast(LogPriority.DEBUG))
        assertTrue(LogPriority.ERROR.isAtLeast(LogPriority.INFO))
        assertTrue(LogPriority.ERROR.isAtLeast(LogPriority.WARNING))
        assertTrue(LogPriority.ERROR.isAtLeast(LogPriority.ERROR))

        assertTrue(LogPriority.WARNING.isAtLeast(LogPriority.VERBOSE))
        assertTrue(LogPriority.WARNING.isAtLeast(LogPriority.DEBUG))
        assertTrue(LogPriority.WARNING.isAtLeast(LogPriority.INFO))
        assertTrue(LogPriority.WARNING.isAtLeast(LogPriority.WARNING))
        assertFalse(LogPriority.WARNING.isAtLeast(LogPriority.ERROR))

        assertTrue(LogPriority.INFO.isAtLeast(LogPriority.VERBOSE))
        assertTrue(LogPriority.INFO.isAtLeast(LogPriority.DEBUG))
        assertTrue(LogPriority.INFO.isAtLeast(LogPriority.INFO))
        assertFalse(LogPriority.INFO.isAtLeast(LogPriority.WARNING))
        assertFalse(LogPriority.INFO.isAtLeast(LogPriority.ERROR))

        assertTrue(LogPriority.DEBUG.isAtLeast(LogPriority.VERBOSE))
        assertTrue(LogPriority.DEBUG.isAtLeast(LogPriority.DEBUG))
        assertFalse(LogPriority.DEBUG.isAtLeast(LogPriority.INFO))
        assertFalse(LogPriority.DEBUG.isAtLeast(LogPriority.WARNING))
        assertFalse(LogPriority.DEBUG.isAtLeast(LogPriority.ERROR))

        assertTrue(LogPriority.VERBOSE.isAtLeast(LogPriority.VERBOSE))
        assertFalse(LogPriority.VERBOSE.isAtLeast(LogPriority.DEBUG))
        assertFalse(LogPriority.VERBOSE.isAtLeast(LogPriority.INFO))
        assertFalse(LogPriority.VERBOSE.isAtLeast(LogPriority.WARNING))
        assertFalse(LogPriority.VERBOSE.isAtLeast(LogPriority.ERROR))
    }

    @Test
    fun testStringRepresentation() {
        assertEquals("VERBOSE", LogPriority.VERBOSE.toString())
        assertEquals("DEBUG", LogPriority.DEBUG.toString())
        assertEquals("INFO", LogPriority.INFO.toString())
        assertEquals("WARNING", LogPriority.WARNING.toString())
        assertEquals("ERROR", LogPriority.ERROR.toString())
    }

    @Test
    fun testValueOfMethod() {
        assertEquals(LogPriority.VERBOSE, LogPriority.valueOf("VERBOSE"))
        assertEquals(LogPriority.DEBUG, LogPriority.valueOf("DEBUG"))
        assertEquals(LogPriority.INFO, LogPriority.valueOf("INFO"))
        assertEquals(LogPriority.WARNING, LogPriority.valueOf("WARNING"))
        assertEquals(LogPriority.ERROR, LogPriority.valueOf("ERROR"))
    }

    @Test
    fun testPriorityFilteringScenarios() {
        val allPriorities = LogPriority.entries

        val infoAndAbove = allPriorities.filter { it >= LogPriority.INFO }
        assertEquals(
            listOf(LogPriority.INFO, LogPriority.WARNING, LogPriority.ERROR),
            infoAndAbove
        )

        val warningAndAbove = allPriorities.filter { it >= LogPriority.WARNING }
        assertEquals(
            listOf(LogPriority.WARNING, LogPriority.ERROR),
            warningAndAbove
        )

        val errorOnly = allPriorities.filter { it >= LogPriority.ERROR }
        assertEquals(
            listOf(LogPriority.ERROR),
            errorOnly
        )

        val debugAndBelow = allPriorities.filter { it <= LogPriority.DEBUG }
        assertEquals(
            listOf(LogPriority.VERBOSE, LogPriority.DEBUG),
            debugAndBelow
        )
    }

    @Test
    fun testHashCodeConsistency() {
        assertEquals(LogPriority.VERBOSE.hashCode(), LogPriority.VERBOSE.hashCode())
        assertEquals(LogPriority.DEBUG.hashCode(), LogPriority.DEBUG.hashCode())
        assertEquals(LogPriority.INFO.hashCode(), LogPriority.INFO.hashCode())
        assertEquals(LogPriority.WARNING.hashCode(), LogPriority.WARNING.hashCode())
        assertEquals(LogPriority.ERROR.hashCode(), LogPriority.ERROR.hashCode())
    }

    @Test
    fun testEqualsMethod() {
        assertTrue(LogPriority.VERBOSE == LogPriority.VERBOSE)
        assertFalse(LogPriority.VERBOSE == LogPriority.DEBUG)
        assertFalse(LogPriority.VERBOSE.equals(null))
        assertFalse(LogPriority.VERBOSE.equals("VERBOSE"))
    }
} 