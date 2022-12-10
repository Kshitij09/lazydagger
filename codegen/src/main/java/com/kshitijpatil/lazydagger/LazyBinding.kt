package com.kshitijpatil.lazydagger

import com.squareup.kotlinpoet.ClassName

internal class LazyBinding(
    val packageName: String,
    val typeName: String,
    val implClassName: String,
    val components: List<ClassName> = emptyList()
)