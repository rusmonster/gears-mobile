plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.common.annotations"
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        androidMain {
            dependencies {
                api(libs.compose.runtime)
            }
        }
    }
}
