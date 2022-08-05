package ru.dimsuz.vanilla.processor.extension

import com.squareup.kotlinpoet.ClassName
import kotlinx.metadata.KmClass

val KmClass.simpleName: String get() {
  return this.name.substringAfterLast('/')
}

val KmClass.packageName: String get() {
  return this.name.substringBeforeLast('/', missingDelimiterValue = "").replace('/', '.')
}

fun KmClass.toClassName(): ClassName {
  return ClassName(this.packageName, this.simpleName)
}
