package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

fun interface BindingContributor {
    fun addBinding(implClassName: ClassName, bindingDeclaration: KSClassDeclaration)
}