package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.compose
import ru.dimsuz.vanilla.validator.isNotNull

fun main() {
  val validator = PersonDraftValidatorBuilder<String>()
    .firstName(isNotNull("expected not null first name"))
    .lastName(isNotNull("expected not null second name"))
    .age(
      compose {
        startWith<String>(isNotNull("age must not be null"))
          .andThen(
            Validator { input ->
              input.toIntOrNull()?.let { Result.Ok(it) }
                ?: Result.Error("error must be not null string convertible to int")
            }
          )
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
