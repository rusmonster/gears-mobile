package convention

import libs

plugins {
    id("com.android.library")
    id("app.constructor.ktlint")
    id("app.constructor.detekt")
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "app.constructor.mobile"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Disable androidTest tasks for modules without androidTest sources.
// This prevents ClassNotFoundException when running connectedAndroidTest
// at the root level on modules that have no test runner dependency,
// and avoids generating empty test APKs from assembleAndroidTest.
afterEvaluate {
    val androidTestDir = projectDir.resolve("src/androidTest")
    if (!androidTestDir.exists() || androidTestDir.walkTopDown().none { it.extension in listOf("kt", "java") }) {
        tasks.matching {
            (it.name.startsWith("connected") || it.name.startsWith("assemble")) &&
                it.name.contains("AndroidTest", ignoreCase = true)
        }.configureEach { enabled = false }
    }
}
