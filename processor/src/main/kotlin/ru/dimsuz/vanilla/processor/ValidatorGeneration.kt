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
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.enclosingPackageName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, analysisResult: SourceAnalysisResult): Either<Error, Unit> {
  val validatorClassName = createValidatorClassName(analysisResult)
  val fileSpec = FileSpec.builder(analysisResult.models.sourceElement.enclosingPackageName, validatorClassName)
    .addType(
      TypeSpec
        .classBuilder(validatorClassName)
        .addTypeVariable(TypeVariableName("E"))
        .addSuperinterface(createValidatorSuperClassName(analysisResult))
        .addFunctions(createValidateFunctions(analysisResult))
        .addType(createBuilderTypeSpec(analysisResult))
        .build()
    )
    .build()
  return writeFile(processingEnv, fileSpec)
}

private fun createValidatorClassName(result: SourceAnalysisResult): String {
  return "${result.models.sourceTypeSpec.name}Validator"
}

private fun createValidateFunctions(analysisResult: SourceAnalysisResult): List<FunSpec> {
  val sourceClassName = extractSourceClassName(analysisResult)
  val targetClassName = extractTargetClassName(analysisResult)
  val invokeFunction = FunSpec.builder("invoke")
    .addModifiers(KModifier.OVERRIDE)
    .addParameter("input", sourceClassName)
    .returns(RESULT_CLASS_NAME.parameterizedBy(targetClassName, TypeVariableName("E")))
    .addCode("TODO()")
    .build()
  val validateFunction = FunSpec.builder("validate")
    .addParameter("input", sourceClassName)
    .returns(RESULT_CLASS_NAME.parameterizedBy(targetClassName, TypeVariableName("E")))
    .addStatement("return %N(input)", invokeFunction)
    .build()
  return listOf(invokeFunction, validateFunction)
}

private fun extractTargetClassName(analysisResult: SourceAnalysisResult): ClassName {
  return ClassName(
    analysisResult.models.targetElement.enclosingPackageName, analysisResult.models.targetTypeSpec.name!!
  )
}

private fun extractSourceClassName(analysisResult: SourceAnalysisResult): ClassName {
  return ClassName(
    analysisResult.models.sourceElement.enclosingPackageName, analysisResult.models.sourceTypeSpec.name!!
  )
}

private fun createBuilderTypeSpec(analysisResult: SourceAnalysisResult): TypeSpec {
  return TypeSpec.classBuilder("Builder")
    .addTypeVariable(TypeVariableName("E"))
    .addProperties(createBuilderProperties(analysisResult))
    .addFunctions(createBuilderRuleFunctions(analysisResult))
    .addFunctions(createBuildFunction(analysisResult))
    .build()
}

private fun createBuilderRuleFunctions(analysisResult: SourceAnalysisResult): Iterable<FunSpec> {
  return analysisResult.mapping.map { (sourcePropName, targetPropName) ->
    val sourcePropType = analysisResult.models.sourceTypeSpec.propertySpecs.first { it.name == sourcePropName }.type
    val targetPropType = analysisResult.models.targetTypeSpec.propertySpecs.first { it.name == targetPropName }.type
    val propValidatorType = VALIDATOR_CLASS_NAME
      .parameterizedBy(sourcePropType, targetPropType, TypeVariableName("E"))
    FunSpec.builder(sourcePropName)
      .addParameter("validator", propValidatorType)
      .returns(ClassName("", "Builder").parameterizedBy(TypeVariableName("E")))
      .addStatement("%N.remove(%S)", MISSING_RULES_PROPERTY_NAME, sourcePropName)
      .addStatement("%N = validator", createRuleValidatorPropertyName(sourcePropName))
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

private fun createBuildFunction(analysisResult: SourceAnalysisResult): List<FunSpec> {
  val resultValidatorTypeName = ClassName("", createValidatorClassName(analysisResult))
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

private fun createValidatorSuperClassName(analysisResult: SourceAnalysisResult): ParameterizedTypeName {
  return VALIDATOR_CLASS_NAME
    .parameterizedBy(
      extractSourceClassName(analysisResult),
      extractTargetClassName(analysisResult),
      TypeVariableName("E")
    )
}

private fun createBuilderProperties(analysisResult: SourceAnalysisResult): List<PropertySpec> {
  return ArrayList<PropertySpec>(analysisResult.mapping.size + 1).apply {
    add(createMissingFieldRulesProperty(analysisResult.mapping.keys))
    addAll(createRuleValidatorProperties(analysisResult))
  }
}

private fun createRuleValidatorProperties(analysisResult: SourceAnalysisResult): Iterable<PropertySpec> {
  val sourceTypeSpec = analysisResult.models.sourceElement.toTypeSpec()
  val targetTypeSpec = analysisResult.models.targetElement.toTypeSpec()
  return analysisResult.mapping.map { (sourcePropName, targetPropName) ->
    val sourcePropType = analysisResult.models.sourceTypeSpec.propertySpecs.first { it.name == sourcePropName }.type
    val targetPropType = analysisResult.models.targetTypeSpec.propertySpecs.first { it.name == targetPropName }.type
    val typeName = VALIDATOR_CLASS_NAME
      .parameterizedBy(sourcePropType, targetPropType, TypeVariableName("E"))
      .copy(nullable = true)
    PropertySpec.builder(createRuleValidatorPropertyName(sourcePropName), typeName, KModifier.PRIVATE)
      .mutable(true)
      .initializer("null")
      .build()
  }
}

private fun createRuleValidatorPropertyName(sourcePropertyName: String) = "${sourcePropertyName}Validator"

private fun createMissingFieldRulesProperty(sourceProperties: Set<String>): PropertySpec {
  val type = ClassName("kotlin.collections", "MutableList").parameterizedBy(String::class.asTypeName())
  return PropertySpec.builder(MISSING_RULES_PROPERTY_NAME, type, KModifier.PRIVATE)
    .initializer(
      "mutableListOf(" + generateSequence { "%S" }.take(sourceProperties.size).joinToString(",") + ")",
      *sourceProperties.map { it }.toTypedArray()
    )
    .build()
}

private val VALIDATOR_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Validator")
private val RESULT_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Result")
private const val MISSING_RULES_PROPERTY_NAME = "missingFieldRules"
