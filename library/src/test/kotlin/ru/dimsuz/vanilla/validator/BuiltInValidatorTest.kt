package ru.dimsuz.vanilla.validator

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.dimsuz.vanilla.Result

class BuiltInValidatorTest {
  @Test
  fun isNotNullFailsOnNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator(null)

    assertThat(result)
      .isInstanceOf(Result.Error::class.java)
  }

  @Test
  fun isNotNullReportsOkOnNotNull() {
    val validator = isNotNull<Int, String>("expected not null")
    val result = validator(3)

    assertThat(result)
      .isEqualTo(Result.Ok(3))
  }

  @Test
  fun isNotBlankFailsOnEmpty() {
    val validator = isNotBlank { "error" }
    val result = validator("")

    assertThat(result)
      .isEqualTo(Result.Error("error"))
  }

  @Test
  fun isNotBlankFailsOnBlank() {
    val validator = isNotBlank { "error: '$it'" }
    val result = validator("  \t")

    assertThat(result)
      .isEqualTo(Result.Error("error: '  \t'"))
  }

  @Test
  fun isNotBlankNoErrorForNonBlank() {
    val validator = isNotBlank { "error" }
    val result = validator("  hello")

    assertThat(result)
      .isEqualTo(Result.Ok("  hello"))
  }

  @Test
  fun isNotEmptyFailsOnEmpty() {
    val validator = isNotEmpty("error")
    val result = validator("")

    assertThat(result)
      .isEqualTo(Result.Error("error"))
  }

  @Test
  fun isNotEmptyNoErrorForNonEmpty() {
    val validator = isNotEmpty { "error" }
    val result = validator("  hello")

    assertThat(result)
      .isEqualTo(Result.Ok("  hello"))
  }

  @Test
  fun minimumLengthSuccessOnEnoughLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5) { "error" }
    val result1 = validator("world")
    val result2 = validator("worldd")

    assertThat(result1)
      .isEqualTo(Result.Ok("world"))
    assertThat(result2)
      .isEqualTo(Result.Ok("worldd"))
  }

  @Test
  fun minimumLengthFailsOnShortLength() {
    val validator = hasLengthGreaterThanOrEqualTo(5) { "error '$it'" }
    val result = validator("worl")

    assertThat(result)
      .isEqualTo(Result.Error("error 'worl'"))
  }

  @Test
  fun maximumLengthSuccessOnEnoughLength() {
    val validator = hasLengthLessThanOrEqualTo(5) { "error" }
    val result1 = validator("world")
    val result2 = validator("worl")
    val result3 = validator("")

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
    val result = validator("worldd")

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

    val result1 = validator("h")
    val result2 = validator("hello")

    assertThat(result1)
      .isEqualTo(Result.Error("error 'h'"))
    assertThat(result2)
      .isEqualTo(Result.Error("error 'hello'"))
  }

  @Test
  fun lengthInRangeSuccessOnInRangeValues() {
    val validator = hasLengthInRange(2, 4) { "error '$it'" }

    val result1 = validator("he")
    val result2 = validator("hel")
    val result3 = validator("hell")

    assertThat(result1)
      .isEqualTo(Result.Ok("he"))
    assertThat(result2)
      .isEqualTo(Result.Ok("hel"))
    assertThat(result3)
      .isEqualTo(Result.Ok("hell"))
  }
}
