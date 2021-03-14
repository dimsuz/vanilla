package ru.dimsuz.vanilla

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

/**
 * Validates input data of type [I] and produces either:
 *   - An [Ok] value, containing a validated data (which can be of different type [O])
 *   - An [Err] value, containing a list of validation errors
 */
inline class Validator<I, O, E>(val validate: (I) -> Result<O, List<E>>)
