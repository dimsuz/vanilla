package ru.dimsuz.vanilla.processor.extension

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement

internal val Element.enclosingPackage: PackageElement
  get() {
    var enclosing: Element? = this
    while (enclosing != null && enclosing.kind != ElementKind.PACKAGE) {
      enclosing = enclosing.enclosingElement
    }
    return (enclosing as? PackageElement) ?: throw IllegalStateException("no package element found")
  }

internal val Element.enclosingPackageName get() = enclosingPackage.qualifiedName.toString()
