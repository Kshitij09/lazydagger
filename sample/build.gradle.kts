plugins {
    kotlin("jvm")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    //implementation(project(":runtime"))
    implementation(libs.dagger)
    implementation(libs.hilt.core)
    kapt(libs.dagger.compiler)
    //ksp(project(":codegen"))
    implementation("com.kshitijpatil.lazydagger:lazydagger-core:0.0.2-SNAPSHOT")
    ksp("com.kshitijpatil.lazydagger:lazydagger-codegen:0.0.2-SNAPSHOT")
}