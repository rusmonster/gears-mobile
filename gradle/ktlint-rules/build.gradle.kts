plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "app.constructor.ktlint"
version = "1.0.0"

dependencies {
    compileOnly("com.pinterest.ktlint:ktlint-cli-ruleset-core:${libs.versions.ktlintCore.get()}")
    compileOnly("com.pinterest.ktlint:ktlint-rule-engine-core:${libs.versions.ktlintCore.get()}")

    testImplementation("com.pinterest.ktlint:ktlint-cli-ruleset-core:${libs.versions.ktlintCore.get()}")
    testImplementation("com.pinterest.ktlint:ktlint-rule-engine-core:${libs.versions.ktlintCore.get()}")
    testImplementation("com.pinterest.ktlint:ktlint-test:${libs.versions.ktlintCore.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.12")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}