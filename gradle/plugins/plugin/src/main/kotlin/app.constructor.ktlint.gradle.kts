import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

configure<KtlintExtension> {
    version.set(libs.versions.ktlintCore.get())
    android.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    baseline.set(file("${rootProject.projectDir}/.ktlint-baseline.xml"))
    // Workaround for AGP 9 built-in Kotlin integration issues with ktlint task wiring:
    // - https://github.com/JLLeitschuh/ktlint-gradle/issues/1016
    // - https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/
    // Keep explicit src/**/*.kt include until the plugin behavior is fully stable for AGP 9.
    // TODO: The fix is merged into ktlint repo, but isn't released yet.
    //  Update ktlint and remove this workaround once ktlint team releases the fix.
    kotlinScriptAdditionalPaths {
        include(fileTree("src") {
            include("**/*.kt")
        })
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// Custom ktlint rules for FeatureSpecification.md enforcement.
// The JAR is built from the standalone gradle/ktlint-rules project.
// Both android-app and gears register it as an included build so that
// the JAR location is discoverable via gradle.includedBuilds.
val ktlintRulesBuild = gradle.includedBuilds.firstOrNull { it.name == "ktlint-rules" }

if (ktlintRulesBuild != null) {
    val ktlintRulesJar = ktlintRulesBuild.projectDir.resolve("build/libs/ktlint-rules-1.0.0.jar")
    dependencies {
        "ktlintRuleset"(files(ktlintRulesJar))
    }

    // Ensure the rules JAR is built (and rebuilt when sources change) before any ktlint task runs.
    // Must match both lifecycle tasks (ktlint*) and worker tasks (runKtlint*) for --parallel.
    val buildRulesJar = ktlintRulesBuild.task(":jar")
    tasks.matching { it.name.startsWith("ktlint") || it.name.startsWith("runKtlint") }.configureEach {
        dependsOn(buildRulesJar)
    }
}
