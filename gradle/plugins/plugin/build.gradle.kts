plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle.plugin.ktlint)
    implementation(libs.gradle.plugin.detekt)
    implementation(libs.gradle.plugin.sort.dependencies)
    implementation(libs.compose.gradle)
    implementation(libs.compose.compiler.gradle)
    implementation(libs.mokkery.gradle.plugin)
}
