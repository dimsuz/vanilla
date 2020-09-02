package ru.dimsuz.vanilla.sample.test

import ru.dimsuz.vanilla.Result
import ru.dimsuz.vanilla.Validator
import ru.dimsuz.vanilla.sample.Address
import ru.dimsuz.vanilla.sample.AddressDraft

data class AddressDraftValidationErrors<E>(
  val city: Result.Error<E>?,
  val street: Result.Error<E>?,
  val house: Result.Error<E>?,
)

class AddressDraftValidatorBuilder<E> {
  private val missingFieldRules: MutableList<String> = mutableListOf("city", "street", "house")

  private var cityValidator: Validator<String?, String, E>? = null

  private var streetValidator: Validator<String?, String, E>? = null

  private var houseValidator: Validator<Int?, Int, E>? = null

  fun city(validator: Validator<String?, String, E>): AddressDraftValidatorBuilder<E> {
    missingFieldRules.remove("city")
    cityValidator = validator
    return this
  }

  fun street(validator: Validator<String?, String, E>): AddressDraftValidatorBuilder<E> {
    missingFieldRules.remove("street")
    streetValidator = validator
    return this
  }

  fun house(validator: Validator<Int?, Int, E>): AddressDraftValidatorBuilder<E> {
    missingFieldRules.remove("house")
    houseValidator = validator
    return this
  }

  private fun checkMissingRules() {
    if (missingFieldRules.isNotEmpty()) {
      val fieldNames = missingFieldRules.joinToString { """"$it"""" }
      error("""missing validation rules for properties: $fieldNames""")
    }
  }

  fun buildWith(districtNameId: String?): Validator<AddressDraft, Address, AddressDraftValidationErrors<E>> {
    checkMissingRules()
    return Validator(createValidateFunction(cityValidator!!, streetValidator!!, houseValidator!!,
      districtNameId))
  }

  companion object {
    private fun <E> createValidateFunction(
      cityValidator: Validator<String?, String, E>,
      streetValidator: Validator<String?, String, E>,
      houseValidator: Validator<Int?, Int, E>,
      districtNameId: String?
    ): (AddressDraft) -> Result<Address, AddressDraftValidationErrors<E>> = { input ->
      val cityResult = cityValidator.validate(input.city)
      val streetResult = streetValidator.validate(input.street)
      val houseResult = houseValidator.validate(input.house)
      if (cityResult is Result.Ok && streetResult is Result.Ok && houseResult is Result.Ok) {
        Result.Ok(Address(city = cityResult.value, street = streetResult.value, house = houseResult.value, districtNameId =
        districtNameId))
      }
      else {
        Result.Error(
          AddressDraftValidationErrors(
            city = cityResult as? Result.Error<E>,
            street = streetResult as? Result.Error<E>,
            house = houseResult as? Result.Error<E>
          )
        )
      }
    }
  }
}
