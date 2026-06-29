package convention

import libs

plugins {
    id("internal.kotlin-multiplatform-base")
}

kotlin {
    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.hilt.android)
            }
        }
    }
}
