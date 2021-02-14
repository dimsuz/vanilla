package ru.dimsuz.vanilla.sample

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.buildValidator
import ru.dimsuz.vanilla.validator.Validators.isNotNull

fun main() {
  val validator = PersonDraftValidatorBuilder<String>()
    .firstName(isNotNull("expected not null first name"))
    .lastName(isNotNull("expected not null second name"))
    .age(
      buildValidator {
        startWith(isNotNull("age must not be null"))
          .andThen(
            Validator { input ->
              input.toIntOrNull()?.let { Ok(it) }
                ?: Err(listOf("error must be not null string convertible to int"))
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
