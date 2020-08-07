package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty

typealias Error = String

data class PropertyMapping(
  val models: ModelPair,
  val mapping: Map<ImmutableKmProperty, ImmutableKmProperty>
)

data class ModelPair(
  val source: ImmutableKmClass,
  val target: ImmutableKmClass
)
