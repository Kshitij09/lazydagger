plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    implementation("com.kshitijpatil.lazydagger:lazydagger-core:0.0.1")
    implementation(libs.dagger)
    implementation(libs.hilt.core)
    kapt(libs.dagger.compiler)
    ksp("com.kshitijpatil.lazydagger:lazydagger-codegen:0.0.1")
}