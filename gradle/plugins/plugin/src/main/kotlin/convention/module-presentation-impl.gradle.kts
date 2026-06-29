package convention

import libs

plugins {
    id("internal.kotlin-multiplatform-base")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
}

// Make generated Res class public so Android app modules can access compose resources
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
                implementation(project(":common:logging"))

                implementation(libs.compose.resources)
                implementation(libs.compose.runtime)
            }
        }
        jvmTest {
            dependencies {
                dependencies {
                    // to access strings from jvm tests on desktop
                    implementation(compose.desktop.currentOs)
                }
            }
        }
    }
}
