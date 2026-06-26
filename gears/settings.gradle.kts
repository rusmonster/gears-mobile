rootProject.name = "GearsMobile"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../gradle/plugins")
}

includeBuild("../gradle/ktlint-rules")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(":common:di:app-module")
include(":common:annotations")
include(":common:common")
include(":common:files")
include(":common:logging")
include(":common:zip")
include(":common:test-utils")
include(":common:mvi")
include(":common:resources")
include(":feature:problem-report")
include(":feature:problem-report:data")
include(":feature:problem-report:domain")
include(":feature:problem-report:presentation:api")
include(":feature:problem-report:presentation:impl")
include(":gears")
