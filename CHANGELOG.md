# Changelog

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
