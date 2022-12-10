package com.kshitijpatil.lazydagger

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class LazyDagger(vararg val components: KClass<*>)
