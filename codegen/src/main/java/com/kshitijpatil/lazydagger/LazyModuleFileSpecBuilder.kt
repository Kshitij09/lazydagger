package com.kshitijpatil.lazydagger

import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.StringUtils

internal class LazyModuleFileSpecBuilder : BindingContributor {
    private val bindingFunctions = mutableListOf<FunSpec>()
    private val packageNames = mutableListOf<String>()
    private val installInSingletonAnnotation = AnnotationSpec
        .builder(PoetClasses.hiltInstallIn)
        .addMember("dagger.hilt.components.SingletonComponent::class")
        .build()
    var className = "LazyDaggerModule"
    var implParamName = "impl"
    var bindFunctionPrefix = "bind"
    override fun addBinding(binding: LazyBinding) {
        val functionSpec = funSpecOf(binding)
        bindingFunctions.add(functionSpec)
        packageNames.add(binding.packageName)
    }

    fun build(): FileSpec {
        val module = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
            .addAnnotation(PoetClasses.daggerModule)
            .addAnnotation(installInSingletonAnnotation)
            .addFunctions(bindingFunctions)
            .build()
        val packageName = StringUtils.getCommonPrefix(*packageNames.toTypedArray())
        return FileSpec.builder(packageName, className)
            .addType(module)
            .build()
    }

    private fun funSpecOf(binding: LazyBinding): FunSpec {
        val packageNamePart = binding.packageName.replace(".", "_")
        return FunSpec.builder("${bindFunctionPrefix}_${packageNamePart}_${binding.typeName}")
            .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
            .addParameter(implParamName, ClassName(binding.packageName, binding.implClassName))
            .addAnnotation(PoetClasses.daggerBinds)
            .returns(ClassName(binding.packageName, binding.typeName))
            .clearBody()
            .build()
    }


}