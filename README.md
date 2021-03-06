# Vanilla

A validation library with distinct separation of pre- and post-validation models, focused on validator composability.

## Usage

This library assumes a clear separation between a non-validated and validated models, which makes only a validated and 
mapped data appear in the resulting model:

```kotlin
@ValidatedAs(Person::class)
data class PersonDraft(
  val firstName: String?,
  val lastName: String?,
  val age: String?,
  val index: Int,
  val nickname: String?
)

data class Person(
  val firstName: String,
  val lastName: String,
  val age: Int
)
```

Given above data types, library will generate a validator builder which will let you specify validation and mapping rules:

```kotlin
val validator = PersonDraftValidatorBuilder<String>()
  .firstName(isNotNull(error = "expected not null first name"))
  .lastName(isNotNull(error = "expected not null last name"))
  .age(isNonNullInteger(error = "expected not null int as age value"))
  .build()

val personDraft = PersonDraft(
  firstName = "Alex",
  lastName = "Smirnoff",
  age = "23",
  index = 1,
  nickname = "al183"
)

println(validator.validate(personDraft)) 
// "Ok(Person(firstName = "Alex", lastName = "Smirnoff", age = 23))"

println(validator.validate(personDraft.copy(firstName = null, lastName = null)))
// "Err(error = ["expected not null first name", "expected not null last name"])"
```

Note that validator successful output is a nice and clean `Person` class with _not nullable_ `firstName` and `lastName`
fields and `age` field converted from `String?` to `Int`.

## Validation result

Resulting validator builder is parametrized by an error type, it is up to you to choose an error representation:

```kotlin
enum class PersonFormError { MissingFirstName, MissingLastName, AgeIsNotInt }

val validator = PersonDraftValidatorBuilder<PersonFormError>()
  .firstName(isNotNull(error = PersonFormError.MissingFirstName))
  .lastName(isNotNull(error = PersonFormError.MissingLastName))
  .age(isNonNullInteger(error = PersonFormError.AgeIsNotInt))
  .build()
```

Validation result is an instance of the `Result` [type](https://github.com/michaelbull/kotlin-result) which would be either an `Ok(validatedModel)` or an `Err<List<E>>`, 
where `E` is an error type:

```kotlin
val result = validator.validate(
  PersonDraft(firstName = null, lastName = "Smirnoff", age = null, index = 1, nickname = null)
)

println(result)
// Err(error = [MissingFirstName, AgeIsNotInt])
```

## Building and composing validators
