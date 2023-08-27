package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.zip
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toTypeName
import ru.dimsuz.vanilla.ValidatedAs
import ru.dimsuz.vanilla.ValidatedName
import kotlin.reflect.KClass

fun findValidationModelPairs(sourceElement: KSClassDeclaration): Result<ModelPair, Error> {
  val targetElement = sourceElement.extractTargetClassDeclaration()
  if (sourceElement.typeParameters.isNotEmpty()) {
    return Err("Source class \"${sourceElement.simpleName.asString()}\" has a generic parameter. This is not supported yet")
  }
  if (targetElement?.typeParameters?.isNotEmpty() == true) {
    return Err("Target class \"${targetElement.simpleName.asString()}\" has a generic parameter. This is not supported yet")
  }
  return Ok(
    ModelPair(
      sourceElement,
      targetElement!!
    )
  )
}

fun findMatchingProperties(models: ModelPair): Result<SourceAnalysisResult, Error> {
  val sourceProperties = models.sourceElement.getAllProperties()
  val targetProperties = models.targetElement.getAllProperties()
  val mapping = mutableMapOf<PropertySpec, PropertySpec>()
  sourceProperties.forEach { sourceProperty ->
    val targetProperty = targetProperties
      .find { it.simpleName.asString() == sourceProperty.mappedName(models.sourceElement) }
    if (targetProperty != null) {
      mapping[sourceProperty.toPropertySpec()] = targetProperty.toPropertySpec()
    }
  }
  return if (mapping.isEmpty()) {
    Err(
      "failed to find matching properties. Consider adding @${ValidatedName::class.java.simpleName} " +
        "annotation to properties of \"${models.sourceElement.simpleName.asString()}\" class"
    )
  } else {
    val unmappedTargetProperties = targetProperties
      .map { it.toPropertySpec() }
      .minus(mapping.values.toSet())
      .toSet()
    val additionalProperties = unmappedTargetProperties.associateBy {
      PropertySpec.builder(it.name, Unit::class).build()
    }
    Ok(
      SourceAnalysisResult(
        models,
        mapping + additionalProperties,
        unmappedTargetProperties
      )
    )
  }
}

private fun KSPropertyDeclaration.toPropertySpec(): PropertySpec {
  return PropertySpec.builder(this.simpleName.asString(), this.type.toTypeName()).build()
}

private fun KSPropertyDeclaration.mappedName(sourceTypeElement: KSClassDeclaration): String {
  val annotatedElement = sourceTypeElement.getAllProperties()
    .filter { it.getAnnotation(ValidatedName::class) != null }
    .find { it.simpleName.asString() == this.simpleName.asString() }
    ?.getAnnotation(ValidatedName::class)
    ?.getArgument(VALIDATE_NAME_PARAMETER_NAME) as? String
  return annotatedElement ?: this.simpleName.asString()
}

private fun KSClassDeclaration.extractTargetClassDeclaration(): KSClassDeclaration? {
  val consumerType = getAnnotation(ValidatedAs::class)
    ?.getArgument(VALIDATE_AS_PARAMETER_NAME) as? KSType
  return consumerType?.declaration as? KSClassDeclaration
}

private fun KSAnnotated.getAnnotation(kclass: KClass<*>): KSAnnotation? {
  return annotations.firstOrNull {
    it.shortName.asString() == kclass.simpleName &&
      it.annotationType.resolve().declaration.qualifiedName?.asString() == kclass.qualifiedName
  }
}

private fun KSAnnotation.getArgument(name: String): Any? {
  return arguments.firstOrNull { it.name?.asString() == name }?.value
}

private const val VALIDATE_AS_PARAMETER_NAME = "verifiedModel"
private const val VALIDATE_NAME_PARAMETER_NAME = "name"
