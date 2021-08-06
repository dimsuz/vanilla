package ru.dimsuz.vanilla.sample

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.dimsuz.vanilla.buildValidator
import ru.dimsuz.vanilla.sample.dummy.DummyValidator
import ru.dimsuz.vanilla.validator.Validators.hasLengthLessThan
import ru.dimsuz.vanilla.validator.Validators.isNotBlank
import ru.dimsuz.vanilla.validator.Validators.isNotNull
import ru.dimsuz.vanilla.validator.Validators.isNullOr
import ru.dimsuz.vanilla.validator.Validators.just
import ru.dimsuz.vanilla.validator.Validators.ok

class SampleImplementationTest {
  @Test
  fun `given a validator with missing rules should list field names without rules`() {
    val draft = createPersonDraftModel()

    var exception: Exception? = null
    try {
      PersonDraftValidatorBuilder<String>()
        .build()
        .validate(draft)
    } catch (e: Exception) {
      exception = e
    }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "missing validation rules for properties: \"firstName\", \"lastName\", \"age\", \"addr\", " +
          "\"phoneNumbers\", \"friends\""
      )
  }

  @Test
  fun `given a validator with partially missing rules should list missing and specified rules`() {
    val draft = createPersonDraftModel()

    var exception: Exception? = null
    try {
      PersonDraftValidatorBuilder<String>()
        .addr(DummyValidator.success(createAddressModel()))
        .build()
        .validate(draft)
    } catch (e: Exception) {
      exception = e
    }

    assertThat(exception?.message)
      .contains(
        "missing validation rules for properties: \"firstName\", \"lastName\", \"age\", " +
          "\"phoneNumbers\", \"friends\""
      )
  }

  @Test
  fun `given a set of simple validation rules and valid models should correctly parse and validate`() {
    val draft = createPersonDraftModel(
      firstName = "Fiodor",
      lastName = "Dostoyevsky",
      age = "33",
      addr = AddressDraft(city = "Moscow", street = "Tverskaya", house = 3, extraData = emptyMap(), poBox = null),
      phoneNumbers = listOf(PhoneNumberDraft(PhoneType.HomePhone, "334455")),
      friends = emptyMap(),
      extraUnused1 = 3,
      extraUnused2 = "none"
    )
    val expectedAddress = Address(city = "Moscow", street = "Tverskaya", house = 3, districtNameId = null, poBox = null)
    val expectedPhoneNumbers = listOf(PhoneNumber(PhoneType.HomePhone, "334455"))
    val expectedPerson = Person(
      firstName = "Fiodor",
      lastName = "Dostoyevsky",
      age = 33,
      address = expectedAddress,
      phoneNumbers = expectedPhoneNumbers,
      friends = emptyMap()
    )

    val result = PersonDraftValidatorBuilder<String>()
      .firstName(isNotNull("first name is null"))
      .lastName(isNotNull("last name is null"))
      .age(DummyValidator.success(33))
      .addr(DummyValidator.success(expectedAddress))
      .phoneNumbers(DummyValidator.success(expectedPhoneNumbers))
      .friends(DummyValidator.success(emptyMap()))
      .build()
      .validate(draft)

    assertThat(result)
      .isEqualTo(Ok(expectedPerson))
  }

  @Test
  fun `given a set of simple validation rules and not valid models should correctly report errors`() {
    val draft = createPersonDraftModel(
      firstName = "Fiodor",
      lastName = null,
      age = null,
      addr = null,
      phoneNumbers = null,
      friends = null,
      extraUnused1 = 3,
      extraUnused2 = "none"
    )

    val result = PersonDraftValidatorBuilder<String>()
      .firstName(DummyValidator.success("Fiodor"))
      .lastName(DummyValidator.fail("lastName error"))
      .age(DummyValidator.fail(listOf("age error 1", "age error 2")))
      .addr(DummyValidator.fail("addr error"))
      .phoneNumbers(DummyValidator.fail("phoneNumbers error"))
      .friends(DummyValidator.fail(listOf("friends error 1", "friends error 2")))
      .build()
      .validate(draft)

    assertThat(result)
      .isEqualTo(
        Err(
          listOf(
            "lastName error", "age error 1", "age error 2", "addr error", "phoneNumbers error",
            "friends error 1", "friends error 2"
          )
        )
      )
  }

  @Test
  fun `given a set of rules with composer and valid models should correctly chain rules`() {
    val draft = createPersonDraftModel(
      firstName = "Fiodor",
      lastName = "Dostoyevsky",
      age = "33",
      addr = AddressDraft(city = "Moscow", street = "Tverskaya", house = 3, extraData = emptyMap(), poBox = null),
      phoneNumbers = listOf(PhoneNumberDraft(PhoneType.HomePhone, "334455")),
      friends = emptyMap(),
      extraUnused1 = 3,
      extraUnused2 = "none"
    )
    val expectedAddress = Address(city = "Moscow", street = "Tverskaya", house = 3, districtNameId = null, poBox = null)
    val expectedPhoneNumbers = listOf(PhoneNumber(PhoneType.HomePhone, "334455"))
    val expectedPerson = Person(
      firstName = "Fiodor3",
      lastName = "Dostoyevsky",
      age = 33,
      address = expectedAddress,
      phoneNumbers = expectedPhoneNumbers,
      friends = emptyMap()
    )

    val trace = mutableListOf<String>()

    val result = PersonDraftValidatorBuilder<String>()
      .firstName(
        buildValidator {
          startWith(DummyValidator.success("Fiodor", action = { trace.add("first") }))
            .andThen(DummyValidator.success("Fiodor2", action = { trace.add("second") }))
            .andThen(DummyValidator.success("Fiodor3", action = { trace.add("third") }))
        }
      )
      .lastName(isNotNull("expected not null "))
      .age(DummyValidator.success(33))
      .addr(DummyValidator.success(expectedAddress))
      .phoneNumbers(DummyValidator.success(expectedPhoneNumbers))
      .friends(DummyValidator.success(emptyMap()))
      .build()
      .validate(draft)

    assertThat(trace)
      .containsExactly("first", "second", "third")
    assertThat(result)
      .isEqualTo(Ok(expectedPerson))
  }

  @Test
  fun `given a set of rules with composer when chain has error should report error and break chain execution`() {
    val draft = createPersonDraftModel(
      firstName = "Fiodor",
      lastName = "Dostoyevsky",
      age = "33",
      addr = AddressDraft(city = "Moscow", street = "Tverskaya", house = 3, extraData = emptyMap(), poBox = null),
      phoneNumbers = listOf(PhoneNumberDraft(PhoneType.HomePhone, "334455")),
      friends = emptyMap(),
      extraUnused1 = 3,
      extraUnused2 = "none"
    )
    val address = Address(city = "Moscow", street = "Tverskaya", house = 3, districtNameId = null, poBox = null)

    val trace = mutableListOf<String>()

    val result = PersonDraftValidatorBuilder<String>()
      .firstName(
        buildValidator {
          startWith(DummyValidator.success("Fiodor", action = { trace.add("first") }))
            .andThen<String>(
              DummyValidator.fail(
                listOf("error1", "error2"),
                action = { trace.add("second") }
              )
            )
            .andThen(DummyValidator.success("Fiodor3", action = { trace.add("third") }))
        }
      )
      .lastName(isNotNull("expected not null"))
      .age(DummyValidator.success(33))
      .addr(DummyValidator.success(address))
      .phoneNumbers(DummyValidator.success(emptyList()))
      .friends(DummyValidator.success(emptyMap()))
      .build()
      .validate(draft)

    assertThat(trace)
      .containsExactly("first", "second")
    assertThat(result)
      .isEqualTo(Err(listOf("error1", "error2")))
  }

  @Test
  fun `given a set of rules when chain has error and non-chain has error should accumulate both`() {
    val draft = createPersonDraftModel(
      firstName = "Fiodor",
      lastName = "Dostoyevsky",
      age = "33",
      addr = AddressDraft(city = "Moscow", street = "Tverskaya", house = 3, extraData = emptyMap(), poBox = null),
      phoneNumbers = listOf(PhoneNumberDraft(PhoneType.HomePhone, "334455")),
      friends = emptyMap(),
      extraUnused1 = 3,
      extraUnused2 = "none"
    )
    val address = Address(city = "Moscow", street = "Tverskaya", house = 3, districtNameId = null, poBox = null)

    val trace = mutableListOf<String>()

    val result = PersonDraftValidatorBuilder<String>()
      .firstName(
        buildValidator {
          startWith(DummyValidator.success("Fiodor", action = { trace.add("first") }))
            .andThen(
              DummyValidator.fail<String, String>(
                listOf("error1", "error2"),
                action = { trace.add("second") }
              )
            )
            .andThen(DummyValidator.success("Fiodor3", action = { trace.add("third") }))
        }
      )
      .lastName(isNotNull("expected not null"))
      .age(DummyValidator.fail("some age error"))
      .addr(DummyValidator.success(address))
      .phoneNumbers(DummyValidator.success(emptyList()))
      .friends(DummyValidator.success(emptyMap()))
      .build()
      .validate(draft)

    assertThat(result)
      .isEqualTo(Err(listOf("error1", "error2", "some age error")))
  }

  @Test
  fun `given a model with extra target field, correctly constructs validated target`() {
    val validator = AddressDraftValidatorBuilder<String>()
      .city(isNotNull("null city"))
      .house(isNotNull("null house"))
      .street(isNotNull("null street"))
      .poBox(isNullOr(ok()))
      .districtNameId(just("hubba bubba"))
      .build()

    val result = validator.validate(AddressDraft("fjfj", "fjfj", 33, emptyMap(), poBox = "33"))
    assertThat(result)
      .isInstanceOf(Ok::class.java)
    assertThat((result as Ok).value.districtNameId)
      .isEqualTo("hubba bubba")
  }

  @Test
  fun `given a model with nullable field, correctly preserves it if validator leaves it null`() {
    val validator = AddressDraftValidatorBuilder<String>()
      .city(isNotNull("null city"))
      .house(isNotNull("null house"))
      .street(isNotNull("null street"))
      .poBox(isNullOr(hasLengthLessThan(333, "error")))
      .districtNameId(just("hubba bubba"))
      .build()

    val result = validator.validate(AddressDraft("fjfj", "fjfj", 33, emptyMap(), poBox = null))
    assertThat(result)
      .isInstanceOf(Ok::class.java)
    assertThat((result as Ok).value.poBox)
      .isNull()
  }

  @Test
  fun `correctly composes nullable field validator`() {
    val validator = AddressDraftValidatorBuilder<String>()
      .city(
        buildValidator {
          startWith(isNotNull("null city"))
            .andThen(isNotBlank("blank city"))
            .andThen(just("city"))
        }
      )
      .house(isNotNull("null house"))
      .street(isNotNull("null street"))
      .poBox(isNullOr(hasLengthLessThan(333, "error")))
      .districtNameId(just("hubba bubba"))
      .build()

    val result = validator.validate(AddressDraft(null, "fjfj", 33, emptyMap(), poBox = null))
    assertThat(result)
      .isInstanceOf(Err::class.java)
  }
}

private fun createPersonDraftModel(
  firstName: String? = null,
  lastName: String? = null,
  age: String? = null,
  addr: AddressDraft? = null,
  phoneNumbers: List<PhoneNumberDraft>? = null,
  friends: Map<PersonDraft, AddressDraft>? = null,
  extraUnused1: Int = 0,
  extraUnused2: String = ""
): PersonDraft {
  return PersonDraft(
    firstName = firstName,
    lastName = lastName,
    age = age,
    addr = addr,
    phoneNumbers = phoneNumbers,
    friends = friends,
    extraUnused1 = extraUnused1,
    extraUnused2 = extraUnused2
  )
}

private fun createAddressModel(): Address {
  return Address(
    city = "Kaliningrad",
    street = "Epronovskaya",
    house = 33,
    districtNameId = "334",
    poBox = null
  )
}
