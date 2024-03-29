package ru.dimsuz.vanilla.validator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import ru.dimsuz.vanilla.Validator

/**
 * Keeps input value as-is. Synonym for [keep]
 */
fun <I, E> Validator.Companion.ok(): Validator<I, I, E> {
  return Validator { Ok(it) }
}

/**
 * Keeps input value as-is. Synonym for [ok]
 */
fun <I, E> Validator.Companion.keep(): Validator<I, I, E> = ok()

/**
 * Always successfully validates and returns the provided [value]
 */
fun <I, O, E> Validator.Companion.just(value: O): Validator<I, O, E> {
  return Validator { Ok(value) }
}

/**
 * Returns an [Ok] with a non-null input or [error] if input value is null
 */
fun <I : Any, E> Validator.Companion.isNotNull(error: E): Validator<I?, I, E> {
  return Validator { input -> if (input != null) Ok(input) else Err(listOf(error)) }
}

/**
 * Delegates validation to the provided [validator] if input is not null, otherwise returns [error]
 */
fun <I : Any, O, E> Validator.Companion.isNotNullAnd(validator: Validator<I, O, E>, error: E): Validator<I?, O, E> {
  return Validator { input -> if (input != null) validator.validate(input) else Err(listOf(error)) }
}

/**
 * Returns an input value if it is not null, otherwise delegates to a provided [validator]
 */
fun <I : Any, E> Validator.Companion.isNullOr(validator: Validator<I, I, E>): Validator<I?, I?, E> {
  return Validator { input ->
    if (input == null) {
      Ok(input)
    } else {
      validator.validate(input)
    }
  }
}

fun <E> Validator.Companion.isNotEmpty(error: E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotEmpty()) Ok(input) else Err(listOf(error))
  }
}

fun <E> Validator.Companion.isNotBlank(error: E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotBlank()) Ok(input) else Err(listOf(error))
  }
}

fun <E> Validator.Companion.hasLengthGreaterThanOrEqualTo(
  length: Int,
  error: E,
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length >= length) Ok(input) else Err(listOf(error))
  }
}

fun <E> Validator.Companion.hasLengthLessThanOrEqualTo(
  length: Int,
  error: E,
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length <= length) Ok(input) else Err(listOf(error))
  }
}

fun <E> Validator.Companion.hasLengthGreaterThan(
  length: Int,
  error: E,
): Validator<String, String, E> {
  return hasLengthGreaterThanOrEqualTo(length + 1, error)
}

fun <E> Validator.Companion.hasLengthLessThan(
  length: Int,
  error: E,
): Validator<String, String, E> {
  return hasLengthLessThanOrEqualTo(length - 1, error)
}

fun <E> Validator.Companion.hasLengthInRange(
  min: Int,
  max: Int,
  error: E,
): Validator<String, String, E> {
  require(min <= max) { "invalid range ($min..$max), expected min <= max" }
  return Validator { input ->
    if (input.length in (min..max)) Ok(input) else Err(listOf(error))
  }
}

fun <E> Validator.Companion.matches(
  pattern: String,
  error: E,
): Validator<String, String, E> {
  return matches(Regex(pattern), error)
}

fun <E> Validator.Companion.matches(
  regex: Regex,
  error: E,
): Validator<String, String, E> {
  return Validator { input ->
    if (input.matches(regex)) Ok(input) else Err(listOf(error))
  }
}

fun <T : Comparable<T>, E> Validator.Companion.isLessThan(
  value: T,
  error: E,
): Validator<T, T, E> {
  return Validator { input ->
    if (input < value) Ok(input) else Err(listOf(error))
  }
}

fun <T : Comparable<T>, E> Validator.Companion.isLessThanOrEqual(
  value: T,
  error: E,
): Validator<T, T, E> {
  return Validator { input ->
    if (input <= value) Ok(input) else Err(listOf(error))
  }
}

fun <T : Comparable<T>, E> Validator.Companion.isGreaterThan(
  value: T,
  error: E,
): Validator<T, T, E> {
  return Validator { input ->
    if (input > value) Ok(input) else Err(listOf(error))
  }
}

fun <T : Comparable<T>, E> Validator.Companion.isGreaterThanOrEqual(
  value: T,
  error: E,
): Validator<T, T, E> {
  return Validator { input ->
    if (input >= value) Ok(input) else Err(listOf(error))
  }
}

fun <I : Iterable<IE>, IE, O, E> Validator.Companion.eachElement(
  elementValidator: Validator<IE, O, E>,
): Validator<I, List<O>, E> {
  return Validator { input ->
    val results = input.asSequence().map { elementValidator.validate(it) }
    if (results.all { it is Ok }) {
      Ok(results.map { (it as Ok).value }.toList())
    } else {
      val errors = results.filterIsInstance<Err<List<E>>>().flatMap { it.error }
      Err(errors.toList())
    }
  }
}
