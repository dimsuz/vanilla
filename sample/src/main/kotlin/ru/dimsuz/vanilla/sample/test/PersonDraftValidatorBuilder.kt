package ru.dimsuz.vanilla.sample.test

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.sample.Address
import ru.dimsuz.vanilla.sample.AddressDraft
import ru.dimsuz.vanilla.sample.Person
import ru.dimsuz.vanilla.sample.PersonDraft
import ru.dimsuz.vanilla.sample.PhoneNumber
import ru.dimsuz.vanilla.sample.PhoneNumberDraft

data class PersonDraftValidationErrors<E>(
  val firstName: Result.Error<E>?,
  val lastName: Result.Error<E>?,
  val age: Result.Error<E>?,
  val addr: Result.Error<AddressDraftValidationErrors<E>>?,
  val phoneNumbers: Result.Error<E>?,
  val friends: Result.Error<E>?,
)

sealed class ChildFormValidationError<out E, out PE> {
  data class FormError<out E>(val value: E) : ChildFormValidationError<E, Nothing>()
  data class FormPropertyError<out PE>(val value: PE) : ChildFormValidationError<Nothing, PE>()
}

class PersonDraftValidatorBuilder<E> {

  private val missingFieldRules: MutableList<String> = mutableListOf("firstName", "lastName", "age",
    "addr", "phoneNumbers", "friends")

  private var firstNameValidator: Validator<String?, String, E>? = null

  private var lastNameValidator: Validator<String?, String, E>? = null

  private var ageValidator: Validator<String?, Int, E>? = null

  private var addrValidator: Validator<AddressDraft?, Address, ChildFormValidationError<E, AddressDraftValidationErrors<E>>>? = null

  private var phoneNumbersValidator: Validator<List<PhoneNumberDraft>?, List<PhoneNumber>, E>? =
    null

  private var friendsValidator: Validator<Map<PersonDraft, AddressDraft>?, Map<Person, Address>, E>? = null

  fun firstName(validator: Validator<String?, String, E>): PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("firstName")
    firstNameValidator = validator
    return this
  }

  fun lastName(validator: Validator<String?, String, E>): PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("lastName")
    lastNameValidator = validator
    return this
  }

  fun age(validator: Validator<String?, Int, E>): PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("age")
    ageValidator = validator
    return this
  }

  fun addr(validator: Validator<AddressDraft?, Address, ChildFormValidationError<E, AddressDraftValidationErrors<E>>>): PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("addr")
    addrValidator = validator
    return this
  }

  fun phoneNumbers(validator: Validator<List<PhoneNumberDraft>?, List<PhoneNumber>, E>):
    PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("phoneNumbers")
    phoneNumbersValidator = validator
    return this
  }

  fun friends(validator: Validator<Map<PersonDraft, AddressDraft>?, Map<Person, Address>, E>):
    PersonDraftValidatorBuilder<E> {
    missingFieldRules.remove("friends")
    friendsValidator = validator
    return this
  }

  private fun checkMissingRules() {
    if (missingFieldRules.isNotEmpty()) {
      val fieldNames = missingFieldRules.joinToString { """"$it"""" }
      error("""missing validation rules for properties: $fieldNames""")
    }
  }

  fun build(): Validator<PersonDraft, Person, PersonDraftValidationErrors<E>> {
    checkMissingRules()
    return Validator(createValidateFunction(firstNameValidator!!, lastNameValidator!!,
      ageValidator!!, addrValidator!!, phoneNumbersValidator!!, friendsValidator!!))
  }

  companion object {
    private fun <E> createValidateFunction(
      firstNameValidator: Validator<String?, String, E>,
      lastNameValidator: Validator<String?, String, E>,
      ageValidator: Validator<String?, Int, E>,
      addrValidator: Validator<AddressDraft?, Address, ChildFormValidationError<E, AddressDraftValidationErrors<E>>>,
      phoneNumbersValidator: Validator<List<PhoneNumberDraft>?, List<PhoneNumber>, E>,
      friendsValidator: Validator<Map<PersonDraft, AddressDraft>?, Map<Person, Address>, E>,
    ): (PersonDraft) -> Result<Person, PersonDraftValidationErrors<E>> = { input ->
      val firstNameResult = firstNameValidator.validate(input.firstName)
      val lastNameResult = lastNameValidator.validate(input.lastName)
      val ageResult = ageValidator.validate(input.age)
      val addrResult = addrValidator.validate(input.addr)
      val phoneNumbersResult = phoneNumbersValidator.validate(input.phoneNumbers)
      val friendsResult = friendsValidator.validate(input.friends)
      if (firstNameResult is Result.Ok && lastNameResult is Result.Ok && ageResult is Result.Ok && addrResult is Result.Ok &&
        phoneNumbersResult is Result.Ok && friendsResult is Result.Ok) {
        Result.Ok(Person(firstName = firstNameResult.value, lastName = lastNameResult.value, age = ageResult.value,
          address = addrResult.value, phoneNumbers = phoneNumbersResult.value, friends = friendsResult.value))
      } else {
        Result.Error(PersonDraftValidationErrors(
          firstName = firstNameResult as? Result.Error<E>,
          lastName = lastNameResult as? Result.Error<E>,
          age = ageResult as? Result.Error<E>,
          addr = addrResult as? Result.Error<AddressDraftValidationErrors<E>>,
          phoneNumbers = phoneNumbersResult as? Result.Error<E>,
          friends = friendsResult as? Result.Error<E>
        ))
      }
    }
  }
}
