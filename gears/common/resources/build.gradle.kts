plugins {
    id("convention.module-presentation-impl")
}

compose.resources {
    publicResClass = true
}

kotlin {
    android {
        namespace = "app.constructor.csdk.resources"
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.compose.resources)
            }
        }
    }
}
