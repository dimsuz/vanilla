package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
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
    .addProperties(createBuilderProperties(mapping))
    .addFunctions(createBuilderRuleFunctions(mapping))
    .addFunctions(createBuildFunction(mapping))
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
      .addStatement("%N.remove(%S)", MISSING_RULES_PROPERTY_NAME, sourceProp.name)
      .addStatement("return this")
      .build()
  }
}

private fun createCheckMissingRulesFunction(): FunSpec {
  return FunSpec.builder("checkMissingRules")
    .addModifiers(KModifier.PRIVATE)
    .beginControlFlow("if (%N.isNotEmpty())", MISSING_RULES_PROPERTY_NAME)
    .addStatement("val fieldNames = %N.joinToString { %P }", MISSING_RULES_PROPERTY_NAME, "\"\$it\"")
    .addStatement("error(%P)", "missing validation rules for properties: \$fieldNames")
    .endControlFlow()
    .build()
}

private fun createBuildFunction(mapping: PropertyMapping): List<FunSpec> {
  val resultValidatorTypeName = ClassName("", createValidatorClassName(mapping))
  val checkMissingRulesFunction = createCheckMissingRulesFunction()
  return listOf(
    checkMissingRulesFunction,
    FunSpec.builder("build")
      .returns(resultValidatorTypeName.parameterizedBy(TypeVariableName("E")))
      .addStatement("%N()", checkMissingRulesFunction)
      .addStatement("return %T()", resultValidatorTypeName)
      .build()
  )
}

private fun createValidatorSuperClassName(mapping: PropertyMapping): ParameterizedTypeName {
  return ClassName("ru.dimsuz.vanilla", "Validator")
    .parameterizedBy(
      mapping.models.sourceKmClass.toClassName(),
      mapping.models.targetKmClass.toClassName(),
      TypeVariableName("E")
    )
}

private fun createBuilderProperties(mapping: PropertyMapping): List<PropertySpec> {
  return listOf(
    createMissingFieldRulesProperty(mapping.mapping.keys)
  )
}

private fun createMissingFieldRulesProperty(sourceProperties: Set<ImmutableKmProperty>): PropertySpec {
  val type = ClassName("kotlin.collections", "MutableList").parameterizedBy(String::class.asTypeName())
  return PropertySpec.builder(MISSING_RULES_PROPERTY_NAME, type, KModifier.PRIVATE)
    .initializer(
      "mutableListOf(" + generateSequence { "%S" }.take(sourceProperties.size).joinToString(",") + ")",
      *sourceProperties.map { it.name }.toTypedArray()
    )
    .build()
}

private const val MISSING_RULES_PROPERTY_NAME = "missingFieldRules"
