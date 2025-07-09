package com.chilinoodles.cedar.logging

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.properties.Delegates

/**
 * A lightweight, extensible logging system for Kotlin Multiplatform projects.
 */
class Cedar private constructor() {

    companion object Forest {
        @OptIn(InternalCoroutinesApi::class)
        private val treeLock = SynchronizedObject()
        private val logTrees = mutableListOf<LogTree>()
        private var treeArray: Array<LogTree> by Delegates.observable(emptyArray()) { _, _, _ ->
            hasPlantedTrees = treeArray.isNotEmpty()
        }
        private var hasPlantedTrees = false

        fun tag(tag: String): TaggedLogger = TaggedLogger(tag)

        fun getLogger(tag: String? = null): TaggedLogger {
            return TaggedLogger(tag ?: "AppLogger")
        }

        @OptIn(InternalCoroutinesApi::class)
        fun plant(tree: LogTree) {
            tree.setup()
            synchronized(treeLock) {
                logTrees.add(tree)
                treeArray = logTrees.toTypedArray()
            }
        }

        @OptIn(InternalCoroutinesApi::class)
        fun plant(vararg trees: LogTree) {
            for (tree in trees) {
                tree.setup()
            }

            synchronized(treeLock) {
                logTrees.addAll(trees)
                treeArray = logTrees.toTypedArray()
            }
        }

        /**
         * Remove a previously planted tree
         */
        @OptIn(InternalCoroutinesApi::class)
        fun uproot(tree: LogTree) {
            synchronized(treeLock) {
                if (logTrees.remove(tree)) {
                    tree.tearDown()
                    treeArray = logTrees.toTypedArray()
                }
            }
        }

        @OptIn(InternalCoroutinesApi::class)
        fun clearForest() {
            synchronized(treeLock) {
                logTrees.forEach { it.tearDown() }
                logTrees.clear()
                treeArray = emptyArray()
            }
        }

        @OptIn(InternalCoroutinesApi::class)
        fun forest(): List<LogTree> {
            synchronized(treeLock) {
                return logTrees.toList()
            }
        }

        val treeCount: Int
            get() = treeArray.size

        fun v(message: String, throwable: Throwable? = null) {
            getLogger().v(message, throwable)
        }

        fun d(message: String, throwable: Throwable? = null) {
            getLogger().d(message, throwable)
        }

        fun i(message: String, throwable: Throwable? = null) {
            getLogger().i(message, throwable)
        }

        fun w(message: String, throwable: Throwable? = null) {
            getLogger().w(throwable, message)
        }

        fun w(throwable: Throwable? = null, message: String = "") {
            getLogger().w(throwable, message)
        }

        fun e(throwable: Throwable? = null, message: String = "") {
            getLogger().e(throwable, message)
        }

        fun e(message: String, throwable: Throwable? = null) {
            getLogger().e(throwable, message)
        }

        internal fun logToAllTrees(
            priority: LogPriority,
            tag: String,
            message: String,
            throwable: Throwable? = null
        ) {
            val trees = treeArray

            for (tree in trees) {
                if (tree.isLoggable(tag, priority)) {
                    tree.log(priority, tag, message, throwable)
                }
            }
        }
    }
}