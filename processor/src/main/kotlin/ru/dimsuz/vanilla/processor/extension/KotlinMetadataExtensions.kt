package ru.dimsuz.vanilla.processor.extension

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass

val ImmutableKmClass.simpleName: String get() {
  return this.name.substringAfterLast('/')
}

val ImmutableKmClass.packageName: String get() {
  return this.name.substringBeforeLast('/', missingDelimiterValue = "").replace('/', '.')
}

fun ImmutableKmClass.toClassName(): ClassName {
  return ClassName(this.packageName, this.simpleName)
}
