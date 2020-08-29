package ru.dimsuz.vanilla.processor

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInternal
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.isPrivate
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import kotlinx.metadata.KmClassifier
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@KotlinPoetMetadataPreview
class VanillaProcessorTest {
  @Rule
  @JvmField var temporaryFolder: TemporaryFolder = TemporaryFolder()

  @Test
  fun classMustHaveProperties() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        
        @ValidatedAs(Validated::class)
        class Draft
        class Validated(val name: String)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages).contains(
      "error: failed to find matching properties. " +
        "Consider adding @ValidatedName annotation to properties of \"Draft\" class"
    )
  }

  @Test
  fun parameterizedSourceNotSupported() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft<T>(val firstName: T?)
        data class Validated(val firstName: String?)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
      .contains("Source class \"Draft\" has a generic parameter. This is not supported yet.")
  }

  @Test
  fun parameterizedTargetNotSupported() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int)
        data class Validated<T>(val firstName: T?)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
      .contains("Target class \"Validated\" has a generic parameter. This is not supported yet.")
  }

  @Test
  fun matchesDirectProperties() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val lastName: String)
        data class Validated(val firstName: Int, val lastName: String)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    assertThat(validatorClass.functions.map { it.name })
      .containsAtLeast("firstName", "lastName")
  }

  @Test
  fun matchesPropertiesDifferingInType() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        package ru.dimsuz.vanilla.test

        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val isAdult: String)
        data class Validated(val firstName: Float, val isAdult: Boolean)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader
      .loadClass("ru.dimsuz.vanilla.test.DraftValidatorBuilder")
      .toImmutableKmClass()
    assertThat(builderClass.functions.map { it.name })
      .containsAtLeast("firstName", "isAdult")
  }

  @Test
  fun builderHasErrorTypeParam() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val lastName: String)
        data class Validated(val firstName: Int, val lastName: String)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    assertThat(builderClass.typeParameters.map { it.name })
      .containsExactly("E")
  }

  @Test
  fun generatesCorrectPropertyRuleSignature() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int)
        data class Validated(val firstName: Float)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()

    val ruleFunction = validatorClass.functions.first { it.name == "firstName" }
    assertThat(ruleFunction.valueParameters).hasSize(1)
    val parameter = ruleFunction.valueParameters.first()
    assertThat(parameter.name).isEqualTo("validator")
    assertIsValidatorType(parameter.type)
    parameter.type.withFirstTypeArg { type ->
      assertThat(type?.classifier)
        .isEqualTo(KmClassifier.Class("kotlin/Int"))
    }
    parameter.type.withResultOkArg { type ->
      assertThat(type?.classifier).isEqualTo(KmClassifier.Class("kotlin/Float"))
    }

    assertThat(ruleFunction.returnType.classifier)
      .isEqualTo(KmClassifier.Class("DraftValidatorBuilder"))
  }

  @Test
  fun generatesCorrectGenericPropertyRuleSignature() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        object Name
        object NameComplex

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Map<Name, NameComplex?>?)
        data class Validated(val firstName: MutableMap<Int, NameComplex>)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()

    val ruleFunction = builderClass.functions.first { it.name == "firstName" }
    assertThat(ruleFunction.valueParameters).hasSize(1)
    val parameter = ruleFunction.valueParameters.first()

    assertIsValidatorType(parameter.type)

    parameter.type.withFirstTypeArg { type ->
      assertThat(type?.classifier).isEqualTo(KmClassifier.Class("kotlin/collections/Map"))
      assertThat(type?.isNullable).isTrue()
      assertThat(type?.arguments?.map { it.type?.classifier })
        .containsExactly(KmClassifier.Class("Name"), KmClassifier.Class("NameComplex"))
      assertThat(type?.arguments?.get(0)?.type?.isNullable).isFalse()
      assertThat(type?.arguments?.get(1)?.type?.isNullable).isTrue()
    }

    parameter.type.withResultOkArg { type ->
      assertThat(type?.classifier)
        .isEqualTo(KmClassifier.Class("kotlin/collections/MutableMap"))
      assertThat(type?.isNullable)
        .isFalse()
      assertThat(type?.arguments?.map { it.type?.classifier })
        .containsExactly(KmClassifier.Class("kotlin/Int"), KmClassifier.Class("NameComplex"))
      assertThat(type?.arguments?.get(0)?.type?.isNullable)
        .isFalse()
      assertThat(type?.arguments?.get(1)?.type?.isNullable)
        .isFalse()
    }
  }

  @Test
  fun builderHasBuildMethod() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int?)
        data class Validated(val firstName: String?)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    val buildFunction = builderClass.functions.find { it.name == "build" }
    assertThat(buildFunction).isNotNull()
    assertThat(buildFunction!!.valueParameters).isEmpty()
    assertIsValidatorType(buildFunction.returnType)
  }

  @Test
  fun mapsAnnotatedWithValidatedName() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        import ru.dimsuz.vanilla.annotation.ValidatedName

        @ValidatedAs(Validated::class)
        data class Draft(@ValidatedName("address") val addr: Int?)
        data class Validated(val address: String)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    val ruleFunction = builderClass.functions.find { it.name == "addr" }
    val parameter = ruleFunction?.valueParameters?.firstOrNull()
    assertThat(ruleFunction).isNotNull()
    parameter?.type?.withFirstTypeArg { type ->
      assertThat(type?.classifier).isEqualTo(KmClassifier.Class("kotlin/Int"))
      assertThat(type?.isNullable).isTrue()
    }
    parameter?.type?.withResultOkArg { type ->
      assertThat(type?.classifier).isEqualTo(KmClassifier.Class("kotlin/String"))
      assertThat(type?.isNullable).isFalse()
    }
  }

  @Test
  fun hasCompanionObjectWithValidateFunction() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        import ru.dimsuz.vanilla.annotation.ValidatedName

        @ValidatedAs(Validated::class)
        data class Draft(@ValidatedName("address") val addr: Int?)
        data class Validated(val address: String)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    val companionClass = builderClass.companionObject?.let {
      result.classLoader.loadClass("DraftValidatorBuilder$$it").toImmutableKmClass()
    }
    val createValidateFunction = companionClass?.functions?.find { it.name == "createValidateFunction" }
    assertThat(createValidateFunction).isNotNull()
    assertThat(createValidateFunction?.isPrivate).isTrue()
    assertThat(createValidateFunction?.returnType?.classifier)
      .isEqualTo(KmClassifier.Class("kotlin/Function1"))
    assertThat(createValidateFunction?.returnType?.arguments?.get(0)?.type?.classifier)
      .isEqualTo(KmClassifier.Class("Draft"))
    assertThat(createValidateFunction?.returnType?.arguments?.get(1)?.type?.arguments?.get(0)?.type?.classifier)
      .isEqualTo(KmClassifier.Class("Validated"))
  }

  @Test
  fun generatesBuildWith() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        import ru.dimsuz.vanilla.annotation.ValidatedName

        @ValidatedAs(Validated::class)
        data class Draft(val address: Int?)
        data class Validated(val address: Int, val cityId: String, val titleMapping: Map<Int, String>)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    val buildFunction = builderClass.functions.find { it.name == "buildWith" }
    assertThat(buildFunction).isNotNull()
    assertThat(buildFunction!!.valueParameters).hasSize(2)
    val firstParam = buildFunction.valueParameters[0]
    val secondParam = buildFunction.valueParameters[1]
    assertThat(firstParam.name).isEqualTo("cityId")
    assertThat(firstParam.type?.classifier).isEqualTo(KmClassifier.Class("kotlin/String"))
    assertThat(secondParam.name).isEqualTo("titleMapping")
    assertThat(secondParam.type?.classifier).isEqualTo(KmClassifier.Class("kotlin/collections/Map"))
  }

  @Test
  fun hasInternalModifierIfInternalSource() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        import ru.dimsuz.vanilla.annotation.ValidatedName

        @ValidatedAs(Validated::class)
        internal data class Draft(val address: Int?)
        data class Validated(val address: Int, val cityId: String, val titleMapping: Map<Int, String>)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    assertThat(builderClass.isInternal)
      .isTrue()
  }

  @Test
  fun hasInternalModifierIfInternalTarget() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        import ru.dimsuz.vanilla.annotation.ValidatedName

        @ValidatedAs(Validated::class)
        data class Draft(val address: Int?)
        internal data class Validated(val address: Int, val cityId: String, val titleMapping: Map<Int, String>)
        """.trimIndent()
      )
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidatorBuilder").toImmutableKmClass()
    assertThat(builderClass.isInternal)
      .isTrue()
  }

  // TODO gives an error if source and target have properties but no match can be found
  // TODO generates property validator signature which has target field type if it differs from source
  // TODO generates properly if source/target classes are inside another class
  // TODO generates properly if source/target classes are inside another interface
  // TODO gives an error if source or target is private,must be public/internal

  private fun assertIsValidatorType(type: ImmutableKmType?) {
    assertThat(type?.classifier).isEqualTo(KmClassifier.Class("ru/dimsuz/vanilla/Validator"))
  }

  private fun ImmutableKmType?.withResultOkArg(body: (ImmutableKmType?) -> Unit) {
    body(this?.arguments?.getOrNull(1)?.type)
  }

  private fun ImmutableKmType?.withFirstTypeArg(body: (ImmutableKmType?) -> Unit) {
    body(this?.arguments?.getOrNull(0)?.type)
  }
  private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
    return KotlinCompilation()
      .apply {
        workingDir = temporaryFolder.root
        annotationProcessors = listOf(VanillaProcessor())
        inheritClassPath = true
        sources = sourceFiles.asList()
        verbose = false
      }
  }

  private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
    return prepareCompilation(*sourceFiles).compile()
  }
}
