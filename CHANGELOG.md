# Changelog

## 0.14.1 - 2023-09-01

* Update kotlin to 1.9.10 and ksp to 1.9.10-1.0.13
* Fix incremental compilation
* Convert processor to isolated

## 0.14.0 - 2023-08-27

* Replace kapt with ksp

## 0.13.0 - 2023-06-15

* Upgrade kotlin version to `1.8.21`, upgrade kotlinpoet and gradle versions

## 0.12.0 - 2022-08-05

* Upgrade kotlin version to `1.7.10`, upgrade kotlin-poet to fix kapt compilation errors on projects using kotlin `1.7.x`

## 0.11.0 - 2021-08-21

* Remove `Validators` object, move built-in validators to be extensions on `Validator` companion object
* Fix generation when source/target class is a `public` class enclosed an `internal` class
* Add new combinators: `mapError`, `bind` (up to 14 arguments)

## 0.10.0 - 2021-08-06

* Fix generation when source/target class is an inner class

## 0.9.0 - 2020-03-14

* Initial release
