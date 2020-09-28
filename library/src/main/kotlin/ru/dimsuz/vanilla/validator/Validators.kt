package ru.dimsuz.vanilla.validator

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator

fun <I, E> ok(): Validator<I, I, E> {
  return Validator { Result.Ok(it) }
}

fun <T : Any, E> isNotNull(error: E): Validator<T?, T, E> {
  return Validator { input -> if (input != null) Result.Ok(input) else Result.Error(error) }
}

fun <I : Any, E> isNullOr(validator: Validator<I, I, E>): Validator<I?, I?, E> {
  return Validator { input ->
    if (input == null) {
      Result.Ok(input)
    } else {
      validator.validate(input)
    }
  }
}

fun <E> isNotEmpty(error: E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotEmpty()) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> isNotBlank(error: E): Validator<String, String, E> {
  return Validator { input ->
    if (input.isNotBlank()) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> hasLengthGreaterThanOrEqualTo(
  length: Int,
  error: E
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length >= length) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> hasLengthLessThanOrEqualTo(
  length: Int,
  error: E
): Validator<String, String, E> {
  return Validator { input ->
    if (input.length <= length) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> hasLengthGreaterThan(
  length: Int,
  error: E
): Validator<String, String, E> {
  return hasLengthGreaterThanOrEqualTo(length + 1, error)
}

fun <E> hasLengthLessThan(
  length: Int,
  error: E
): Validator<String, String, E> {
  return hasLengthLessThanOrEqualTo(length - 1, error)
}

fun <E> hasLengthInRange(
  min: Int,
  max: Int,
  error: E
): Validator<String, String, E> {
  require(min <= max) { "invalid range ($min..$max), expected min <= max" }
  return Validator { input ->
    if (input.length in (min..max)) Result.Ok(input) else Result.Error(error)
  }
}

fun <T : Comparable<T>, E> isLessThan(
  value: T,
  error: E
): Validator<T, T, E> {
  return Validator { input ->
    if (input < value) Result.Ok(input) else Result.Error(error)
  }
}

fun <T : Comparable<T>, E> isLessThanOrEqual(
  value: T,
  error: E
): Validator<T, T, E> {
  return Validator { input ->
    if (input <= value) Result.Ok(input) else Result.Error(error)
  }
}

fun <T : Comparable<T>, E> isGreaterThan(
  value: T,
  error: E
): Validator<T, T, E> {
  return Validator { input ->
    if (input > value) Result.Ok(input) else Result.Error(error)
  }
}

fun <T : Comparable<T>, E> isGreaterThanOrEqual(
  value: T,
  error: E
): Validator<T, T, E> {
  return Validator { input ->
    if (input >= value) Result.Ok(input) else Result.Error(error)
  }
}

fun <I : Iterable<IE>, IE, O, E> eachElement(
  elementValidator: Validator<IE, O, E>,
): Validator<I, List<O>, E> {
  return Validator { input ->
    val results = input.asSequence().map { elementValidator.validate(it) }
    if (results.all { it is Result.Ok }) {
      Result.Ok(results.map { (it as Result.Ok).value }.toList())
    } else {
      val errors = results.filterIsInstance<Result.Error<E>>().flatMap { it.errors }
      Result.Error(errors.toList())
    }
  }
}
