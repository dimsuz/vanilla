package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.combine
import com.github.michaelbull.result.flatMap
import ru.dimsuz.vanilla.annotation.ValidatedAs
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
        modelPairList.map { findMatchingProperties(it) }.combine()
      }
      .flatMap { propertyMappings ->
        propertyMappings.map { generateValidator(processingEnv, it) }.combine()
      }
    if (result is Err) {
      processingEnv.messager.error(result.error)
      return true
    }
    return true
  }
}
