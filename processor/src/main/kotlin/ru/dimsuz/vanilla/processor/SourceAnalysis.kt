package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import ru.dimsuz.vanilla.annotation.ValidatedAs
import ru.dimsuz.vanilla.annotation.ValidatedName
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.either.Left
import ru.dimsuz.vanilla.processor.either.Right
import ru.dimsuz.vanilla.processor.either.join
import ru.dimsuz.vanilla.processor.either.lift4
import ru.dimsuz.vanilla.processor.either.toRightOr
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

fun findValidationModelPairs(roundEnv: RoundEnvironment): Either<Error, List<ModelPair>> {
  return roundEnv.getElementsAnnotatedWith(ValidatedAs::class.java).map { element ->
    val sourceElement = element as? TypeElement
    val targetElement = element.extractTargetModelClass()
    val sourceTypeSpec = sourceElement?.toImmutableKmClass()?.toTypeSpec(null)
      .toRightOr("internal error: failed to read source model information")
    val targetTypeSpec = targetElement?.toImmutableKmClass()?.toTypeSpec(null)
      .toRightOr("internal error: failed to read target model information")
    val sourceTypeElement = Right(sourceElement!!)
    val targetTypeElement = Right(targetElement!!)
    lift4(sourceTypeSpec, targetTypeSpec, sourceTypeElement, targetTypeElement, ::ModelPair)
  }.join()
}

fun findMatchingProperties(models: ModelPair): Either<Error, SourceAnalysisResult> {
  // See NOTE_PROPERTY_SORTING_ORDER
  val sourceProps = models.sourceTypeSpec.propertySpecs.sortedByDeclarationOrderIn(models.sourceElement)
  val targetProps = models.targetTypeSpec.propertySpecs.sortedByDeclarationOrderIn(models.targetElement)
  val mapping = mutableMapOf<String, String>()
  sourceProps.forEach { sProp ->
    val tProp = targetProps.find { it.name == sProp.mappedName(models.sourceElement) }
    if (tProp != null) {
      mapping[sProp.name] = tProp.name
    }
  }
  return if (mapping.isEmpty()) {
    Left(
      "failed to find matching properties. Consider adding @${ValidatedName::class.java.simpleName} " +
        "annotation to properties of \"${models.sourceTypeSpec.name}\" class"
    )
  } else {
    val unmappedTargetProperties = targetProps.filterTo(mutableSetOf()) { !mapping.containsValue(it.name) }
    Right(SourceAnalysisResult(models, mapping, unmappedTargetProperties))
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
