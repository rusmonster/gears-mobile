package convention

plugins {
    id("internal.kotlin-multiplatform-base")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":common:mvi"))
                api(project(":common:annotations"))
            }
        }
    }
}
