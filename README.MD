# 🌲 Cedar Logger

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-blue?style=for-the-badge&logo=kotlin"/>
  <img src="https://img.shields.io/badge/Platform-Android%20|%20iOS%20|%20JVM%20|%20wasmJs-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/iOS-000000?style=flat-square&logo=ios&logoColor=white"/>
  <img src="https://img.shields.io/badge/JVM-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/wasmJs-654FF0?style=flat-square&logo=webassembly&logoColor=white"/>
</p>

Multiplatform Timber-like lightweight and extensible logging library for Kotlin Multiplatform projects. Plant different trees to log to various destinations with beautiful formatting and powerful filtering.

> **🚀 Familiar API**: If you've used [Timber](https://github.com/JakeWharton/timber) before, you'll feel right at home! Cedar Logger uses the same intuitive API but extends it to support Kotlin Multiplatform, making migration simple and straightforward.

## ✨ Features

- **Multiple Log Levels**: VERBOSE, DEBUG, INFO, WARNING, ERROR
- **Tagged Logging**: Organize logs by categories
- **Scoped Logging**: Automatic timing for operations
- **Tree Architecture**: Extensible output destinations
- **Thread-Safe**: Concurrent logging support
- **Multiplatform**: Android, iOS, JVM, and wasmJs support

## 🚀 Quick Start

### Installation
```kotlin
dependencies {
    implementation("io.github.chilinoodles:cedar-logging:0.2.0")
}
```

### iOS Integration Options

#### Option 1: Direct Framework Export (Recommended)
```kotlin
// In your iOS app's build.gradle.kts
listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
        export("io.github.chilinoodles:cedar-logging:0.2.0")
        baseName = "YourApp"
        isStatic = true
    }
}

// In commonMain dependencies
api("io.github.chilinoodles:cedar-logging:0.2.0")
```

#### Option 2: CocoaPods Integration
```ruby
# In your Podfile
pod 'CedarLogger', :git => 'https://github.com/ChiliNoodles/Cedar-Logger.git', :tag => 'v0.2.0'
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

### PlatformLogTree
Uses platform-specific logging APIs for optimal performance. Supports platform-specific configuration for advanced features.

#### Basic Usage
```kotlin
Cedar.plant(PlatformLogTree())
```

#### Advanced Configuration
```kotlin
Cedar.plant(PlatformLogTree().configureForPlatform {
    iosSubsystem = "com.myapp.network"
    iosCategory = "API"
    androidMaxLogLength = 2000
    jvmLoggerName = "MyApp.Logger"
    enableEmojis = true
})
```

**Platform-Specific Features:**
- **iOS**: Custom subsystems and categories for better organization in Console.app
- **Android**: Configurable max log length
- **JVM**: Custom logger names for java.util.logging integration
- **All platforms**: Configurable emoji support

> **📱 iOS/Swift Usage**: Cedar Logger can be used directly in Swift code via framework export. See [iOS/Swift Usage Guide](docs/ios-swift-usage.md) for detailed examples.

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

## 📦 Maven Central Publishing

Cedar Logger is published to Maven Central and ready for production use:

### Dependency Information
```kotlin
// Kotlin Multiplatform
implementation("io.github.chilinoodles:cedar-logging:0.2.0")

// Android only
implementation("io.github.chilinoodles:cedar-logging-android:0.2.0")

// JVM only  
implementation("io.github.chilinoodles:cedar-logging-jvm:0.2.0")
```

### Platform Support
- ✅ **Android**: AAR with ProGuard rules
- ✅ **iOS**: Static framework + CocoaPods
- ✅ **JVM**: JAR for desktop/server
- ✅ **wasmJs**: WebAssembly for browsers

### iOS Framework
- **Name**: `CedarLogger`
- **Min iOS**: 12.0
- **Type**: Static framework
- **Architectures**: arm64, x64, simulator

## 📝 License

This project is licensed under the Apache License, Version 2.0.

## 🌟 Acknowledgments

- Inspired by [Timber](https://github.com/JakeWharton/timber) for Android
- Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

---

<p align="center">
  Made with ❤️ for the Kotlin Multiplatform community
</p>
