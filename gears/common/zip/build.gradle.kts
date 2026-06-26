plugins {
    id("convention.module-core")
}

kotlin {
    android {
        namespace = "app.constructor.csdk.zip"
    }

    sourceSets {
        val jvmCommon by creating {
            dependsOn(commonMain.get())
        }

        getByName("jvmMain").dependsOn(jvmCommon)
        getByName("androidMain").dependsOn(jvmCommon)

        commonMain {
            dependencies {
                implementation(projects.common.files)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.kmp.zip)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.common.testUtils)
            }
        }
    }
}
