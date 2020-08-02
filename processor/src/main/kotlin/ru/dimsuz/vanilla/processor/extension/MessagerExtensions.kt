package ru.dimsuz.vanilla.processor.extension

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

internal fun Messager.note(message: String) {
  printMessage(Diagnostic.Kind.NOTE, message)
}

internal fun Messager.warning(message: String) {
  printMessage(Diagnostic.Kind.WARNING, message)
}

internal fun Messager.error(message: String) {
  printMessage(Diagnostic.Kind.ERROR, message)
}

