package ru.dimsuz.vanilla.sample.dummy

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator

class DummyValidator<I, O> private constructor(
  private val output: O?,
  private val errors: List<String>?,
  private val onRight: (() -> Unit)?,
  private val onLeft: (() -> Unit)?
) : Validator<I, O, String> {

  companion object {
    fun <I, O> success(output: O, action: (() -> Unit)? = null): DummyValidator<I, O> {
      return DummyValidator(output, null, onRight = action, onLeft = null)
    }

    fun <I, O> fail(error: String, action: (() -> Unit)? = null): DummyValidator<I, O> {
      return DummyValidator(null, listOf(error), onRight = null, onLeft = action)
    }

    fun <I, O> fail(errors: List<String>, action: (() -> Unit)? = null): DummyValidator<I, O> {
      return DummyValidator(null, errors, onRight = null, onLeft = action)
    }
  }

  override fun validate(input: I): Result<O, String> {
    return if (output != null) {
      onRight?.invoke()
      Result.Ok(output)
    } else {
      onLeft?.invoke()
      Result.Error(errors!!.first(), if (errors.size == 1) null else errors.drop(1))
    }
  }
}
