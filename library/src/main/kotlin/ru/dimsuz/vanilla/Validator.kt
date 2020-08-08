package ru.dimsuz.vanilla

interface Validator<I, O, E> {
  fun validate(input: I): Result<O, List<E>>
}

sealed class Result<out T, out E> {
  data class Ok<out T>(val value: T) : Result<T, Nothing>()
  data class Error<out E>(val value: E) : Result<Nothing, E>()
}

inline fun <T, E> T?.toOkOrElse(errorProvider: (T?) -> E): Result<T, E> {
  return if (this != null) Result.Ok(this) else Result.Error(errorProvider(this))
}
