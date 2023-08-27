package ru.dimsuz.vanilla.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.PropertySpec

typealias Error = String

data class SourceAnalysisResult(
  val models: ModelPair,
  val mapping: Map<PropertySpec, PropertySpec>,
  val unmappedTargetProperties: Set<PropertySpec>
)

data class ModelPair(
  val sourceElement: KSClassDeclaration,
  val targetElement: KSClassDeclaration
)
