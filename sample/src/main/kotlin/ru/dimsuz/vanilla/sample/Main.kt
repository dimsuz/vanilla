package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.toOkOrElse

fun <T : Any, E> isNotNull(errorProvider: (T?) -> E): Validator<T?, T, E> {
  return { input -> input.toOkOrElse(errorProvider) }
}

fun main() {
  val validator = PersonDraftValidator.Builder<String>()
    .firstName(isNotNull { "expected not null first name" })
    .lastName(isNotNull { "expected not null second name" })
    // TODO use compose isNotNull + toInt
    .age { input -> input?.toIntOrNull().toOkOrElse { "error must be not null string convertible to int" } }
    .build()
}
