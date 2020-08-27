package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName
import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.extension.enclosingPackageName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, analysisResult: SourceAnalysisResult): Either<Error, Unit> {
  val builderTypeSpec = createBuilderTypeSpec(analysisResult)
  val fileSpec = FileSpec.builder(analysisResult.models.sourceElement.enclosingPackageName, builderTypeSpec.name!!)
    .addType(builderTypeSpec)
    .build()
  return writeFile(processingEnv, fileSpec)
}

@Suppress("SameParameterValue") // intentionally passing here to sync caller/callee
private fun createValidationExecStatements(
  analysisResult: SourceAnalysisResult,
  errorsVariableName: String
): List<CodeBlock> {
  return analysisResult.mapping.map { (sProp, tProp) ->
    CodeBlock.builder()
      .beginControlFlow(
        "val %N = when (val result = %N.validate(input.%N))",
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
  val className = ClassName("", "${analysisResult.models.sourceTypeSpec.name}ValidatorBuilder")
  return TypeSpec.classBuilder(className)
    .addTypeVariable(TypeVariableName("E"))
    .addProperties(createBuilderProperties(analysisResult))
    .addFunctions(createBuilderRuleFunctions(className, analysisResult))
    .addFunctions(createBuildFunction(analysisResult))
    .addType(createBuilderCompanionObject(analysisResult))
    .build()
}

private fun createBuilderRuleFunctions(
  builderClassName: ClassName,
  analysisResult: SourceAnalysisResult
): Iterable<FunSpec> {
  return analysisResult.mapping.map { (sourcePropName, targetPropName) ->
    val sourcePropType = analysisResult.models.sourceTypeSpec.propertySpecs.first { it.name == sourcePropName }.type
    val targetPropType = analysisResult.models.targetTypeSpec.propertySpecs.first { it.name == targetPropName }.type
    val propValidatorType = VALIDATOR_CLASS_NAME
      .parameterizedBy(sourcePropType, targetPropType, TypeVariableName("E"))
    FunSpec.builder(sourcePropName)
      .addParameter("validator", propValidatorType)
      .returns(builderClassName.parameterizedBy(TypeVariableName("E")))
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
  val checkMissingRulesFunction = createCheckMissingRulesFunction()
  val propertyNames = analysisResult.mapping.keys.map { createRuleValidatorPropertyName(it) }
  val name = if (analysisResult.unmappedTargetProperties.isEmpty()) "build" else "buildWith"
  val parameters = createUnmappedParameterSpecs(analysisResult)
  val propertyNamesTemplate = repeatTemplate("%N!!", propertyNames.size)
  val unmappedPropertyNamesTemplate = if (parameters.isNotEmpty()) {
    repeatTemplate("%N", parameters.size, prefix = ", ")
  } else ""
  return listOf(
    checkMissingRulesFunction,
    FunSpec.builder(name)
      .addParameters(parameters)
      .returns(createValidatorParameterizedName(analysisResult))
      .addStatement("%N()", checkMissingRulesFunction)
      .addStatement(
        "return %T(%N($propertyNamesTemplate$unmappedPropertyNamesTemplate))",
        Validator::class,
        CREATE_VALIDATE_FUNCTION_NAME,
        *(propertyNames + parameters.map { it.name }).toTypedArray()
      )
      .build()
  )
}

private fun createUnmappedParameterSpecs(analysisResult: SourceAnalysisResult): List<ParameterSpec> {
  return if (analysisResult.unmappedTargetProperties.isNotEmpty()) {
    analysisResult.unmappedTargetProperties.map { property ->
      ParameterSpec.builder(property.name, property.type).build()
    }
  } else emptyList()
}

private fun createBuilderCompanionObject(analysisResult: SourceAnalysisResult): TypeSpec {
  return TypeSpec.companionObjectBuilder()
    .addFunction(createValidateFunction(analysisResult))
    .build()
}

private fun createValidateFunction(analysisResult: SourceAnalysisResult): FunSpec {
  val parameters = analysisResult.mapping.map { (sourcePropName, targetPropName) ->
    val sourcePropType = analysisResult.models.sourceTypeSpec.propertySpecs.first { it.name == sourcePropName }.type
    val targetPropType = analysisResult.models.targetTypeSpec.propertySpecs.first { it.name == targetPropName }.type
    val typeName = VALIDATOR_CLASS_NAME.parameterizedBy(sourcePropType, targetPropType, TypeVariableName("E"))
    ParameterSpec(createRuleValidatorPropertyName(sourcePropName), typeName)
  }
  val unmappedParameters = createUnmappedParameterSpecs(analysisResult)
  return FunSpec
    .builder(CREATE_VALIDATE_FUNCTION_NAME)
    .addTypeVariable(TypeVariableName("E"))
    .returns(
      LambdaTypeName.get(
        parameters = listOf(ParameterSpec.unnamed(extractSourceClassName(analysisResult))),
        returnType = createResultParameterizedByTargetName(analysisResult)
      )
    )
    .addModifiers(KModifier.PRIVATE)
    .addParameters(parameters + unmappedParameters)
    .addCode(createValidateFunctionBody(analysisResult))
    .build()
}

private fun createValidateFunctionBody(analysisResult: SourceAnalysisResult): CodeBlock {
  val targetClassName = extractTargetClassName(analysisResult)
  val errorsVariableName = "errors"
  val validationStatements = createValidationExecStatements(analysisResult, errorsVariableName)
  val unmappedPropertyNames = analysisResult.unmappedTargetProperties.map { it.name }
  val validatorParametersTemplate = repeatTemplate("%N = %N!!", analysisResult.mapping.size)
  val unmappedParametersTemplate = if (unmappedPropertyNames.isNotEmpty()) {
    repeatTemplate("%N = %N", unmappedPropertyNames.size, prefix = ", ")
  } else ""
  return CodeBlock.builder()
    .beginControlFlow("return { input ->") // BEGIN FLOW_A
    .addStatement("val %N = mutableListOf<E>()", errorsVariableName)
    .add(validationStatements.fold(CodeBlock.builder(), { builder, block -> builder.add(block) }).build())
    .beginControlFlow("if (%N.isEmpty())", errorsVariableName) // BEGIN FLOW_B
    .addStatement(
      "%T(%T($validatorParametersTemplate$unmappedParametersTemplate))",
      Result.Ok::class.asTypeName(),
      targetClassName,
      *(analysisResult.mapping.values + unmappedPropertyNames).flatMap { name -> listOf(name, name) }.toTypedArray()
    )
    .endControlFlow() // END FLOW_B
    .beginControlFlow("else") // BEGIN FLOW_C
    .addStatement(
      "%1T(%2N.first(), if (%2N.size > 1) %2N.drop(1) else null)",
      Result.Error::class.asTypeName(),
      errorsVariableName
    )
    .endControlFlow() // END FLOW_C
    .endControlFlow() // END FLOW_A
    .build()
}

private fun createValidatorParameterizedName(analysisResult: SourceAnalysisResult): ParameterizedTypeName {
  return VALIDATOR_CLASS_NAME
    .parameterizedBy(
      extractSourceClassName(analysisResult),
      extractTargetClassName(analysisResult),
      TypeVariableName("E")
    )
}

private fun createResultParameterizedByTargetName(analysisResult: SourceAnalysisResult): ParameterizedTypeName {
  return RESULT_CLASS_NAME
    .parameterizedBy(
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
 * repeatTemplate("%S", 3) => "%S, %S, %S"
 * ```
 */
private fun repeatTemplate(template: String, count: Int, prefix: String = "", suffix: String = ""): String {
  return generateSequence { template }.take(count).joinToString(prefix = prefix.orEmpty(), postfix = suffix.orEmpty())
}

private val VALIDATOR_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Validator")
private val RESULT_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Result")
private const val CREATE_VALIDATE_FUNCTION_NAME = "createValidateFunction"
private const val MISSING_RULES_PROPERTY_NAME = "missingFieldRules"
