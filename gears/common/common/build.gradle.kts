plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.common.common"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common.logging)
            }
        }
    }
}
