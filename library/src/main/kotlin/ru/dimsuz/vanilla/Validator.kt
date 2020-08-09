package ru.dimsuz.vanilla

typealias Validator<I, O, E> = (I) -> Result<O, E>

sealed class Result<out T, out E> {
  data class Ok<out T>(val value: T) : Result<T, Nothing>()
  data class Error<out E>(val first: E, val rest: List<E>? = null) : Result<Nothing, E>()
}

inline fun <T, E> T?.toOkOrElse(errorProvider: (T?) -> E): Result<T, E> {
  return if (this != null) Result.Ok(this) else Result.Error(errorProvider(this))
}
