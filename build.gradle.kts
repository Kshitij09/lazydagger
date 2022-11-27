buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradlePlugin)
    }
}

subprojects {
    repositories { mavenCentral() }
}