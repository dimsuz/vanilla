package ru.dimsuz.vanilla

interface Validator<I, O, E> {
  fun validate(input: I): Result<O, E>
}

sealed class Result<out T, out E> {
  data class Ok<out T>(val value: T) : Result<T, Nothing>()
  data class Error<out E>(val first: E, val rest: List<E>? = null) : Result<Nothing, E>()
}

