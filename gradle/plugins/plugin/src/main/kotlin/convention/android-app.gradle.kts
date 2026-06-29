package convention

import libs

plugins {
    id("com.android.application")
    id("app.constructor.ktlint")
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.android.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        manifestPlaceholders["appAuthRedirectScheme"] = "app.constructor.mobile"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
