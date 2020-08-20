package ru.dimsuz.vanilla

@Suppress("UNCHECKED_CAST") // interface generic params ensure consistent usage, internally can cast away
internal class SimpleValidatorComposer<I, E> :
  ValidatorComposer<I, E> {
  private val validators = mutableListOf<Validator<Any, Any, Any>>()
  override fun <SO> startWith(v: Validator<I, SO, E>): StartedValidatorComposer<I, SO, E> {
    validators.add(v as Validator<Any, Any, Any>)
    return StartedComposer()
  }

  private inner class StartedComposer<CI, SO, E> :
    StartedValidatorComposer<CI, SO, E> {
    override fun <ATO> andThen(v: Validator<SO, ATO, E>): StartedValidatorComposer<CI, ATO, E> {
      validators.add(v as Validator<Any, Any, Any>)
      return this as StartedValidatorComposer<CI, ATO, E>
    }

    override fun <O> build(): Validator<CI, O, E> {
      return object : Validator<CI, O, E> {
        override fun validate(input: CI): Result<O, E> {
          var currentInput = input as Any
          var errorResult: Result<O, E>? = null
          for (v in validators) {
            when (val result = v.validate(currentInput)) {
              is Result.Error -> {
                errorResult = result as Result<O, E>
              }
              is Result.Ok -> {
                currentInput = result.value
              }
            }
            if (errorResult != null) break
          }
          return errorResult ?: (Result.Ok(currentInput) as Result<O, E>)
        }
      }
    }
  }
}
