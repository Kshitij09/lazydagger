plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":runtime"))
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    ksp(project(":codegen"))
}