plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":runtime"))
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp)
}