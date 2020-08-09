package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import ru.dimsuz.vanilla.annotation.ValidatedAs
import ru.dimsuz.vanilla.annotation.ValidatedName
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.either.Left
import ru.dimsuz.vanilla.processor.either.Right
import ru.dimsuz.vanilla.processor.either.join
import ru.dimsuz.vanilla.processor.either.lift3
import ru.dimsuz.vanilla.processor.either.toRightOr
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

fun findValidationModelPairs(roundEnv: RoundEnvironment): Either<Error, List<ModelPair>> {
  return roundEnv.getElementsAnnotatedWith(ValidatedAs::class.java).map { element ->
    val sourceKmClass = (element as? TypeElement)?.toImmutableKmClass()
      .toRightOr("internal error: failed to read source model information")
    val targetKmClass = element.extractTargetModelClass()?.toImmutableKmClass()
      .toRightOr("internal error: failed to read target model information")
    val sourceTypeElement = Right(element as TypeElement)
    lift3(sourceKmClass, targetKmClass, sourceTypeElement, ::ModelPair)
  }.join()
}

fun findMatchingProperties(models: ModelPair): Either<Error, PropertyMapping> {
  val sourceProps = models.sourceKmClass.properties.toSet()
  val targetProps = models.targetKmClass.properties.toSet()
  val mapping = mutableMapOf<ImmutableKmProperty, ImmutableKmProperty>()
  sourceProps.forEach { sProp ->
    val tProp = targetProps.find { it.name == sProp.mappedName(models.sourceElement) }
    if (tProp != null) {
      mapping[sProp] = tProp
    }
  }
  return if (mapping.isEmpty()) {
    Left(
      "failed to find matching properties. Consider adding @${ValidatedName::class.java.simpleName} " +
        "annotation to properties of \"${models.sourceKmClass.name}\" class"
    )
  } else {
    Right(PropertyMapping(models, mapping))
  }
}

private fun ImmutableKmProperty.mappedName(sourceTypeElement: TypeElement): String {
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
