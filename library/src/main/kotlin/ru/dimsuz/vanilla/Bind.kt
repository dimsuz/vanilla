package ru.dimsuz.vanilla

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import ru.dimsuz.vanilla.validator.just

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  bindFn: (A, B) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, _, _, _, _, _, _, _, _, _, _, _, _ ->
    bindFn(a, b)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  bindFn: (A, B, C) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, _, _, _, _, _, _, _, _, _, _, _ ->
    bindFn(a, b, c)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  bindFn: (A, B, C, D) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, _, _, _, _, _, _, _, _, _, _ ->
    bindFn(a, b, c, d)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  bindFn: (A, B, C, D, E) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, _, _, _, _, _, _, _, _, _ ->
    bindFn(a, b, c, d, e)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  bindFn: (A, B, C, D, E, F) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, _, _, _, _, _, _, _, _ ->
    bindFn(a, b, c, d, e, f)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  bindFn: (A, B, C, D, E, F, G) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, _, _, _, _, _, _, _ ->
    bindFn(a, b, c, d, e, f, g)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  bindFn: (A, B, C, D, E, F, G, H) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, h, _, _, _, _, _, _ ->
    bindFn(a, b, c, d, e, f, g, h)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, h, i, _, _, _, _, _ ->
    bindFn(a, b, c, d, e, f, g, h, i)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    validatorJ,
    unitValidator,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, h, i, j, _, _, _, _ ->
    bindFn(a, b, c, d, e, f, g, h, i, j)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J, K> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  validatorK: Validator<VI, K, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J, K) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    validatorJ,
    validatorK,
    unitValidator,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, h, i, j, k, _, _, _ ->
    bindFn(a, b, c, d, e, f, g, h, i, j, k)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J, K, L> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  validatorK: Validator<VI, K, VE>,
  validatorL: Validator<VI, L, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J, K, L) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    validatorJ,
    validatorK,
    validatorL,
    unitValidator,
    unitValidator
  ) { a, b, c, d, e, f, g, h, i, j, k, l, _, _ ->
    bindFn(a, b, c, d, e, f, g, h, i, j, k, l)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J, K, L, M> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  validatorK: Validator<VI, K, VE>,
  validatorL: Validator<VI, L, VE>,
  validatorM: Validator<VI, M, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J, K, L, M) -> VO
): Validator<VI, VO, VE> {
  val unitValidator = Validator.just<VI, Unit, VE>(Unit)
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    validatorJ,
    validatorK,
    validatorL,
    validatorM,
    unitValidator
  ) { a, b, c, d, e, f, g, h, i, j, k, l, m, _ ->
    bindFn(a, b, c, d, e, f, g, h, i, j, k, l, m)
  }
}

/**
 * Applies multiple validators to the value and then uses [bindFn] function to produce a final result if they
 * all return success.
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Ok(input + 9) },
 *   { a, b -> a + b }
 * )
 *
 * v.validate(1) // produces Ok(15), i.e (1 + 4) + (1 + 9)
 * ```
 *
 * If some validators fail then all their errors will be accumulated in the final [Err] list:
 *
 * ```
 * val v = bind(
 *   Validator { input -> Ok(input + 4) },
 *   Validator { input -> Err(listOf("v2 failed1", "v2 failed2")) },
 *   Validator { input -> Err(listOf("v3 failed1")) },
 *   { a, b -> a + b }
 * )
 * v.validate(1) // produces Err(listOf("v2 failed1", "v2 failed2", "v3 failed"))
 * ```
 */
fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J, K, L, M, N> Validator.Companion.bind(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  validatorK: Validator<VI, K, VE>,
  validatorL: Validator<VI, L, VE>,
  validatorM: Validator<VI, M, VE>,
  validatorN: Validator<VI, N, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> VO
): Validator<VI, VO, VE> {
  return Validator.bindN(
    validatorA,
    validatorB,
    validatorC,
    validatorD,
    validatorE,
    validatorF,
    validatorG,
    validatorH,
    validatorI,
    validatorJ,
    validatorK,
    validatorL,
    validatorM,
    validatorN,
    bindFn
  )
}

private fun <VI, VE, VO, A, B, C, D, E, F, G, H, I, J, K, L, M, N> Validator.Companion.bindN(
  validatorA: Validator<VI, A, VE>,
  validatorB: Validator<VI, B, VE>,
  validatorC: Validator<VI, C, VE>,
  validatorD: Validator<VI, D, VE>,
  validatorE: Validator<VI, E, VE>,
  validatorF: Validator<VI, F, VE>,
  validatorG: Validator<VI, G, VE>,
  validatorH: Validator<VI, H, VE>,
  validatorI: Validator<VI, I, VE>,
  validatorJ: Validator<VI, J, VE>,
  validatorK: Validator<VI, K, VE>,
  validatorL: Validator<VI, L, VE>,
  validatorM: Validator<VI, M, VE>,
  validatorN: Validator<VI, N, VE>,
  bindFn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) -> VO
): Validator<VI, VO, VE> {
  return Validator { input ->
    val errors = mutableListOf<VE>()
    val a = validatorA.validate(input).onFailure { errors.addAll(it) }.get()
    val b = validatorB.validate(input).onFailure { errors.addAll(it) }.get()
    val c = validatorC.validate(input).onFailure { errors.addAll(it) }.get()
    val d = validatorD.validate(input).onFailure { errors.addAll(it) }.get()
    val e = validatorE.validate(input).onFailure { errors.addAll(it) }.get()
    val f = validatorF.validate(input).onFailure { errors.addAll(it) }.get()
    val g = validatorG.validate(input).onFailure { errors.addAll(it) }.get()
    val h = validatorH.validate(input).onFailure { errors.addAll(it) }.get()
    val i = validatorI.validate(input).onFailure { errors.addAll(it) }.get()
    val j = validatorJ.validate(input).onFailure { errors.addAll(it) }.get()
    val k = validatorK.validate(input).onFailure { errors.addAll(it) }.get()
    val l = validatorL.validate(input).onFailure { errors.addAll(it) }.get()
    val m = validatorM.validate(input).onFailure { errors.addAll(it) }.get()
    val n = validatorN.validate(input).onFailure { errors.addAll(it) }.get()
    if (errors.isEmpty()) {
      Ok(bindFn(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!, i!!, j!!, k!!, l!!, m!!, n!!))
    } else {
      Err(errors)
    }
  }
}
