package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.enclosingPackageName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, analysisResult: SourceAnalysisResult): Either<Error, Unit> {
  val validatorClassName = createValidatorClassName(analysisResult)
  val fileSpec = FileSpec.builder(analysisResult.models.sourceElement.enclosingPackageName, validatorClassName)
    .addType(createValidatorTypeSpec(validatorClassName, analysisResult))
    .build()
  return writeFile(processingEnv, fileSpec)
}

private fun createValidatorTypeSpec(
  validatorClassName: String,
  analysisResult: SourceAnalysisResult
): TypeSpec {
  return TypeSpec
    .classBuilder(validatorClassName)
    .addTypeVariable(TypeVariableName("E"))
    .addSuperinterface(createValidatorSuperClassName(analysisResult))
    .addValidatorPrimaryConstructor(analysisResult)
    .addFunctions(createValidateFunctions(analysisResult))
    .addType(createBuilderTypeSpec(analysisResult))
    .build()
}

fun TypeSpec.Builder.addValidatorPrimaryConstructor(analysisResult: SourceAnalysisResult): TypeSpec.Builder {
  val parameters = analysisResult.mapping.map { (sourcePropName, targetPropName) ->
    val sourcePropType = analysisResult.models.sourceTypeSpec.propertySpecs.first { it.name == sourcePropName }.type
    val targetPropType = analysisResult.models.targetTypeSpec.propertySpecs.first { it.name == targetPropName }.type
    val typeName = VALIDATOR_CLASS_NAME.parameterizedBy(sourcePropType, targetPropType, TypeVariableName("E"))
    ParameterSpec(createRuleValidatorPropertyName(sourcePropName), typeName)
  }
  return this
    .primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).addParameters(parameters).build())
    .addProperties(
      parameters.map {
        PropertySpec.builder(it.name, it.type, KModifier.PRIVATE).initializer(it.name).build()
      }
    )
}

private fun createValidatorClassName(result: SourceAnalysisResult): String {
  return "${result.models.sourceTypeSpec.name}Validator"
}

private fun createValidateFunctions(analysisResult: SourceAnalysisResult): List<FunSpec> {
  val sourceClassName = extractSourceClassName(analysisResult)
  val targetClassName = extractTargetClassName(analysisResult)
  val errorsVariableName = "errors"
  val validationStatements = createValidationExecStatements(analysisResult, errorsVariableName)
  val invokeFunction = FunSpec.builder("invoke")
    .addModifiers(KModifier.OVERRIDE)
    .addParameter("input", sourceClassName)
    .returns(RESULT_CLASS_NAME.parameterizedBy(targetClassName, TypeVariableName("E")))
    .addStatement("val %N = mutableListOf<E>()", errorsVariableName)
    .addCode(validationStatements.fold(CodeBlock.builder(), { builder, block -> builder.add(block) }).build())
    .beginControlFlow("return if (%N.isEmpty())", errorsVariableName)
    .addStatement(
      "%T(%T(${repeatTemplate("%N = %N!!", analysisResult.mapping.size)}))",
      Result.Ok::class.asTypeName(),
      targetClassName,
      *analysisResult.mapping.flatMap { (_, tProp) -> listOf(tProp, tProp) }.toTypedArray()
    )
    .endControlFlow()
    .beginControlFlow("else")
    .addStatement(
      "%1T(%2N.first(), if (%2N.size > 1) %2N.drop(1) else null)",
      Result.Error::class.asTypeName(),
      errorsVariableName
    )
    .endControlFlow()
    .build()
  val validateFunction = FunSpec.builder("validate")
    .addParameter("input", sourceClassName)
    .returns(RESULT_CLASS_NAME.parameterizedBy(targetClassName, TypeVariableName("E")))
    .addStatement("return %N(input)", invokeFunction)
    .build()
  return listOf(invokeFunction, validateFunction)
}

@Suppress("SameParameterValue") // intentionally passing here to sync caller/callee
private fun createValidationExecStatements(
  analysisResult: SourceAnalysisResult,
  errorsVariableName: String
): List<CodeBlock> {
  return analysisResult.mapping.map { (sProp, tProp) ->
    CodeBlock.builder()
      .beginControlFlow(
        "val %N = when (val result = %N(input.%N))",
        tProp,
        createRuleValidatorPropertyName(sProp),
        sProp
      )
      .beginControlFlow("is %T ->", Result.Error::class.asTypeName())
      .addStatement("%N.add(result.first)", errorsVariableName)
      .addStatement("if (result.rest != null) %N.addAll(result.rest!!)", errorsVariableName)
      .addStatement("null")
      .endControlFlow()
      .addStatement("is %T -> result.value", Result.Ok::class.asTypeName())
      .endControlFlow()
      .build()
  }
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
  val propertyNames = analysisResult.mapping.keys.map { createRuleValidatorPropertyName(it) }
  return listOf(
    checkMissingRulesFunction,
    FunSpec.builder("build")
      .returns(resultValidatorTypeName.parameterizedBy(TypeVariableName("E")))
      .addStatement("%N()", checkMissingRulesFunction)
      .addStatement(
        "return %T(${repeatTemplate("%N!!", propertyNames.size)})",
        resultValidatorTypeName,
        *propertyNames.toTypedArray()
      )
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
      "mutableListOf(" + repeatTemplate("%S", sourceProperties.size) + ")",
      *sourceProperties.map { it }.toTypedArray()
    )
    .build()
}

/**
 * Repeats template [count] times, for example:
 *
 * ```
 * repeateTemplate("%S", 3) => "%S, %S, %S"
 * ```
 */
private fun repeatTemplate(template: String, count: Int): String {
  return generateSequence { template }.take(count).joinToString()
}

private val VALIDATOR_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Validator")
private val RESULT_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Result")
private const val MISSING_RULES_PROPERTY_NAME = "missingFieldRules"
