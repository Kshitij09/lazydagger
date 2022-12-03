package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.writeTo

class LazyDaggerProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(LazyDagger::class.qualifiedName!!)
        val unableToProcess = symbols.filterNot { it.validate() }.toList()

        val targetSymbols = symbols.filter {
            it is KSClassDeclaration && it.isInterface && it.validate()
        }

        if (!targetSymbols.any()) return unableToProcess

        val moduleFileSpecBuilder = LazyModuleFileSpecBuilder()
        targetSymbols.forEach { it.accept(Visitor(moduleFileSpecBuilder), Unit) }
        moduleFileSpecBuilder.build().writeTo(codeGenerator, aggregating = false)
        return unableToProcess
    }

    private val KSClassDeclaration.isInterface: Boolean get() = classKind == ClassKind.INTERFACE

    private inner class Visitor(private val bindingContributor: BindingContributor) : KSVisitorVoid() {
        private val properties = mutableListOf<PropertySpec>()
        private val constructorParams = mutableListOf<ParameterSpec>()
        private lateinit var typeParamResolver: TypeParameterResolver
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val packageName = classDeclaration.packageName.asString()
            typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()
            val classTypeName = classDeclaration.asType(emptyList()).toTypeName()
            val interfaceName = classDeclaration.simpleName.asString()
            val className = "${interfaceName}Impl"
            classDeclaration.getAllProperties().forEach { it.accept(this, Unit) }
            val constructor = FunSpec.constructorBuilder()
                .addParameters(constructorParams)
                .addAnnotation(ClassName("javax.inject", "Inject"))
                .build()

            val originatingElementClass = ClassName("dagger.hilt.codegen", "OriginatingElement")
            val foo = AnnotationSpec.builder(originatingElementClass)
                .addMember("topLevelClass = %T::class", classTypeName)
                .build()
            val classType = TypeSpec.classBuilder(className)
                .addProperties(properties)
                .addAnnotation(foo)
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

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val typeName = property.type
                .toTypeName(typeParamResolver)
            val propertyName = property.simpleName.asString()
            val constructorParamName = propertyName + lazyPropertySuffix
            val constructorParamType = daggerLazyType.parameterizedBy(typeName)
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

    companion object {
        private val daggerLazyType = ClassName("dagger", "Lazy")
        private val hiltInstallInType = ClassName("dagger.hilt", "InstallIn")
        private val installInSingletonAnnotation = AnnotationSpec
            .builder(hiltInstallInType)
            .addMember("dagger.hilt.components.SingletonComponent::class")
            .build()
        private const val lazyPropertySuffix = "Lazy"
    }
}

class LazyDaggerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LazyDaggerProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}