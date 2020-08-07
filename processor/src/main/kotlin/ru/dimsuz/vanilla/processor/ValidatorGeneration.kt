package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.packageName
import ru.dimsuz.vanilla.processor.extension.simpleName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, mapping: PropertyMapping): Either<Error, Unit> {
  val validatorClassName = "${mapping.models.source.simpleName}Validator"
  val fileSpec = FileSpec.builder(mapping.models.source.packageName, validatorClassName)
    .addType(
      TypeSpec
        .classBuilder(validatorClassName)
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
      .addParameter("validator", Int::class)
      .build()
  }
}
