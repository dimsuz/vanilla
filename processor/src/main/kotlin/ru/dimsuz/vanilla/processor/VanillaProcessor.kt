package ru.dimsuz.vanilla.processor

import ru.dimsuz.vanilla.annotation.ValidatedAs
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
    println("running processor!")
    return false
  }
}
