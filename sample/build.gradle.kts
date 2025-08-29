import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
}

kotlin {
    val rootDirPath = project.rootDir.path
    val projectDirPath = project.projectDir.path

    jvmToolchain(libs.versions.javaVersion.get().toInt())

    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            export(project(":cedar-logging"))
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js(IR) {
        outputModuleName.set("composeApp-js")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp-js.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            api(project(":cedar-logging"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "io.github.chilinoodles.sample"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        targetSdk = 36

        applicationId = "io.github.chilinoodles.sample"
        versionCode = 1
        versionName = "1.0.0"
    }
}

compose.desktop {
    application {
        mainClass = "io.github.chilinoodles.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.chilinoodles.sample"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runJvm") {
    group = "application"
    description = "Runs the JVM MainKt"
    mainClass.set("io.github.chilinoodles.sample.MainKt")
    classpath = kotlin.targets
        .getByName("jvm")
        .compilations
        .getByName("main")
        .runtimeDependencyFiles!!
}