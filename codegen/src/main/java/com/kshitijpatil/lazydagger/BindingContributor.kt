package com.kshitijpatil.lazydagger

internal fun interface BindingContributor {
    fun addBinding(binding: LazyBinding)
}