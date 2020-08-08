package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.toOkOrElse

fun <T : Any, E> isNotNull(errorProvider: (T?) -> E): Validator<T?, T, E> {
  return object : Validator<T?, T, E> {
    override fun validate(input: T?): Result<T, List<E>> {
      return input.toOkOrElse { listOf(errorProvider(input)) }
    }
  }
}

fun main() {
  val validator = PersonDraftValidator.Builder<String>()
    .firstName(isNotNull { "expected not null first name" })
    .lastName(isNotNull { "expected not null second name" })
    .build()
}
