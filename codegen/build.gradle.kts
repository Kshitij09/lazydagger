plugins {
    kotlin("jvm")
    alias(libs.plugins.mavenPublish)
}

dependencies {
    implementation(project(":runtime"))
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp)
    implementation(libs.commons.lang3)
}