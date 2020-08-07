package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.packageName
import ru.dimsuz.vanilla.processor.extension.simpleName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, mapping: PropertyMapping): Either<Error, Unit> {
  val validatorClassName = "${mapping.models.sourceKmClass.simpleName}Validator"
  val fileSpec = FileSpec.builder(mapping.models.sourceKmClass.packageName, validatorClassName)
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
    .addTypeVariable(TypeVariableName("E"))
    .addFunctions(createBuilderRuleFunctions(mapping))
    .build()
}

private fun createBuilderRuleFunctions(mapping: PropertyMapping): Iterable<FunSpec> {
  return mapping.mapping.keys.map { property ->
    val propValidatorType = Validator::class.asClassName()
      .parameterizedBy(
        ClassName(mapping.models.sourceKmClass.packageName, mapping.models.sourceKmClass.simpleName),
        ClassName(mapping.models.targetKmClass.packageName, mapping.models.targetKmClass.simpleName)
      )
      .plusParameter(TypeVariableName("E"))
    FunSpec.builder(property.name)
      .addParameter("validator", propValidatorType)
      .build()
  }
}
