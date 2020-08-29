package ru.dimsuz.vanilla

import ru.dimsuz.vanilla.validator.isNull

fun <I, O, E> compose(
  body: ValidatorComposer<I, E>.() -> StartedValidatorComposer<I, O, E>
): Validator<I, O, E> {
  return body(SimpleValidatorComposer()).build()
}

fun <I, E> satisfiesAnyOf(validators: Iterable<Validator<I, I, E>>): Validator<I, I, E> {
  return Validator { input ->
    val errors = mutableListOf<E>()
    for (v in validators) {
      when (val result = v.validate(input)) {
        is Result.Ok -> return@Validator Result.Ok(input)
        is Result.Error -> {
          errors.add(result.first)
          errors.addAll(result.rest.orEmpty())
        }
      }
    }
    Result.Error(errors.first(), if (errors.size > 1) errors.drop(1) else null)
  }
}
