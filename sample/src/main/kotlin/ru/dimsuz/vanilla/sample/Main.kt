package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.compose
import ru.dimsuz.vanilla.validator.isNotNull

fun main() {
  val validator = PersonDraftValidator.Builder<String>()
    .firstName(isNotNull("expected not null first name"))
    .lastName(isNotNull("expected not null second name"))
    .age(
      compose {
        startWith(isNotNull(""))
          .andThen(object : Validator<String, Int, String> {
            override fun validate(input: String): Result<Int, String> {
              TODO("Not yet implemented")
            }
          })
      }
    )
    .build()
  validator.validate(
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
