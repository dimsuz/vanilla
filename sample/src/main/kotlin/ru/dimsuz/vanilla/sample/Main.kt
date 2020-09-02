package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.compose
import ru.dimsuz.vanilla.sample.test.AddressDraftValidatorBuilder
import ru.dimsuz.vanilla.sample.test.PersonDraftValidatorBuilder
import ru.dimsuz.vanilla.validator.isNotNull

fun main() {
  val validator = PersonDraftValidatorBuilder<String>()
    .firstName(isNotNull("expected not null first name"))
    .lastName(isNotNull("expected not null second name"))
    .addr(compose {
      startWith(isNotNull("he"))
        .andThen(
          AddressDraftValidatorBuilder<String>()
            .city(isNotNull("h"))
            .house(isNotNull("h"))
            .street(isNotNull(""))
            .buildWith(districtNameId = null)
        )
    })
    .age(
      compose {
        startWith(isNotNull("age must not be null"))
          .andThen(
            Validator { input ->
              input.toIntOrNull()?.let { Result.Ok(it) }
                ?: Result.Error("error must be not null string convertible to int")
            }
          )
      }
    )
    .build()
  val result = validator.validate(
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
  if (result is Result.Error) {
    result.errors.first().addr!!.errors.first().city
  }
}
