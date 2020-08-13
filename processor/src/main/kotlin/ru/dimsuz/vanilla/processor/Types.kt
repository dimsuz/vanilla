package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import javax.lang.model.element.TypeElement

typealias Error = String

data class PropertyMapping(
  val models: ModelPair,
  val mapping: Map<ImmutableKmProperty, ImmutableKmProperty>
)

data class ModelPair(
  val sourceKmClass: ImmutableKmClass,
  val targetKmClass: ImmutableKmClass,
  val sourceElement: TypeElement,
  val targetElement: TypeElement
)
