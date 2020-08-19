package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.toOkOrElse
import ru.dimsuz.vanilla.validator.isNotNull

fun main() {
  val validator = PersonDraftValidator.Builder<String>()
    .firstName(isNotNull { "expected not null first name" })
    .lastName(isNotNull { "expected not null second name" })
    // TODO use compose isNotNull + toInt
    .age { input -> input?.toIntOrNull().toOkOrElse { "error must be not null string convertible to int" } }
    .build()
  validator(
    PersonDraft(
      firstName = null,
      lastName = null,
      age = "23year",
      addr = null,
      phoneNumbers = null,
      friends = null,
      extraUnused1 = 1,
      extraUnused2 = ""
    )
  )
}
