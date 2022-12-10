package com.kshitijpatil.lazydagger

import com.squareup.kotlinpoet.ClassName

internal object PoetClasses {
    @JvmField
    val daggerModule = ClassName("dagger", "Module")

    @JvmField
    val daggerBinds = ClassName("dagger", "Binds")

    @JvmField
    val hiltInstallIn = ClassName("dagger.hilt", "InstallIn")

    @JvmField
    val daggerLazy = ClassName("dagger", "Lazy")

    @JvmField
    val javaxInject = ClassName("javax.inject", "Inject")

    @JvmField
    val hiltOriginatingElement = ClassName("dagger.hilt.codegen", "OriginatingElement")

    @JvmField
    val singletonComponent = ClassName("dagger.hilt.components", "SingletonComponent")
}