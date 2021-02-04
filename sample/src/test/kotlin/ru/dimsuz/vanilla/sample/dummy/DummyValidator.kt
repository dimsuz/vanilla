package ru.dimsuz.vanilla.sample.dummy

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import ru.dimsuz.vanilla.Validator

class DummyValidator {

  companion object {
    fun <I, O> success(output: O, action: (() -> Unit)? = null): Validator<I, O, String> {
      return Validator {
        action?.invoke()
        Ok(output)
      }
    }

    fun <I, O> fail(error: String, action: (() -> Unit)? = null): Validator<I, O, String> {
      return Validator {
        action?.invoke()
        Err(listOf(error))
      }
    }

    fun <I, O> fail(errors: List<String>, action: (() -> Unit)? = null): Validator<I, O, String> {
      require(errors.size > 1) { "use other fail overload for 1 error" }
      return Validator {
        action?.invoke()
        Err(errors)
      }
    }
  }
}
