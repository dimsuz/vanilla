package ru.dimsuz.vanilla

interface ValidatorComposer<I, E> {
  fun <O> startWith(v: Validator<I, O, E>): StartedValidatorComposer<I, O, E>
}

interface StartedValidatorComposer<I, O1, E> {
  fun <O2> andThen(v: Validator<O1, O2, E>): StartedValidatorComposer<I, O2, E>
  fun <O> build(): Validator<I, O, E>
}

