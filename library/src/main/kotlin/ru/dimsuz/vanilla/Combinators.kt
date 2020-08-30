package ru.dimsuz.vanilla

fun <I, O, E> compose(
  body: ValidatorComposer<I, E>.() -> StartedValidatorComposer<I, O, E>
): Validator<I, O, E> {
  return body(SimpleValidatorComposer()).build()
}

// TODO document that for writing validators only, because inference problems, use compose in rules
fun <I, O1, O2, E> Validator<I, O1, E>.andThen(other: Validator<O1, O2, E>): Validator<I, O2, E> {
  return Validator { input ->
    when (val result = this.validate(input)) {
      is Result.Ok -> other.validate(result.value)
      is Result.Error -> result
    }
  }
}

fun <I, O1, O2, E> Validator<I, O1, E>.map(mapper: (O1) -> O2): Validator<I, O2, E> {
  return Validator { input -> this.validate(input).map(mapper) }
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
