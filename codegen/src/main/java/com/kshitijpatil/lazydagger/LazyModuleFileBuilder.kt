package com.kshitijpatil.lazydagger

import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.StringUtils

internal class LazyModuleFileBuilder : BindingContributor {
    private val bindingMap = mutableMapOf<ClassName, MutableList<LazyBinding>>(
        PoetClasses.singletonComponent to mutableListOf()
    )

    override fun addBinding(binding: LazyBinding) {
        binding.components.ifEmpty { listOf(PoetClasses.singletonComponent) }
            .forEach { component ->
                bindingMap.compute(component) { _, list ->
                    (list ?: mutableListOf()).also {
                        it.add(binding)
                    }
                }
            }
    }

    private fun moduleFileFor(component: ClassName, bindings: List<LazyBinding>): FileSpec {
        val bindingFunctions = bindings.map { bindingFunctionFor(it) }
        val packageNames = bindings.map { it.packageName }
        val moduleName = "${component.simpleName}_$className"
        val module = TypeSpec.classBuilder(moduleName)
            .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
            .addAnnotation(PoetClasses.daggerModule)
            .addAnnotation(
                AnnotationSpec.builder(PoetClasses.hiltInstallIn)
                    .addMember("${component.canonicalName}::class")
                    .build()
            )
            .addFunctions(bindingFunctions)
            .build()
        val packageName = StringUtils.getCommonPrefix(*packageNames.toTypedArray())
        return FileSpec.builder(packageName, moduleName)
            .addType(module)
            .build()
    }

    fun build(): List<FileSpec> {
        return bindingMap.map { (component, bindings) ->
            moduleFileFor(component, bindings)
        }
    }

    private fun bindingFunctionFor(binding: LazyBinding): FunSpec {
        val packageNamePart = binding.packageName.replace(".", "_")
        return FunSpec.builder("${bindFunctionPrefix}_${packageNamePart}_${binding.typeName}")
            .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
            .addParameter(implParamName, ClassName(binding.packageName, binding.implClassName))
            .addAnnotation(PoetClasses.daggerBinds)
            .returns(ClassName(binding.packageName, binding.typeName))
            .clearBody()
            .build()
    }


    companion object {
        private const val className = "LazyDaggerModule"
        private const val implParamName = "impl"
        private const val bindFunctionPrefix = "bind"
    }
}