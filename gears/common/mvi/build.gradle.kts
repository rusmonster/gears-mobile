plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.common.mvi"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.common.resources)

                implementation(projects.common.common)
                implementation(projects.common.logging)
            }
        }
        androidMain {
            dependencies {
                api(libs.androidx.lifecycle.viewmodel)
            }
        }
    }
}
