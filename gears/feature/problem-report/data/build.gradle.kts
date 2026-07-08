plugins {
    id("convention.module-data-compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "app.constructor.csdk.problemreport.data"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.cryptography.kotlin)
                implementation(libs.cryptography.kotlin.optimal)
                implementation(libs.kotlinx.io.core)
                implementation(projects.common.common)
                implementation(projects.common.files)
                implementation(projects.common.logging)
                implementation(projects.common.zip)
                implementation(projects.feature.problemReport.domain)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.common.testUtils)
            }
        }
    }
}
