import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "gc.david.dipole.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        // Needed for Compose Multiplatform resources (the translated strings).
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.multiplatform.settings.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

dependencies {
    // Renders @Preview composables in Android Studio.
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

compose.resources {
    packageOfResClass = "gc.david.dipole.resources"
}

compose.desktop {
    application {
        mainClass = "gc.david.dipole.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Dipole"
            packageVersion = "1.0.0"
            macOS { iconFile.set(project.file("icons/icon.icns")) }
            windows { iconFile.set(project.file("icons/icon.ico")) }
            linux { iconFile.set(project.file("icons/icon.png")) }
        }
    }
}

// Renders the Google Play screenshots from the real screens, in every language, for phones and
// tablets: ./gradlew :composeApp:storeScreenshots writes them to composeApp/build/store-screenshots.
// The code is in jvmTest (screenshots/StoreScreenshots.kt) so it stays out of the app.
tasks.register<JavaExec>("storeScreenshots") {
    description = "Renders the Google Play screenshots for every language."
    val jvmTest = kotlin.jvm().compilations.getByName("test")
    dependsOn(jvmTest.compileAllTaskName)
    classpath(jvmTest.output.allOutputs, jvmTest.runtimeDependencyFiles ?: files())
    mainClass = "gc.david.dipole.screenshots.StoreScreenshotsKt"
    args(layout.buildDirectory.dir("store-screenshots").get().asFile.path)
}
