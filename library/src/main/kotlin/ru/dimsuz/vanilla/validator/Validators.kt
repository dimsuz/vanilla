package ru.dimsuz.vanilla.validator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import ru.dimsuz.vanilla.Validator

object Validators {
  fun <I, E> ok(): Validator<I, I, E> {
    return Validator { Ok(it) }
  }

  fun <O, E> just(value: O): Validator<Unit, O, E> {
    return Validator { Ok(value) }
  }

  fun <I, E> keep(): Validator<I, I, E> = ok()

  fun <I : Any, E> isNotNull(error: E): Validator<I?, I, E> {
    return Validator { input -> if (input != null) Ok(input) else Err(listOf(error)) }
  }

  fun <I : Any, O, E> isNotNullAnd(validator: Validator<I, O, E>, error: E): Validator<I?, O, E> {
    return Validator { input -> if (input != null) validator.validate(input) else Err(listOf(error)) }
  }

  fun <I : Any, E> isNullOr(validator: Validator<I, I, E>): Validator<I?, I?, E> {
    return Validator { input ->
      if (input == null) {
        Ok(input)
      } else {
        validator.validate(input)
      }
    }
  }

  fun <E> isNotEmpty(error: E): Validator<String, String, E> {
    return Validator { input ->
      if (input.isNotEmpty()) Ok(input) else Err(listOf(error))
    }
  }

  fun <E> isNotBlank(error: E): Validator<String, String, E> {
    return Validator { input ->
      if (input.isNotBlank()) Ok(input) else Err(listOf(error))
    }
  }

  fun <E> hasLengthGreaterThanOrEqualTo(
    length: Int,
    error: E
  ): Validator<String, String, E> {
    return Validator { input ->
      if (input.length >= length) Ok(input) else Err(listOf(error))
    }
  }

  fun <E> hasLengthLessThanOrEqualTo(
    length: Int,
    error: E
  ): Validator<String, String, E> {
    return Validator { input ->
      if (input.length <= length) Ok(input) else Err(listOf(error))
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
      if (input.length in (min..max)) Ok(input) else Err(listOf(error))
    }
  }

  fun <E> matches(
    pattern: String,
    error: E
  ): Validator<String, String, E> {
    return matches(Regex(pattern), error)
  }

  fun <E> matches(
    regex: Regex,
    error: E
  ): Validator<String, String, E> {
    return Validator { input ->
      if (input.matches(regex)) Ok(input) else Err(listOf(error))
    }
  }

  fun <T : Comparable<T>, E> isLessThan(
    value: T,
    error: E
  ): Validator<T, T, E> {
    return Validator { input ->
      if (input < value) Ok(input) else Err(listOf(error))
    }
  }

  fun <T : Comparable<T>, E> isLessThanOrEqual(
    value: T,
    error: E
  ): Validator<T, T, E> {
    return Validator { input ->
      if (input <= value) Ok(input) else Err(listOf(error))
    }
  }

  fun <T : Comparable<T>, E> isGreaterThan(
    value: T,
    error: E
  ): Validator<T, T, E> {
    return Validator { input ->
      if (input > value) Ok(input) else Err(listOf(error))
    }
  }

  fun <T : Comparable<T>, E> isGreaterThanOrEqual(
    value: T,
    error: E
  ): Validator<T, T, E> {
    return Validator { input ->
      if (input >= value) Ok(input) else Err(listOf(error))
    }
  }

  fun <I : Iterable<IE>, IE, O, E> eachElement(
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
}
