package ru.dimsuz.vanilla.validator

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.bind

class BindTest {
  @Test
  fun bindAccumulatesAllErrors() {
    val result = Validator.bind(
      Validator { Ok(33) },
      Validator<String, Int, String> { Err(listOf("one")) },
      Validator<String, Int, String> { Err(listOf("two", "three")) }
    ) { a, b, c -> a + b + c }

    assertThat(result.validate("hi"))
      .isEqualTo(Err(listOf("one", "two", "three")))
  }

  @Test
  fun bindCorrectWhenNoErrors() {
    val result = Validator.bind(
      Validator<String, Int, String> { Ok(33) },
      Validator { Ok(44) },
      Validator { Ok(10) },
      Validator { Ok(20) },
    ) { a, b, c, d -> a + b + c + d }

    assertThat(result.validate("hi"))
      .isEqualTo(Ok(107))
  }
}
