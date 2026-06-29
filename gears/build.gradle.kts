plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Applying the problem-report-tools convention plugin at the root puts the shared
    // convention-plugins classpath (Compose, SKIE, mokkery, …) on the root buildscript
    // classpath, which subprojects inherit. Pin those plugins' versions here (apply false)
    // so the versioned plugin requests in subprojects still resolve.
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.mokkery) apply false
    id("app.constructor.problem-report-tools")
}

tasks.register("ktlintCheck").configure {
    subprojects.forEach { subproject ->
        subproject.tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
        subproject.tasks.findByName("checkSortDependencies")?.let { dependsOn(it) }
    }
}

tasks.register("ktlintFormat").configure {
    subprojects.forEach { subproject ->
        subproject.tasks.findByName("ktlintFormat")?.let { dependsOn(it) }
        subproject.tasks.findByName("sortDependencies")?.let { dependsOn(it) }
    }
}

tasks.register("jvmTest").configure {
    subprojects.forEach { subproject ->
        subproject.tasks.findByName("jvmTest")?.let { dependsOn(it) }
    }
}

tasks.register("detekt").configure {
    subprojects.forEach { subproject ->
        subproject.tasks.findByName("detekt")?.let { dependsOn(it) }
    }
}

tasks.register("detektBaseline").configure {
    subprojects.forEach { subproject ->
        subproject.tasks.findByName("detektBaseline")?.let { dependsOn(it) }
    }
}
