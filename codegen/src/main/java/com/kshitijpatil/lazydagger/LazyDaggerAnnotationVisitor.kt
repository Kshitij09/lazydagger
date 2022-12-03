package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.writeTo

internal class LazyDaggerAnnotationVisitor(
    private val codeGenerator: CodeGenerator,
    private val bindingContributor: BindingContributor
) : KSVisitorVoid() {
    private val properties = mutableListOf<PropertySpec>()
    private val constructorParams = mutableListOf<ParameterSpec>()
    private lateinit var typeParamResolver: TypeParameterResolver

    // TODO: Move to environment
    private val lazyPropertySuffix = "Lazy"
    private val implClassNameSuffix = "Impl"
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()
        val classTypeName = classDeclaration.asType(emptyList()).toTypeName()
        val interfaceName = classDeclaration.simpleName.asString()
        val className = interfaceName + implClassNameSuffix
        classDeclaration.getAllProperties().forEach { it.accept(this, Unit) }
        val constructor = FunSpec.constructorBuilder()
            .addParameters(constructorParams)
            .addAnnotation(PoetClasses.javaxInject)
            .build()

        val originatingElement = AnnotationSpec.builder(PoetClasses.hiltOriginatingElement)
            .addMember("topLevelClass = %T::class", classTypeName)
            .build()
        val classType = TypeSpec.classBuilder(className)
            .addProperties(properties)
            .addAnnotation(originatingElement)
            .addSuperinterface(classTypeName)
            .primaryConstructor(constructor)
            .build()

        bindingContributor.addBinding(ClassName(packageName, className), classDeclaration)

        val fileSpec = FileSpec.builder(packageName, className)
            .addType(classType)
            .build()

        fileSpec.writeTo(
            codeGenerator,
            aggregating = false,
            originatingKSFiles = listOf(classDeclaration.containingFile!!)
        )
    }

    private fun constructorProperty(name: String, type: TypeName): PropertySpec {
        return PropertySpec
            .builder(name, type)
            .initializer(name)
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private fun lazyClassProperty(
        name: String,
        type: TypeName,
        initializerProperty: PropertySpec
    ): PropertySpec {
        return PropertySpec
            .builder(name, type)
            .delegate("lazy { %N.get() }", initializerProperty)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    }

    private fun paramSpecOf(name: String, type: TypeName): ParameterSpec {
        return ParameterSpec
            .builder(name, type)
            .build()
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val typeName = property.type
            .toTypeName(typeParamResolver)
        val propertyName = property.simpleName.asString()
        val constructorParamName = propertyName + lazyPropertySuffix
        val constructorParamType = PoetClasses.daggerLazy.parameterizedBy(typeName)
        val constructorParam = paramSpecOf(constructorParamName, constructorParamType)
        constructorParams += constructorParam
        // property with same initializer name is required to merge
        // constructor param & property
        val constructorProperty = constructorProperty(constructorParamName, constructorParamType)
        properties += constructorProperty
        val classProperty = lazyClassProperty(propertyName, typeName, constructorProperty)
        properties += classProperty
    }
}