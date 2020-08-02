package ru.dimsuz.vanilla.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass

typealias Error = String

typealias PropertyMapping = Map<String, String>

data class ModelPair(
  val source: ImmutableKmClass,
  val target: ImmutableKmClass
)
