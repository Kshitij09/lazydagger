package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.*
import org.apache.commons.lang3.StringUtils

internal class LazyModuleFileSpecBuilder(private val logger: KSPLogger) : BindingContributor {
    var className = "LazyDaggerModule"
    var implParamName = "impl"
    var bindFunctionPrefix = "bind"
    private val bindingMap = mutableMapOf<ClassName, MutableList<LazyBinding>>(
        PoetClasses.singletonComponent to mutableListOf()
    )

    override fun addBinding(binding: LazyBinding) {
        binding.components.ifEmpty { listOf(PoetClasses.singletonComponent) }
            .also { logger.logging("LazyDagger: Adding $it") }
            .forEach { component ->
                bindingMap.compute(component) { _, list ->
                    (list ?: mutableListOf()).also {
                        it.add(binding)
                    }
                }
            }
    }

    private fun buildModuleFile(component: ClassName, bindings: List<LazyBinding>): FileSpec {
        val bindingFunctions = bindings.map { funSpecOf(it) }
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
        return bindingMap.map { (component, bindings) -> buildModuleFile(component, bindings) }
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