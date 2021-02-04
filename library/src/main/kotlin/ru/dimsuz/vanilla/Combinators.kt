package ru.dimsuz.vanilla

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map

fun <I, O, E> compose(
  body: ValidatorComposer<I, E>.() -> StartedValidatorComposer<I, O, E>,
): Validator<I, O, E> {
  return body(SimpleValidatorComposer()).build()
}

// TODO document that for writing validators only, because inference problems, use compose in rules
fun <I, O1, O2, E> Validator<I, O1, E>.andThen(other: Validator<O1, O2, E>): Validator<I, O2, E> {
  return Validator { input -> this.validate(input).andThen { other.validate(it) } }
}

fun <I, O1, O2, E> Validator<I, O1, E>.map(mapper: (O1) -> O2): Validator<I, O2, E> {
  return Validator { input -> this.validate(input).map(mapper) }
}

// TODO when documenting, mention that outputs are ignored, only Ok/Error matters
fun <I, E> satisfiesAnyOf(validators: Iterable<Validator<I, *, E>>): Validator<I, I, E> {
  require(validators.iterator().hasNext()) { "validator list is empty" }
  return Validator { input ->
    val errors = mutableListOf<E>()
    for (v in validators) {
      when (val result = v.validate(input)) {
        is Ok -> return@Validator Ok(input)
        is Err -> {
          errors.addAll(result.error)
        }
      }
    }
    Err(errors)
  }
}

// TODO when documenting, mention that outputs are ignored, only Ok/Error matters
fun <I, E> satisfiesAllOf(validators: Iterable<Validator<I, *, E>>): Validator<I, I, E> {
  require(validators.iterator().hasNext()) { "validator list is empty" }
  return Validator { input ->
    val errors = mutableListOf<E>()
    for (v in validators) {
      when (val result = v.validate(input)) {
        is Ok -> continue
        is Err -> {
          errors.addAll(result.error)
        }
      }
    }
    if (errors.isEmpty()) Ok(input) else Err(errors)
  }
}
