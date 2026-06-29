plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.files"
    }

    sourceSets {
        val jvmCommon by creating {
            dependsOn(commonMain.get())
        }

        val jvmMain by getting {
            dependsOn(jvmCommon)
        }

        val androidMain by getting {
            dependsOn(jvmCommon)
            dependencies {
                implementation(projects.common.di.appModule)
            }
        }
    }
}
