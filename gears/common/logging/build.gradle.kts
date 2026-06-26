plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.logging"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common.files)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.common.di.appModule)
            }
        }
    }
}
