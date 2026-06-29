import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.mokkery)
    id("internal.kotlin-base")
    id("convention.gitlab-publishing")
}

configure<KotlinMultiplatformExtension> {
    applyDefaultHierarchyTemplate()

    jvm()
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "app.constructor.csdk"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        buildToolsVersion = libs.versions.android.buildTools.get()
        withDeviceTestBuilder {
            sourceSetTreeName = KotlinSourceSetTree.test.name
        }.configure {
            instrumentationRunner = "app.constructor.csdk.testutils.CSDKTestRunner"
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":common:test-utils"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }

        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.runner)
            }
        }
    }
}
