package com.kshitijpatil.lazydagger

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
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

        val moduleFileSpecBuilder = LazyModuleFileSpecBuilder(logger)
        targetSymbols.forEach {
            it.accept(
                visitor = LazyDaggerAnnotationVisitor(codeGenerator, moduleFileSpecBuilder),
                data = Unit
            )
        }
        moduleFileSpecBuilder.build().forEach { it.writeTo(codeGenerator, aggregating = false) }
        return unableToProcess
    }

    private val KSClassDeclaration.isInterface: Boolean get() = classKind == ClassKind.INTERFACE
}

class LazyDaggerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LazyDaggerProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}