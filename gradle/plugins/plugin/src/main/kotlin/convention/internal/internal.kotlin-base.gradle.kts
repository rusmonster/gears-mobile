import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("app.constructor.ktlint")
    id("app.constructor.detekt")
    id("com.squareup.sort-dependencies")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks.withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
