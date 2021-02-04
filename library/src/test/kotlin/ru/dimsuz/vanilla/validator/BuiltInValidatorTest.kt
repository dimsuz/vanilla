package ru.dimsuz.vanilla.validator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.satisfiesAllOf
import ru.dimsuz.vanilla.satisfiesAnyOf
import ru.dimsuz.vanilla.validator.Validators.eachElement
import ru.dimsuz.vanilla.validator.Validators.hasLengthGreaterThan
import ru.dimsuz.vanilla.validator.Validators.hasLengthGreaterThanOrEqualTo
import ru.dimsuz.vanilla.validator.Validators.hasLengthInRange
import ru.dimsuz.vanilla.validator.Validators.hasLengthLessThan
import ru.dimsuz.vanilla.validator.Validators.hasLengthLessThanOrEqualTo
import ru.dimsuz.vanilla.validator.Validators.isGreaterThan
import ru.dimsuz.vanilla.validator.Validators.isGreaterThanOrEqual
import ru.dimsuz.vanilla.validator.Validators.isLessThan
import ru.dimsuz.vanilla.validator.Validators.isLessThanOrEqual
import ru.dimsuz.vanilla.validator.Validators.isNotBlank
import ru.dimsuz.vanilla.validator.Validators.isNotEmpty
import ru.dimsuz.vanilla.validator.Validators.isNotNull
import ru.dimsuz.vanilla.validator.Validators.isNullOr

class BuiltInValidatorTest {
  @Test
  fun isNotNullFailsOnNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator.validate(null)

