package ru.dimsuz.vanilla.validator

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator

fun <T : Any, E> isNotNull(error: E): Validator<T?, T, E> {
  return { input -> if (input != null) Result.Ok(input) else Result.Error(error) }
}

fun <E> isNotEmpty(error: E): Validator<String, String, E> {
  return { input ->
    if (input.isNotEmpty()) Result.Ok(input) else Result.Error(error)
  }
}

fun <E> isNotBlank(errorProvider: (failedInput: String) -> E): Validator<String, String, E> {
  return { input ->
    if (input.isNotBlank()) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> minimumLength(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return { input ->
    if (input.length >= length) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> maximumLength(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return { input ->
    if (input.length <= length) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}

fun <E> lengthInRange(
  min: Int,
  max: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  require(min <= max) { "invalid range ($min..$max), expected min <= max" }
  return { input ->
    if (input.length in (min..max)) Result.Ok(input) else Result.Error(errorProvider(input))
  }
}
