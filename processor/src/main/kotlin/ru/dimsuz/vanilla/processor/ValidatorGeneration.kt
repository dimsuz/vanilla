package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
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
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.processor.extension.enclosingPackageName
import ru.dimsuz.vanilla.processor.file.writeFile
import javax.annotation.processing.ProcessingEnvironment

fun generateValidator(processingEnv: ProcessingEnvironment, analysisResult: SourceAnalysisResult): Result<Unit, Error> {
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
    val isUnmapped = analysisResult.unmappedTargetProperties.contains(tProp)
    CodeBlock.builder()
      .let {
        if (isUnmapped) {
          it.beginControlFlow(
            "val %N = when (val result = %N.validate(Unit))",
            tProp,
            createRuleValidatorPropertyName(sProp.name)
          )
        } else {
          it.beginControlFlow(
            "val %N = when (val result = %N.validate(input.%N))",
            tProp,
            createRuleValidatorPropertyName(sProp.name),
            sProp
          )
        }
      }
      .beginControlFlow("is %T ->", Err::class.asTypeName())
      .addStatement("%N.addAll(result.error)", errorsVariableName)
      .addStatement("null")
      .endControlFlow()
      .addStatement("is %T -> result.value", Ok::class.asTypeName())
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
  val classModifiers = if (analysisResult.models.sourceTypeSpec.modifiers.contains(KModifier.INTERNAL) ||
    analysisResult.models.targetTypeSpec.modifiers.contains(KModifier.INTERNAL)
  ) listOf(KModifier.INTERNAL) else emptyList()
  return TypeSpec.classBuilder(className)
    .addTypeVariable(TypeVariableName("E"))
    .addProperties(createBuilderProperties(analysisResult))
    .addFunctions(createBuilderRuleFunctions(className, analysisResult))
    .addFunctions(createBuildFunction(analysisResult))
    .addType(createBuilderCompanionObject(analysisResult))
    .addModifiers(classModifiers)
    .build()
}

private fun createBuilderRuleFunctions(
  builderClassName: ClassName,
  analysisResult: SourceAnalysisResult
): Iterable<FunSpec> {
  return analysisResult.mapping.map { (sourceProp, targetProp) ->
    val propValidatorType = VALIDATOR_CLASS_NAME
      .parameterizedBy(sourceProp.type, targetProp.type, TypeVariableName("E"))
    FunSpec.builder(sourceProp.name)
      .addParameter("validator", propValidatorType)
      .returns(builderClassName.parameterizedBy(TypeVariableName("E")))
      .addStatement("%N.remove(%S)", MISSING_RULES_PROPERTY_NAME, sourceProp.name)
      .addStatement("%N = validator", createRuleValidatorPropertyName(sourceProp.name))
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
  val propertyNames = analysisResult.mapping.keys.map { createRuleValidatorPropertyName(it.name) }
  val propertyNamesTemplate = repeatTemplate("%N!!", propertyNames.size)
  return listOf(
    checkMissingRulesFunction,
    FunSpec.builder("build")
      .returns(createValidatorParameterizedName(analysisResult))
      .addStatement("%N()", checkMissingRulesFunction)
      .addStatement(
        "return %T(%N($propertyNamesTemplate))",
        Validator::class,
        CREATE_VALIDATE_FUNCTION_NAME,
        *propertyNames.toTypedArray()
      )
      .build()
  )
}

private fun createBuilderCompanionObject(analysisResult: SourceAnalysisResult): TypeSpec {
  return TypeSpec.companionObjectBuilder()
    .addFunction(createValidateFunction(analysisResult))
    .build()
}

private fun createValidateFunction(analysisResult: SourceAnalysisResult): FunSpec {
  val parameters = analysisResult.mapping.map { (sourceProp, targetProp) ->
    val typeName = VALIDATOR_CLASS_NAME.parameterizedBy(sourceProp.type, targetProp.type, TypeVariableName("E"))
    ParameterSpec(createRuleValidatorPropertyName(sourceProp.name), typeName)
  }
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
    .addParameters(parameters)
    .addCode(createValidateFunctionBody(analysisResult))
    .build()
}

private fun SourceAnalysisResult.targetPropertyIsNullable(targetPropertyName: String): Boolean {
  return this.models.targetTypeSpec.propertySpecs.first { it.name == targetPropertyName }.type.isNullable
}

private fun createValidateFunctionBody(analysisResult: SourceAnalysisResult): CodeBlock {
  val targetClassName = extractTargetClassName(analysisResult)
  val errorsVariableName = "errors"
  val validationStatements = createValidationExecStatements(analysisResult, errorsVariableName)
  val validatorParametersTemplate = analysisResult.mapping.values.joinToString { tProp ->
    if (analysisResult.targetPropertyIsNullable(tProp.name)) "%N = %N" else "%N = %N!!"
  }
  return CodeBlock.builder()
    .beginControlFlow("return { input ->") // BEGIN FLOW_A
    .addStatement("val %N = mutableListOf<E>()", errorsVariableName)
    .add(validationStatements.fold(CodeBlock.builder(), { builder, block -> builder.add(block) }).build())
    .beginControlFlow("if (%N.isEmpty())", errorsVariableName) // BEGIN FLOW_B
    .addStatement(
      "%T(%T($validatorParametersTemplate))",
      Ok::class.asTypeName(),
      targetClassName,
      *(analysisResult.mapping.values).flatMap { name -> listOf(name, name) }.toTypedArray()
    )
    .endControlFlow() // END FLOW_B
    .beginControlFlow("else") // BEGIN FLOW_C
    .addStatement(
      "%T(%N)",
      Err::class.asTypeName(),
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
      ClassName("kotlin.collections", "List").parameterizedBy(TypeVariableName("E"))
    )
}

private fun createBuilderProperties(analysisResult: SourceAnalysisResult): List<PropertySpec> {
  return ArrayList<PropertySpec>(analysisResult.mapping.size + 1).apply {
    add(createMissingFieldRulesProperty(analysisResult.mapping.keys.mapTo(mutableSetOf()) { it.name }))
    addAll(createRuleValidatorProperties(analysisResult))
  }
}

private fun createRuleValidatorProperties(analysisResult: SourceAnalysisResult): Iterable<PropertySpec> {
  return analysisResult.mapping.map { (sourceProp, targetProp) ->
    val typeName = VALIDATOR_CLASS_NAME
      .parameterizedBy(sourceProp.type, targetProp.type, TypeVariableName("E"))
      .copy(nullable = true)
    PropertySpec.builder(createRuleValidatorPropertyName(sourceProp.name), typeName, KModifier.PRIVATE)
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
  return generateSequence { template }.take(count).joinToString(prefix = prefix, postfix = suffix)
}

private val VALIDATOR_CLASS_NAME = ClassName("ru.dimsuz.vanilla", "Validator")
private val RESULT_CLASS_NAME = ClassName("com.github.michaelbull.result", "Result")
private const val CREATE_VALIDATE_FUNCTION_NAME = "createValidateFunction"
private const val MISSING_RULES_PROPERTY_NAME = "missingFieldRules"
