import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.skie)
    id("convention.gitlab-publishing")
}

kotlin {
    android {
        namespace = "app.constructor.gears"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        buildToolsVersion = libs.versions.android.buildTools.get()
    }

    val xcf = XCFramework("Gears")

    val xcfTarget: String? = project.findProperty("xcfTarget")?.toString()

    val iosTargets = when (xcfTarget) {
        "iosArm64"          -> listOf(iosArm64())
        "iosSimulatorArm64" -> listOf(iosSimulatorArm64())
        null                -> listOf(iosArm64(), iosSimulatorArm64())
        else                -> error("Invalid xcfTarget='$xcfTarget'. Must be 'iosArm64' or 'iosSimulatorArm64' or null")
    }

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Gears"
            isStatic = false
            binaryOption("bundleId", "constructor.app.ios.sdk")
            export(projects.feature.problemReport.presentation.api)

            xcf.add(this)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.feature.problemReport)

                implementation(projects.common.di.appModule)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
