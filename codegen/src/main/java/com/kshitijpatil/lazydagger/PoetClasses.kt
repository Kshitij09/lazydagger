package com.kshitijpatil.lazydagger

import com.squareup.kotlinpoet.ClassName

internal object PoetClasses {
    @JvmField
    val daggerModule = ClassName("dagger", "Module")
    @JvmField
    val daggerBinds = ClassName("dagger", "Binds")
    @JvmField
    val hiltInstallIn = ClassName("dagger.hilt", "InstallIn")
}