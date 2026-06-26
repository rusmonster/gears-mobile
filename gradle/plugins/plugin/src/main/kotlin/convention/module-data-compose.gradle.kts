package convention

import libs

plugins {
    id("convention.module-data")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}

compose.resources {
    publicResClass = true
}

kotlin {
    android {
        androidResources.enable = true
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.resources)
                implementation(libs.compose.runtime)
            }
        }
    }
}
