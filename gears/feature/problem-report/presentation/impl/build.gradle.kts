plugins {
    id("convention.module-presentation-impl")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.problemreport.presentation.impl"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.feature.problemReport.domain)
                implementation(projects.feature.problemReport.presentation.api)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.turbine)
            }
        }
    }
}
