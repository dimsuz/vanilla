package ru.dimsuz.vanilla.sample.dummy

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator

class DummyValidator {

  companion object {
    fun <I, O> success(output: O, action: (() -> Unit)? = null): Validator<I, O, String> {
      return Validator {
        action?.invoke()
        Result.Ok(output)
      }
    }

    fun <I, O> fail(error: String, action: (() -> Unit)? = null): Validator<I, O, String> {
      return Validator {
        action?.invoke()
        Result.Error(error)
      }
    }

    fun <I, O> fail(errors: List<String>, action: (() -> Unit)? = null): Validator<I, O, String> {
      require(errors.size > 1) { "use other fail overload for 1 error" }
      return Validator {
        action?.invoke()
        Result.Error(errors.first(), errors.drop(1))
      }
    }
  }
}
