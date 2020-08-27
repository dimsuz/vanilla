package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.TypeElement

typealias Error = String

data class SourceAnalysisResult(
  val models: ModelPair,
  val mapping: Map<String, String>,
  val unmappedTargetProperties: Set<PropertySpec>
)

data class ModelPair(
  val sourceTypeSpec: TypeSpec,
  val targetTypeSpec: TypeSpec,
  val sourceElement: TypeElement,
  val targetElement: TypeElement
)
