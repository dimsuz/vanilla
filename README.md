# Vanilla

A validation library with distinct separation of pre- and post-validation models, focused on validator composability.

## Usage

This library is based on an idea of a clear separation between a non-validated and validated models,
which makes only a validated and mapped data appear in the resulting model:

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

Fields are matched by names, ones which are not present in the output model are ignored (`index`, `nickname`).
Properties of the source model can be annotated with `@ValidatedName` annotation which specifies a custom property name
to match in the target model.

## Validation result

Generated validator builder is parametrized by an error type, and it is up to you to choose an error representation.
Here the `enum` is used to represent individual field errors.

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

Note how errors from the individual field validators are accumulated in the final result.

## Building custom validators

Validator is simply a function of type `(I) -> Result<O, List<E>>`, where

* `I` is some input type
* `O` is a successful output type
* `E` is an error type

For example, here's an implementation of the (built-in) `isNotNull()` validator:

```kotlin
fun <I : Any, E> isNotNull(error: E): Validator<I?, I, E> {
  return Validator { input -> if (input != null) Ok(input) else Err(listOf(error)) }
}
```

Another example: an implementation of a custom validator which either converts `String` input to a `LocalDate`
output or produces an error if conversion fails:

```kotlin
fun <E> hasDateFormat(pattern: String, error: E): Validator<String, LocalDate, E> {
  return Validator { input ->
    return try {
      Ok(LocalDate.parse(input, DateTimeFormatter.ofPattern(pattern)))
    } catch (e: DateTimeParseException) {
      Err(listOf(error))
    }
  }
}
```

## Composing validators

Existing validators can be composed together to form a new validator using `buildValidator`:

```kotlin
fun <E> hasNotNullAgeAtLeast(age: Int, error: E): Validator<String?, Int, E> {
  return buildValidator {
    startWith(isNotNull(error = error))
      .andThen(hasDateFormat(pattern = "yyyy.MM.dd", error = error))
      .andThen(Validator { input: LocalDate -> Period.between(input, LocalDate.now()).years })
      .andThen(isGreaterThanOrEqual(age, error))
  }
}
```

Each successive validator in the composition chain will receive an output produced by the previous validator:

* `isNotNull` receives `String?` and returns `String`
* `hasDateFormat` receives `String` and returns `LocalDate`
* "inline" `Validator` receives `LocalDate` and returns `Int`
* finally `isGreaterThanOrEqual` receives `Int` and checks its bounds

The resulting custom validator can then be used just like any other one:

```kotlin
@ValidatedAs(Person::class)
data class PersonDraft(
  @ValidatedName("age")
  val birthday: String?
)
data class Person(
  val age: Int
)

val validator = PersonDraftValidatorBuilder<String>()
  .birthday(hasNotNullAgeAtLeast(age = 18, "expected not null age of at least 18 years old"))
  .build()
```

## Nesting validator builders

If you have several validator builders, they can be reused just like any other custom or built-in validator:

```kotlin
@ValidatedAs(Address::class)
data class AddressDraft(val city: String?, val street: String?, val house: Int?)
data class Address(val city: String, val street: String, val house: Int)

@ValidatedAs(Person::class)
data class PersonDraft(
  val homeAddress: AddressDraft,
  val extraAddresses: List<AddressDraft>?
)
data class Person(
  val homeAddress: Address,
  val extraAddresses: List<Address>
)

val addressValidator = AddressDraftValidatorBuilder<String>()
  .city(isNotNull(error = "expected not null city"))
  .street(isNotNull(error = "expected not null street"))
  .house(isNotNull(error = "expected not null house"))
  .build()

val personValidator = PersonDraftValidatorBuilder<String>()
  .homeAddress(addressValidator)
  .extraAddresses(buildValidator {
    startWith(isNotNull(error = "expected not null extra addresses"))
      .andThen(eachElement(addressValidator))
  })
  .build()
```

Note how `personValidator` is able to turn each of the draft addresses in the list into a validated `Address` model.
It will also accumulate errors (as en `Err` case) and produce a list of failed to validate addresses if there
will be any.

## Download

```
implementation "ru.dimsuz.vanilla:0.9.0"
kapt "ru.dimsuz.vanilla-processor:0.9.0"
```

## License

```
Copyright 2020 Dmitry Suzdalev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
