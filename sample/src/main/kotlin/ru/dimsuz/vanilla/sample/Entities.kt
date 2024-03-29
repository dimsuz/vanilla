package ru.dimsuz.vanilla.sample

import ru.dimsuz.vanilla.ValidatedAs
import ru.dimsuz.vanilla.ValidatedName

@ValidatedAs(Person::class)
data class PersonDraft(
  val firstName: String?,
  val lastName: String?,
  val age: String?,
  @ValidatedName("address")
  val addr: AddressDraft?,
  val phoneNumbers: List<PhoneNumberDraft>?,
  val friends: Map<PersonDraft, AddressDraft>?,
  val extraUnused1: Int,
  val extraUnused2: String?
)

@ValidatedAs(Address::class)
data class AddressDraft(
  val city: String?,
  val street: String?,
  val house: Int?,
  val extraData: Map<String, Int>,
  val poBox: String?
)

@ValidatedAs(PhoneNumber::class)
data class PhoneNumberDraft(
  val type: PhoneType?,
  val number: String?
)

enum class PhoneType { HomePhone, WorkPhone, CellPhone, OtherPhone }

data class Address(
  val city: String,
  val street: String,
  val house: Int,
  val districtNameId: String?,
  val poBox: String?
)

data class PhoneNumber(
  val type: PhoneType,
  val number: String
)

data class Person(
  val firstName: String,
  val lastName: String,
  val age: Int,
  val address: Address,
  val phoneNumbers: List<PhoneNumber>,
  val friends: Map<Person, Address>
)
