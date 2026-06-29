plugins {
    id("convention.module-feature")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.problemreport"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.feature.problemReport.presentation.api)

                implementation(projects.common.files)
                implementation(projects.common.zip)
                implementation(projects.feature.problemReport.data)
                implementation(projects.feature.problemReport.domain)
                implementation(projects.feature.problemReport.presentation.impl)
            }
        }
    }
}
