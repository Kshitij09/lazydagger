import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradlePlugin)
    }
}

plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.S01, true)
            signAllPublications()
        }
    }
}