plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.testutils"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common.resources)
            }
        }

        val jvmCommon by creating {
            dependsOn(commonMain.get())
        }

        getByName("jvmMain").dependsOn(jvmCommon)
        getByName("androidMain").dependsOn(jvmCommon)

        val androidMain by getting {
            dependencies {
                implementation(projects.common.di.appModule)
                compileOnly(libs.androidx.runner)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kmp.zip)
            }
        }
    }
}
