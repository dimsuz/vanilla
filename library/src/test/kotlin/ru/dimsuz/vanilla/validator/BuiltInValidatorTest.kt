package ru.dimsuz.vanilla.validator

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.dimsuz.vanilla.Result

class BuiltInValidatorTest {
  @Test
  fun isNotNullFailsOnNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator.validate(null)

    assertThat(result)
      .isInstanceOf(Result.Error::class.java)
  }

  @Test
  fun isNotNullReportsOkOnNotNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator.validate(3)

    assertThat(result)
      .isEqualTo(Result.Ok(3))
  }

  @Test
  fun isNotBlankFailsOnEmpty() {
    val validator = isNotBlank { "error" }
    val result = validator.validate("")

    assertThat(result)
      .isEqualTo(Result.Error("error"))
  }

  @Test
  fun isNotBlankFailsOnBlank() {
    val validator = isNotBlank { "error: '$it'" }
    val result = validator.validate("  \t")

    assertThat(result)
      .isEqualTo(Result.Error("error: '  \t'"))
  }

  @Test
  fun isNotBlankNoErrorForNonBlank() {
    val validator = isNotBlank { "error" }
    val result = validator.validate("  hello")

    assertThat(result)
      .isEqualTo(Result.Ok("  hello"))
  }

  @Test
  fun isNotEmptyFailsOnEmpty() {
    val validator = isNotEmpty("error")
    val result = validator.validate("")

    assertThat(result)
      .isEqualTo(Result.Error("error"))
  }

  @Test
  fun isNotEmptyNoErrorForNonEmpty() {
    val validator = isNotEmpty { "error" }
    val result = validator.validate("  hello")

    assertThat(result)
      .isEqualTo(Result.Ok("  hello"))
  }

  @Test
  fun minimumLengthSuccessOnEnoughLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5) { "error" }
    val result1 = validator.validate("world")
    val result2 = validator.validate("worldd")

    assertThat(result1)
      .isEqualTo(Result.Ok("world"))
    assertThat(result2)
      .isEqualTo(Result.Ok("worldd"))
  }

  @Test
  fun minimumLengthFailsOnShortLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5) { "error '$it'" }
    val result = validator.validate("worl")

    assertThat(result)
      .isEqualTo(Result.Error("error 'worl'"))
  }

  @Test
  fun maximumLengthSuccessOnEnoughLength() {
    val validator = hasLengthLessThanOrEqualTo(5) { "error" }
    val result1 = validator.validate("world")
    val result2 = validator.validate("worl")
    val result3 = validator.validate("")

    assertThat(result1)
      .isEqualTo(Result.Ok("world"))
    assertThat(result2)
      .isEqualTo(Result.Ok("worl"))
    assertThat(result3)
      .isEqualTo(Result.Ok(""))
  }

  @Test
  fun maximumLengthFailsOnLargeLength() {
    val validator = hasLengthLessThanOrEqualTo(5) { "error '$it'" }
    val result = validator.validate("worldd")

    assertThat(result)
      .isEqualTo(Result.Error("error 'worldd'"))
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
    val validator = hasLengthInRange(2, 4) { "error '$it'" }

    val result1 = validator.validate("h")
    val result2 = validator.validate("hello")

    assertThat(result1)
      .isEqualTo(Result.Error("error 'h'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error 'hello'"))
  }

  @Test
  fun lengthInRangeSuccessOnInRangeValues() {
    val validator = hasLengthInRange(2, 4) { "error '$it'" }

    val result1 = validator.validate("he")
    val result2 = validator.validate("hel")
    val result3 = validator.validate("hell")

    assertThat(result1)
      .isEqualTo(Result.Ok("he"))
    assertThat(result2)
      .isEqualTo(Result.Ok("hel"))
    assertThat(result3)
      .isEqualTo(Result.Ok("hell"))
  }

  @Test
  fun greaterThanLengthSuccessOnEnoughLength() {
    val validator = hasLengthGreaterThan(5) { "error" }
    val result1 = validator.validate("123456")
    val result2 = validator.validate("1234567")

    assertThat(result1)
      .isEqualTo(Result.Ok("123456"))
    assertThat(result2)
      .isEqualTo(Result.Ok("1234567"))
  }

  @Test
  fun greaterThanLengthFailsOnShortLength() {
    val validator = hasLengthGreaterThan(5) { "error '$it'" }
    val result = validator.validate("12345")

    assertThat(result)
      .isEqualTo(Result.Error("error '12345'"))
  }

  @Test
  fun lessThanLengthSuccessOnEnoughLength() {
    val validator = hasLengthLessThan(5) { "error" }
    val result1 = validator.validate("1234")
    val result2 = validator.validate("123")

    assertThat(result1)
      .isEqualTo(Result.Ok("1234"))
    assertThat(result2)
      .isEqualTo(Result.Ok("123"))
  }

  @Test
  fun lessThanLengthFailsOnShortLength() {
    val validator = hasLengthLessThan(5) { "error '$it'" }
    val result = validator.validate("12345")

    assertThat(result)
      .isEqualTo(Result.Error("error '12345'"))
  }

  @Test
  fun isLessThanSucceedsOnValidValue() {
    val validator = isLessThan(5L) { "error '$it'" }
    val result = validator.validate(4)

    assertThat(result)
      .isEqualTo(Result.Ok(4L))
  }

  @Test
  fun isLessThanFailsOnInvalidValue() {
    val validator = isLessThan(5L) { "error '$it'" }
    val result1 = validator.validate(5)
    val result2 = validator.validate(6)

    assertThat(result1)
      .isEqualTo(Result.Error("error '5'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error '6'"))
  }

  @Test
  fun isLessThanOrEqualSucceedsOnValidValue() {
    val validator = isLessThanOrEqual(5L) { "error '$it'" }
    val result1 = validator.validate(5)
    val result2 = validator.validate(4)

    assertThat(result1)
      .isEqualTo(Result.Ok(5L))
    assertThat(result2)
      .isEqualTo(Result.Ok(4L))
  }

  @Test
  fun isLessThanOrEqualFailsOnInvalidValue() {
    val validator = isLessThanOrEqual(5L) { "error '$it'" }
    val result1 = validator.validate(6)
    val result2 = validator.validate(7)

    assertThat(result1)
      .isEqualTo(Result.Error("error '6'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error '7'"))
  }

  @Test
  fun isGreaterThanSucceedsOnValidValue() {
    val validator = isGreaterThan(5L) { "error '$it'" }
    val result = validator.validate(6)

    assertThat(result)
      .isEqualTo(Result.Ok(6L))
  }

  @Test
  fun isGreaterThanFailsOnInvalidValue() {
    val validator = isGreaterThan(5L) { "error '$it'" }
    val result1 = validator.validate(5)
    val result2 = validator.validate(4)

    assertThat(result1)
      .isEqualTo(Result.Error("error '5'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error '4'"))
  }

  @Test
  fun isGreaterThanOrEqualSucceedsOnValidValue() {
    val validator = isGreaterThanOrEqual(5L) { "error '$it'" }
    val result1 = validator.validate(5)
    val result2 = validator.validate(6)

    assertThat(result1)
      .isEqualTo(Result.Ok(5L))
    assertThat(result2)
      .isEqualTo(Result.Ok(6L))
  }

  @Test
  fun isGreaterThanOrEqualFailsOnInvalidValue() {
    val validator = isGreaterThanOrEqual(5L) { "error '$it'" }
    val result1 = validator.validate(4)
    val result2 = validator.validate(3)

    assertThat(result1)
      .isEqualTo(Result.Error("error '4'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error '3'"))
  }
}
