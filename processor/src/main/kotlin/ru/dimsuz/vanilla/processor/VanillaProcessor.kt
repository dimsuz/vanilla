package ru.dimsuz.vanilla.processor

import ru.dimsuz.vanilla.annotation.ValidatedAs
import ru.dimsuz.vanilla.processor.either.Left
import ru.dimsuz.vanilla.processor.either.flatMap
import ru.dimsuz.vanilla.processor.either.join
import ru.dimsuz.vanilla.processor.extension.error
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

class VanillaProcessor : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): Set<String> {
    return listOf(
      ValidatedAs::class.java
    ).mapTo(mutableSetOf()) { it.canonicalName }
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
    val result = findValidationModelPairs(env)
      .flatMap { modelPairList ->
        modelPairList.map { findMatchingProperties(it) }.join()
      }
    if (result is Left) {
      processingEnv.messager.error(result.value)
      return true
    }
    return true
  }
}
