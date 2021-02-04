package ru.dimsuz.vanilla

import com.github.michaelbull.result.Result

inline class Validator<I, O, E>(val validate: (I) -> Result<O, List<E>>)
