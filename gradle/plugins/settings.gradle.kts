rootProject.name = "plugins"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}

include(":plugin")