    assertThat(result)
      .isInstanceOf(Err::class.java)
  }

  @Test
  fun isNotNullReportsOkOnNotNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator.validate(3)

    assertThat(result)
      .isEqualTo(Ok(3))
  }

  @Test
  fun isNotBlankFailsOnEmpty() {
    val validator = isNotBlank("error")
    val result = validator.validate("")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isNotBlankFailsOnBlank() {
    val validator = isNotBlank("error")
    val result = validator.validate("  \t")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isNotBlankNoErrorForNonBlank() {
    val validator = isNotBlank { "error" }
    val result = validator.validate("  hello")

    assertThat(result)
      .isEqualTo(Ok("  hello"))
  }

  @Test
  fun isNotEmptyFailsOnEmpty() {
    val validator = isNotEmpty("error")
    val result = validator.validate("")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isNotEmptyNoErrorForNonEmpty() {
    val validator = isNotEmpty { "error" }
    val result = validator.validate("  hello")

    assertThat(result)
      .isEqualTo(Ok("  hello"))
  }

  @Test
  fun minimumLengthSuccessOnEnoughLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5) { "error" }
    val result1 = validator.validate("world")
    val result2 = validator.validate("worldd")

    assertThat(result1)
      .isEqualTo(Ok("world"))
    assertThat(result2)
      .isEqualTo(Ok("worldd"))
  }

  @Test
  fun minimumLengthFailsOnShortLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5, "error")
    val result = validator.validate("worl")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun maximumLengthSuccessOnEnoughLength() {
    val validator = hasLengthLessThanOrEqualTo(5) { "error" }
    val result1 = validator.validate("world")
    val result2 = validator.validate("worl")
    val result3 = validator.validate("")

    assertThat(result1)
      .isEqualTo(Ok("world"))
    assertThat(result2)
      .isEqualTo(Ok("worl"))
    assertThat(result3)
      .isEqualTo(Ok(""))
  }

  @Test
  fun maximumLengthFailsOnLargeLength() {
    val validator = hasLengthLessThanOrEqualTo(5, "error")
    val result = validator.validate("worldd")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun lengthInRangeThrowsOnInvalidRange() {
    var error: Exception? = null
    try {
      hasLengthInRange(3, 2) { "error" }
    } catch (e: Exception) {
      error = e
    }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo("invalid range (3..2), expected min <= max")
  }

  @Test
  fun lengthInRangeFailsOnOutOfRangeValues() {
    val validator = hasLengthInRange(2, 4, "error")

    val result = validator.validate("h")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun lengthInRangeSuccessOnInRangeValues() {
    val validator = hasLengthInRange(2, 4, "error")

    val result1 = validator.validate("he")
    val result2 = validator.validate("hel")
    val result3 = validator.validate("hell")

    assertThat(result1)
      .isEqualTo(Ok("he"))
    assertThat(result2)
      .isEqualTo(Ok("hel"))
    assertThat(result3)
      .isEqualTo(Ok("hell"))
  }

  @Test
  fun greaterThanLengthSuccessOnEnoughLength() {
    val validator = hasLengthGreaterThan(5) { "error" }
    val result1 = validator.validate("123456")
    val result2 = validator.validate("1234567")

    assertThat(result1)
      .isEqualTo(Ok("123456"))
    assertThat(result2)
      .isEqualTo(Ok("1234567"))
  }

  @Test
  fun greaterThanLengthFailsOnShortLength() {
    val validator = hasLengthGreaterThan(5, "error")
    val result = validator.validate("12345")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun lessThanLengthSuccessOnEnoughLength() {
    val validator = hasLengthLessThan(5) { "error" }
    val result1 = validator.validate("1234")
    val result2 = validator.validate("123")

    assertThat(result1)
      .isEqualTo(Ok("1234"))
    assertThat(result2)
      .isEqualTo(Ok("123"))
  }

  @Test
  fun lessThanLengthFailsOnShortLength() {
    val validator = hasLengthLessThan(5, "error")
    val result = validator.validate("12345")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isLessThanSucceedsOnValidValue() {
    val validator = isLessThan(5L, "error")
    val result = validator.validate(4)

    assertThat(result)
      .isEqualTo(Ok(4L))
  }

  @Test
  fun isLessThanFailsOnInvalidValue() {
    val validator = isLessThan(5L, "error")
    val result1 = validator.validate(5)
    val result2 = validator.validate(6)

    assertThat(result1)
      .isEqualTo(Err(listOf("error")))
    assertThat(result2)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isLessThanOrEqualSucceedsOnValidValue() {
    val validator = isLessThanOrEqual(5L, "error")
    val result1 = validator.validate(5)
    val result2 = validator.validate(4)

    assertThat(result1)
      .isEqualTo(Ok(5L))
    assertThat(result2)
      .isEqualTo(Ok(4L))
  }

  @Test
  fun isLessThanOrEqualFailsOnInvalidValue() {
    val validator = isLessThanOrEqual(5L, "error")
    val result1 = validator.validate(6)
    val result2 = validator.validate(7)

    assertThat(result1)
      .isEqualTo(Err(listOf("error")))
    assertThat(result2)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isGreaterThanSucceedsOnValidValue() {
    val validator = isGreaterThan(5L, "error")
    val result = validator.validate(6)

    assertThat(result)
      .isEqualTo(Ok(6L))
  }

  @Test
  fun isGreaterThanFailsOnInvalidValue() {
    val validator = isGreaterThan(5L, "error")
    val result1 = validator.validate(5)
    val result2 = validator.validate(4)

    assertThat(result1)
      .isEqualTo(Err(listOf("error")))
    assertThat(result2)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isGreaterThanOrEqualSucceedsOnValidValue() {
    val validator = isGreaterThanOrEqual(5L, "error")
    val result1 = validator.validate(5)
    val result2 = validator.validate(6)

    assertThat(result1)
      .isEqualTo(Ok(5L))
    assertThat(result2)
      .isEqualTo(Ok(6L))
  }

  @Test
  fun isGreaterThanOrEqualFailsOnInvalidValue() {
    val validator = isGreaterThanOrEqual(5L, "error")
    val result1 = validator.validate(4)
    val result2 = validator.validate(3)

    assertThat(result1)
      .isEqualTo(Err(listOf("error")))
    assertThat(result2)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun eachElementMapsOnSuccess() {
    val validator = eachElement(Validator<Int, String, String> { input -> Ok(input.toString()) })

    val result = validator.validate(setOf(33, 88))

    assertThat(result)
      .isEqualTo(Ok(listOf("33", "88")))
  }

  @Test
  fun eachElementCollectsAllErrorsOnFailure() {
    val validator = eachElement(
      Validator<Int, String, String> { input ->
        when (input) {
          88 -> Ok(input.toString())
          77 -> Err(listOf("error '$input.1'", "error '$input.2'"))
          else -> Err(listOf("error '$input'"))
        }
      }
    )

    val result = validator.validate(setOf(33, 88, 77, 55))

    assertThat(result)
      .isEqualTo(Err(listOf("error '33'", "error '77.1'", "error '77.2'", "error '55'")))
  }

  @Test
  fun eachElementReturnsOkOnEmptyList() {
    val validator = eachElement(Validator<Int, String, String> { input -> Ok(input.toString()) })

    val result = validator.validate(emptyList())

    assertThat(result)
      .isEqualTo(Ok(emptyList<String>()))
  }

  // otherwise some kind of "combiner" function would be needed to combine
  // all the results
  @Test
  fun anyOfKeepsSourceValueIgnoringOutputOfIndividualValidators() {
    val validator = satisfiesAnyOf<String?, String>(
      listOf(
        Validator { Ok("hello") },
        Validator { Ok("world") },
      )
    )

    val result = validator.validate("33")

    assertThat(result)
      .isEqualTo(Ok("33"))
  }

  @Test
  fun anyOfSuccessIfSecondOk() {
    val validator = satisfiesAnyOf<String?, String>(
      listOf(
        Validator { Err(listOf("error")) },
        Validator { Ok("hello") },
        Validator { Err(listOf("error")) },
      )
    )

    val result = validator.validate("fine")

    assertThat(result)
      .isEqualTo(Ok("fine"))
  }

  @Test
  fun anyOfErrorIfAllError() {
    val validator = satisfiesAnyOf<String?, String>(
      listOf(
        Validator { Err(listOf("error")) },
        Validator { Err(listOf("error2")) },
        Validator { Err(listOf("error3")) },
      )
    )

    val result = validator.validate("final")

    assertThat(result)
      .isEqualTo(Err(listOf("error", "error2", "error3")))
  }

  @Test
  fun anyOfRequiresNonEmptyList() {
    var exception: Throwable? = null
    try {
      satisfiesAnyOf<String?, String>(emptyList())
    } catch (e: Throwable) {
      exception = e
    }

    assertThat(exception)
      .isNotNull()
    assertThat(exception)
      .hasMessageThat()
      .contains("validator list is empty")
  }

  // otherwise some kind of "combiner" function would be needed to combine
  // all the results
  @Test
  fun allOfKeepsSourceValueIgnoringOutputOfIndividualValidators() {
    val validator = satisfiesAllOf<String?, String>(
      listOf(
        Validator { Ok("hello") },
        Validator { Ok("world") },
      )
    )

    val result = validator.validate("33")

    assertThat(result)
      .isEqualTo(Ok("33"))
  }

  @Test
  fun allOfFailsIfAnyFails() {
    val validator = satisfiesAllOf<String?, String>(
      listOf(
        Validator { Ok("world") },
        Validator { Ok("hello") },
        Validator { Err(listOf("error")) },
      )
    )

    val result = validator.validate("fine")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun allOfErrorIfAllErrorAccumulatesAllErrors() {
    val validator = satisfiesAllOf<String?, String>(
      listOf(
        Validator { Err(listOf("error")) },
        Validator { Err(listOf("error2")) },
        Validator { Err(listOf("error3")) },
      )
    )

    val result = validator.validate("final")

    assertThat(result)
      .isEqualTo(Err(listOf("error", "error2", "error3")))
  }

  @Test
  fun allOfRequiresNonEmptyList() {
    var exception: Throwable? = null
    try {
      satisfiesAllOf<String?, String>(emptyList())
    } catch (e: Throwable) {
      exception = e
    }

    assertThat(exception)
      .isNotNull()
    assertThat(exception)
      .hasMessageThat()
      .contains("validator list is empty")
  }

  @Test
  fun isNullOrDoesNotProduceAnErrorOnNull() {
    val validator = isNullOr(hasLengthGreaterThan(3, "error"))

    val result = validator.validate(null)

    assertThat(result)
      .isEqualTo(Ok(null))
  }

  @Test
  fun isNullOrProducesAnErrorOnFailingNonNull() {
    val validator = isNullOr(hasLengthGreaterThan(3, "error"))

    val result = validator.validate("he")

    assertThat(result)
      .isEqualTo(Err(listOf("error")))
  }

  @Test
  fun isNullOrDoesNotProduceAnErrorOnCorrectNonNull() {
    val validator = isNullOr(hasLengthGreaterThan(3, "error"))

    val result = validator.validate("hello")

    assertThat(result)
      .isEqualTo(Ok("hello"))
  }
}
