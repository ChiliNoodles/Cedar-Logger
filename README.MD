# 🌲 Cedar Logger

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-blue?style=for-the-badge&logo=kotlin"/>
  <img src="https://img.shields.io/badge/Platform-Android%20|%20iOS-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge"/>
</p>

Multiplatform Timber-like lightweight and extensible logging library for Kotlin Multiplatform projects. Plant different trees to log to various destinations with beautiful formatting and powerful filtering.

> **🚀 Familiar API**: If you've used [Timber](https://github.com/JakeWharton/timber) before, you'll feel right at home! Cedar Logger uses the same intuitive API but extends it to support Kotlin Multiplatform, making migration simple and straightforward.

## ✨ Features

- **Multiple Log Levels**: VERBOSE, DEBUG, INFO, WARNING, ERROR
- **Tagged Logging**: Organize logs by categories
- **Scoped Logging**: Automatic timing for operations
- **Tree Architecture**: Extensible output destinations
- **Thread-Safe**: Concurrent logging support
- **Multiplatform**: Android, iOS, and more

## 🚀 Quick Start

### Installation
```kotlin
dependencies {
    implementation("com.chilinoodles:cedar-logging:1.0.0")
}
```

### Basic Usage
```kotlin
// Plant a tree
Cedar.plant(ConsoleTree())

// Log messages
Cedar.v("Verbose message")
Cedar.d("Debug message")
Cedar.i("Info message")
Cedar.w("Warning message")
Cedar.e("Error message")

// Tagged logging
Cedar.tag("Network").i("API call successful")
Cedar.tag("Database").d("Query executed")

// Scoped logging with timing
Cedar.tag("Performance").scope("Loading data").use {
    // Your operation here - automatically logs start/end and duration
}
```

## 🌳 Built-in Trees

### ConsoleTree
Beautiful console output with icons and colors.
```kotlin
Cedar.plant(ConsoleTree().withMinPriority(LogPriority.DEBUG))
```
Output: `🐛 DEBUG   [Network] API call completed successfully`

### PlatformDebugTree
Platform-specific optimized logging (expect/actual implementation).
```kotlin
Cedar.plant(PlatformDebugTree())
```

## 🔧 Creating Custom Trees

Creating your own logging destinations is simple. Here are practical examples for popular services:

### Sentry Tree Example
```kotlin
import io.sentry.Sentry
import io.sentry.SentryLevel
import com.chilinoodles.cedar.logging.LogTree
import com.chilinoodles.cedar.logging.LogPriority

class SentryTree : LogTree {
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        val sentryLevel = when (priority) {
            LogPriority.VERBOSE, LogPriority.DEBUG -> SentryLevel.DEBUG
            LogPriority.INFO -> SentryLevel.INFO
            LogPriority.WARNING -> SentryLevel.WARNING
            LogPriority.ERROR -> SentryLevel.ERROR
        }
        
        Sentry.configureScope { scope ->
            scope.setTag("logger_tag", tag)
            scope.setLevel(sentryLevel)
        }
        
        when {
            throwable != null -> Sentry.captureException(throwable)
            priority >= LogPriority.ERROR -> Sentry.captureMessage(message, sentryLevel)
            else -> Sentry.addBreadcrumb(message)
        }
    }
}

// Usage
Cedar.plant(SentryTree())
Cedar.tag("Payment").e("Credit card processing failed", exception)
```

### Firebase Crashlytics Tree Example
```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.chilinoodles.cedar.logging.LogTree
import com.chilinoodles.cedar.logging.LogPriority

class CrashlyticsTree : LogTree {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        crashlytics.setCustomKey("last_log_tag", tag)
        crashlytics.setCustomKey("last_log_priority", priority.name)
        
        val logMessage = "[$tag] $message"
        
        when {
            throwable != null -> {
                crashlytics.log(logMessage)
                crashlytics.recordException(throwable)
            }
            priority >= LogPriority.WARNING -> {
                crashlytics.log(logMessage)
                if (priority == LogPriority.ERROR) {
                    crashlytics.recordException(RuntimeException(message))
                }
            }
            else -> crashlytics.log(logMessage)
        }
    }
}

// Usage
Cedar.plant(CrashlyticsTree())
Cedar.tag("Auth").e("Login failed", authException)
```

## 📱 Interactive Demo

The project includes a beautiful sample app demonstrating all features:

- **Tabbed Interface**: Controls, Live Logs, and Code Examples
- **Real-time Logging**: See logs appear instantly
- **Tree Management**: Test planting and removing trees
- **Material Design**: Modern, responsive UI

### Running the Demo
```bash
./gradlew :sample:run
```

## 🔧 Advanced Usage

### Multiple Trees
```kotlin
Cedar.plant(ConsoleTree())
Cedar.plant(SentryTree())
Cedar.plant(CrashlyticsTree())
// All trees receive the same logs
```

### Tree Management
```kotlin
// Check planted trees
println("Trees: ${Cedar.treeCount}")

// Remove all trees
Cedar.clearForest()

// Remove specific tree
val sentryTree = SentryTree()
Cedar.plant(sentryTree)
Cedar.uproot(sentryTree)
```

### Exception Handling
```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    Cedar.tag("CriticalError").e(e, "Operation failed")
    // Logged to all planted trees
}
```

## 📚 API Reference

| Method | Description |
|--------|-------------|
| `Cedar.plant(tree)` | Add logging tree |
| `Cedar.tag(name)` | Create tagged logger |
| `Cedar.v/d/i/w/e()` | Log messages |
| `Cedar.clearForest()` | Remove all trees |
| `Cedar.uproot(tree)` | Remove specific tree |
| `Cedar.treeCount` | Get planted tree count |


## 📝 License

This project is licensed under the Apache License, Version 2.0.

## 🌟 Acknowledgments

- Inspired by [Timber](https://github.com/JakeWharton/timber) for Android
- Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

---

<p align="center">
  Made with ❤️ for the Kotlin Multiplatform community
</p>
