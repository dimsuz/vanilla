package ru.dimsuz.vanilla.validator

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.SimpleValidatorComposer
import ru.dimsuz.vanilla.StartedValidatorComposer
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.ValidatorComposer

fun <T : Any, E> isNotNull(error: E): Validator<T?, T, E> {
  return object : Validator<T?, T, E> {
    override fun validate(input: T?): Result<T, E> {
      return if (input != null) Result.Ok(input) else Result.Error(error)
    }
  }
}

fun <E> isNotEmpty(error: E): Validator<String, String, E> {
  return object : Validator<String, String, E> {
    override fun validate(input: String): Result<String, E> {
      return if (input.isNotEmpty()) Result.Ok(input) else Result.Error(error)
    }
  }
}

fun <E> isNotBlank(errorProvider: (failedInput: String) -> E): Validator<String, String, E> {
  return object : Validator<String, String, E> {
    override fun validate(input: String): Result<String, E> {
      return if (input.isNotBlank()) Result.Ok(input) else Result.Error(errorProvider(input))
    }
  }
}

fun <E> hasLengthGreaterThanOrEqualTo(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return object : Validator<String, String, E> {
    override fun validate(input: String): Result<String, E> {
      return if (input.length >= length) Result.Ok(input) else Result.Error(errorProvider(input))
    }
  }
}

fun <E> hasLengthLessThanOrEqualTo(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return object : Validator<String, String, E> {
    override fun validate(input: String): Result<String, E> {
      return if (input.length <= length) Result.Ok(input) else Result.Error(errorProvider(input))
    }
  }
}

fun <E> hasLengthGreaterThan(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return hasLengthGreaterThanOrEqualTo(length + 1, errorProvider)
}

fun <E> hasLengthLessThan(
  length: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  return hasLengthLessThanOrEqualTo(length - 1, errorProvider)
}

fun <E> hasLengthInRange(
  min: Int,
  max: Int,
  errorProvider: (failedInput: String) -> E
): Validator<String, String, E> {
  require(min <= max) { "invalid range ($min..$max), expected min <= max" }
  return object : Validator<String, String, E> {
    override fun validate(input: String): Result<String, E> {
      return if (input.length in (min..max)) Result.Ok(input) else Result.Error(errorProvider(input))
    }
  }
}
