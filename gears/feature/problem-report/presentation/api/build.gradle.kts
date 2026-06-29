plugins {
    id("convention.module-presentation-api")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.problemreport.presentation.api"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.feature.problemReport.domain)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.compose.runtime)
            }
        }
    }
}
