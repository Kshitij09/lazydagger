package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
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
    override fun addBinding(implClassName: ClassName, bindingDeclaration: KSClassDeclaration) {
        val packageName = bindingDeclaration.packageName.asString()
        val bindingFunc = bindingFunSpecOf(implClassName, packageName, bindingDeclaration)
        bindingFunctions.add(bindingFunc)
        packageNames.add(packageName)
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

    private fun bindingFunSpecOf(
        implClassName: ClassName,
        packageName: String,
        bindingDeclaration: KSClassDeclaration
    ): FunSpec {
        val packageNamePart = packageName.replace(".", "_")
        val interfaceName = bindingDeclaration.simpleName.asString()
        return FunSpec.builder("${bindFunctionPrefix}_${packageNamePart}_$interfaceName")
            .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
            .addParameter(implParamName, implClassName)
            .addAnnotation(PoetClasses.daggerBinds)
            .returns(bindingDeclaration.asType(emptyList()).toTypeName())
            .clearBody()
            .build()
    }


}