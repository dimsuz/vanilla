package ru.dimsuz.vanilla

inline class Validator<I, O, E>(val validate: (I) -> Result<O, E>)

sealed class Result<out T, out E> {
  data class Ok<out T>(val value: T) : Result<T, Nothing>()
  data class Error<out E>(val first: E, val rest: List<E>? = null) : Result<Nothing, E>()
}

inline fun <T1, T2, E> Result<T1, E>.map(mapper: (T1) -> T2): Result<T2, E> {
  return when (this) {
    is Result.Ok -> Result.Ok(mapper(value))
    is Result.Error -> this
  }
}
