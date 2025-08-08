package io.github.chilinoodles.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.chilinoodles.cedar.logging.Cedar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.Foundation.NSUncaughtExceptionHandler
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    setUnhandledExceptionHook { exception ->
        Cedar.tag("MainViewController").e(
            "Unhandled exception iOS Kotlin",
            exception
        )
        terminateWithUnhandledException(exception)
    }
    handleNSUncaughtException()

    return ComposeUIViewController {
        App()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun handleNSUncaughtException() {
    val handler: CPointer<NSUncaughtExceptionHandler> = staticCFunction { nsException ->
        val cause = Throwable(nsException?.reason)
        val throwable = Throwable(message = nsException?.name, cause)
        Cedar.tag("MainViewController").e(
            "Unhandled exception in iOS main thread",
            throwable
        )
    }
    NSSetUncaughtExceptionHandler(handler)
}
