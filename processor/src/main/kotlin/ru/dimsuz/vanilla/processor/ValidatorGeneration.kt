package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, mapping: PropertyMapping): Either<Error, Unit> {
  val validatorClassName = "${mapping.models.source.name}Validator"
  val fileSpec = FileSpec.builder(mapping.models.sourcePackage, "${validatorClassName}.kt")
    .addType(
      TypeSpec
        .classBuilder(ClassName(mapping.models.sourcePackage, validatorClassName))
        .addType(createBuilderTypeSpec(mapping))
        .build()
    )
    .build()
  return writeFile(processingEnv, fileSpec)
}

private fun createBuilderTypeSpec(mapping: PropertyMapping): TypeSpec {
  return TypeSpec.classBuilder("Builder")
    .addFunctions(createBuilderRuleFunctions(mapping))
    .build()
}

private fun createBuilderRuleFunctions(mapping: PropertyMapping): Iterable<FunSpec> {
  return mapping.mapping.keys.map { property ->
    FunSpec.builder(property.name)
      .build()
  }
}
