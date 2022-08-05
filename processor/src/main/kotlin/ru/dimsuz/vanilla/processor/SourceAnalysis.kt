package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.toResultOr
import com.github.michaelbull.result.zip
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toKmClass
import ru.dimsuz.vanilla.ValidatedAs
import ru.dimsuz.vanilla.ValidatedName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

fun findValidationModelPairs(roundEnv: RoundEnvironment): Result<List<ModelPair>, Error> {
  return roundEnv.getElementsAnnotatedWith(ValidatedAs::class.java).map { element ->
    val sourceElement = element as? TypeElement
    val targetElement = element.extractTargetModelClass()
    if (sourceElement?.typeParameters?.isNotEmpty() == true) {
      return Err("Source class \"${sourceElement.simpleName}\" has a generic parameter. This is not supported yet")
    }
    if (targetElement?.typeParameters?.isNotEmpty() == true) {
      return Err("Target class \"${targetElement.simpleName}\" has a generic parameter. This is not supported yet")
    }
    val sourceTypeSpec = sourceElement?.toKmClass()?.toTypeSpec(null)
      .toResultOr { "internal error: failed to read source model information" }
    val targetTypeSpec = targetElement?.toKmClass()?.toTypeSpec(null)
      .toResultOr { "internal error: failed to read target model information" }
    val sourceTypeElement = Ok(sourceElement!!)
    val targetTypeElement = Ok(targetElement!!)
    zip(
      { sourceTypeSpec },
      { targetTypeSpec },
      { sourceTypeElement },
      { targetTypeElement },
      ::ModelPair
    )
  }.combine()
}

fun findMatchingProperties(models: ModelPair): Result<SourceAnalysisResult, Error> {
  // See NOTE_PROPERTY_SORTING_ORDER
  val sourceProps = models.sourceTypeSpec.propertySpecs.sortedByDeclarationOrderIn(models.sourceElement)
  val targetProps = models.targetTypeSpec.propertySpecs.sortedByDeclarationOrderIn(models.targetElement)
  val mapping = mutableMapOf<PropertySpec, PropertySpec>()
  sourceProps.forEach { sProp ->
    val tProp = targetProps.find { it.name == sProp.mappedName(models.sourceElement) }
    if (tProp != null) {
      mapping[sProp] = tProp
    }
  }
  return if (mapping.isEmpty()) {
    Err(
      "failed to find matching properties. Consider adding @${ValidatedName::class.java.simpleName} " +
        "annotation to properties of \"${models.sourceTypeSpec.name}\" class"
    )
  } else {
    val unmappedTargetProperties = targetProps.minus(mapping.values).toSet()
    val additionalProperties = unmappedTargetProperties
      .associateBy { PropertySpec.builder(it.name, Unit::class).build() }
    Ok(SourceAnalysisResult(models, mapping + additionalProperties, unmappedTargetProperties))
  }
}

// See NOTE_PROPERTY_SORTING_ORDER
private fun List<PropertySpec>.sortedByDeclarationOrderIn(sourceElement: TypeElement): List<PropertySpec> {
  val fieldIndexes = sourceElement
    .enclosedElements
    .filter { it.kind == ElementKind.FIELD }
    .mapIndexed { index, element -> element.simpleName.toString() to index }
    .toMap()
  return this
    .sortedBy { fieldIndexes[it.name] ?: error("failed to find index of the field '${it.name}'") }
}

private fun PropertySpec.mappedName(sourceTypeElement: TypeElement): String {
  val annotatedElement = sourceTypeElement.enclosedElements
    .filter { it is VariableElement && it.getAnnotation(ValidatedName::class.java) != null }
    .find { it.simpleName.toString() == this.name }
  return annotatedElement?.getAnnotation(ValidatedName::class.java)?.name ?: this.name
}

private fun Element.extractTargetModelClass(): TypeElement? {
  val baseClassType = try {
    this.getAnnotation(ValidatedAs::class.java).verifiedModel
    error("expected ${MirroredTypeException::class.java.simpleName} to be thrown")
  } catch (e: MirroredTypeException) {
    e.typeMirror
  }
  return ((baseClassType as? DeclaredType)?.asElement() as? TypeElement)
}

// NOTE_PROPERTY_SORTING_ORDER
// Due to the bug in kotlinx-metadata library, property specs end up being sorted in alphabetical order,
// which results in not optimally looking generated code and inconvenient API.
// To work around that java element api is used to extract original properties order.
// Bugs are reported here:
//   - https://youtrack.jetbrains.com/issue/KT-41042
//   - https://github.com/square/kotlinpoet/issues/965
