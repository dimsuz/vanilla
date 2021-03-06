package ru.dimsuz.vanilla

/**
 * Helps to compose several validators together
 */
interface ValidatorComposer<I, E> {
  /**
   * Starts a validator composition chain
   */
  fun <O> startWith(v: Validator<I, O, E>): StartedValidatorComposer<I, O, E>
}

/**
 * Helps to compose several validators together
 */
interface StartedValidatorComposer<I, O1, E> {
  /**
   * Adds another validator to the current composition
   */
  fun <O2> andThen(v: Validator<O1, O2, E>): StartedValidatorComposer<I, O2, E>
  /**
   * Finalizes composition chain, returns a resulting [Validator]
   */
  fun <O> build(): Validator<I, O, E>
}
