import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    id("io.gitlab.arturbosch.detekt")
}

// Resolve the shared detekt config from the android-app project directory regardless
// of whether this plugin is applied inside the android-app build or the gears
// included build.
val sharedConfig = file("${rootProject.projectDir}/../config/detekt/detekt.yml")
    .takeIf { it.exists() }
    ?: file("${rootProject.projectDir}/config/detekt/detekt.yml")

configure<DetektExtension> {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = false
    allRules = false
    config.setFrom(files(sharedConfig))
    baseline = file("${projectDir}/detekt-baseline.xml")
    parallel = true
    autoCorrect = false
    ignoreFailures = false
}

tasks.withType<Detekt>().configureEach {
    // Type resolution is required for UnusedPrivateClass / UnusedPrivateMember to see
    // cross-file references reliably. Detekt picks up the JVM classpath automatically
    // for Android + KMP source sets when jvmTarget is set.
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
        md.required.set(false)
    }
    exclude("**/build/**")
    exclude("**/generated/**")
}

// The detekt-gradle plugin generates one Detekt task per KMP source set
// (detektAndroidMain, detektJvmMain, detektMetadataCommonMain, detektIosArm64Main, ...).
// The umbrella `detekt` task it creates does NOT depend on them, so per-module aggregation
// reports NO-SOURCE. Wire every per-source-set task into the umbrella task here so
// `./gradlew detekt` actually runs analysis on KMP modules.
afterEvaluate {
    val umbrella = tasks.findByName("detekt") ?: return@afterEvaluate
    tasks.withType<Detekt>().forEach { task ->
        if (task != umbrella) {
            umbrella.dependsOn(task)
        }
    }
}
