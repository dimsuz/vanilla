package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.packageName
import ru.dimsuz.vanilla.processor.extension.simpleName
import ru.dimsuz.vanilla.processor.extension.toClassName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, mapping: PropertyMapping): Either<Error, Unit> {
  val validatorClassName = createValidatorClassName(mapping)
  val fileSpec = FileSpec.builder(mapping.models.sourceKmClass.packageName, validatorClassName)
    .addType(
      TypeSpec
        .classBuilder(validatorClassName)
        .addTypeVariable(TypeVariableName("E"))
        .addSuperinterface(createValidatorSuperClassName(mapping))
        .addFunctions(createValidateFunctions(mapping))
        .addType(createBuilderTypeSpec(mapping))
        .build()
    )
    .build()
  return writeFile(processingEnv, fileSpec)
}

private fun createValidatorClassName(mapping: PropertyMapping): String {
  return "${mapping.models.sourceKmClass.simpleName}Validator"
}

private fun createValidateFunctions(mapping: PropertyMapping): List<FunSpec> {
  val invokeFunction = FunSpec.builder("invoke")
    .addModifiers(KModifier.OVERRIDE)
    .addParameter("input", mapping.models.sourceKmClass.toClassName())
    .returns(
      ClassName("ru.dimsuz.vanilla", "Result").parameterizedBy(
        mapping.models.targetKmClass.toClassName(),
        TypeVariableName("E")
      )
    )
    .addCode("TODO()")
    .build()
  val validateFunction = FunSpec.builder("validate")
    .addParameter("input", mapping.models.sourceKmClass.toClassName())
    .returns(
      ClassName("ru.dimsuz.vanilla", "Result").parameterizedBy(
        mapping.models.targetKmClass.toClassName(),
        TypeVariableName("E")
      )
    )
    .addStatement("return %N(input)", invokeFunction)
    .build()
  return listOf(invokeFunction, validateFunction)
}

private fun createBuilderTypeSpec(mapping: PropertyMapping): TypeSpec {
  return TypeSpec.classBuilder("Builder")
    .addTypeVariable(TypeVariableName("E"))
    .addFunctions(createBuilderRuleFunctions(mapping))
    .addFunction(createBuildFunction(mapping))
    .build()
}

private fun createBuilderRuleFunctions(mapping: PropertyMapping): Iterable<FunSpec> {
  val sourceTypeSpec = mapping.models.sourceKmClass.toTypeSpec(null)
  val targetTypeSpec = mapping.models.targetKmClass.toTypeSpec(null)
  return mapping.mapping.map { (sourceProp, targetProp) ->
    val sourcePropType = sourceTypeSpec.propertySpecs.first { it.name == sourceProp.name }.type
    val targetPropType = targetTypeSpec.propertySpecs.first { it.name == targetProp.name }.type
    val propValidatorType = ClassName("ru.dimsuz.vanilla", "Validator")
      .parameterizedBy(
        sourcePropType,
        targetPropType
      )
      .plusParameter(TypeVariableName("E"))
    FunSpec.builder(sourceProp.name)
      .addParameter("validator", propValidatorType)
      .returns(ClassName("", "Builder").parameterizedBy(TypeVariableName("E")))
      .addCode("TODO()")
      .build()
  }
}

private fun createBuildFunction(mapping: PropertyMapping): FunSpec {
  val resultValidatorType = ClassName("", createValidatorClassName(mapping))
    .parameterizedBy(TypeVariableName("E"))
  return FunSpec.builder("build")
    .returns(resultValidatorType)
    .addCode("TODO()")
    .build()
}

private fun createValidatorSuperClassName(mapping: PropertyMapping): ParameterizedTypeName {
  return ClassName("ru.dimsuz.vanilla", "Validator")
    .parameterizedBy(
      mapping.models.sourceKmClass.toClassName(),
      mapping.models.targetKmClass.toClassName(),
      TypeVariableName("E")
    )
}
