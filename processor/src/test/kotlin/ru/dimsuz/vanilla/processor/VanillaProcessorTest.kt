package ru.dimsuz.vanilla.processor

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
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
    val result = compile(kotlin("source.kt",
      """
        import ru.dimsuz.vanilla.annotation.ValidatedAs
        
        @ValidatedAs(Validated::class)
        class Draft
        class Validated(val name: String)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages).contains(
      "error: failed to find matching properties. " +
        "Consider adding @ValidatedName annotation to properties of \"Draft\" class"
    )
  }

  @Test
  fun matchesDirectProperties() {
    val result = compile(kotlin("source.kt",
      """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val lastName: String)
        data class Validated(val firstName: Int, val lastName: String)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader.loadClass("DraftValidator\$Builder").toImmutableKmClass()
    assertThat(validatorClass.functions.map { it.name })
      .containsExactly("firstName", "lastName")
  }

  @Test
  fun matchesPropertiesDifferingInType() {
    val result = compile(kotlin("source.kt",
      """
        package ru.dimsuz.vanilla.test

        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val isAdult: String)
        data class Validated(val firstName: Float, val isAdult: Boolean)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader
      .loadClass("ru.dimsuz.vanilla.test.DraftValidator\$Builder")
      .toImmutableKmClass()
    assertThat(validatorClass.functions.map { it.name })
      .containsExactly("firstName", "isAdult")
  }

  @Test
  fun builderHasErrorTypeParam() {
    val result = compile(kotlin("source.kt",
      """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int, val lastName: String)
        data class Validated(val firstName: Int, val lastName: String)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val builderClass = result.classLoader.loadClass("DraftValidator\$Builder").toImmutableKmClass()
    assertThat(builderClass.typeParameters.map { it.name })
      .containsExactly("E")
  }

  @Test
  fun generatesCorrectPropertyRuleSignature() {
    val result = compile(kotlin("source.kt",
      """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Int)
        data class Validated(val firstName: Float)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader.loadClass("DraftValidator\$Builder").toImmutableKmClass()

    assertThat(validatorClass.functions.single().valueParameters).hasSize(1)
    val parameter = validatorClass.functions.first().valueParameters.first()
    assertThat(parameter.name).isEqualTo("validator")
    assertThat(parameter.type?.classifier)
      .isEqualTo(KmClassifier.Class("ru/dimsuz/vanilla/Validator"))
    assertThat(parameter.type?.arguments?.map { it.type?.classifier })
      .containsExactly(
        KmClassifier.Class("kotlin/Int"),
        KmClassifier.Class("kotlin/Float"),
        KmClassifier.TypeParameter(id = 0)
      )
  }

  @Test
  fun generatesCorrectGenericPropertyRuleSignature() {
    val result = compile(kotlin("source.kt",
      """
        import ru.dimsuz.vanilla.annotation.ValidatedAs

        object Name
        object NameComplex

        @ValidatedAs(Validated::class)
        data class Draft(val firstName: Map<Name, NameComplex?>?)
        data class Validated(val firstName: MutableMap<Int, NameComplex>)
      """.trimIndent()
    ))

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val validatorClass = result.classLoader.loadClass("DraftValidator\$Builder").toImmutableKmClass()

    assertThat(validatorClass.functions.single().valueParameters).hasSize(1)
    val parameter = validatorClass.functions.first().valueParameters.first()
    val firstTypeArgument = parameter.type?.arguments?.getOrNull(0)
    val secondTypeArgument = parameter.type?.arguments?.getOrNull(1)

    assertThat(firstTypeArgument?.type?.classifier)
      .isEqualTo(KmClassifier.Class("kotlin/collections/Map"))
    assertThat(firstTypeArgument?.type?.isNullable)
      .isTrue()
    assertThat(firstTypeArgument?.type?.arguments?.map { it.type?.classifier })
      .containsExactly(KmClassifier.Class("Name"), KmClassifier.Class("NameComplex"))
    assertThat(firstTypeArgument?.type?.arguments?.get(0)?.type?.isNullable)
      .isFalse()
    assertThat(firstTypeArgument?.type?.arguments?.get(1)?.type?.isNullable)
      .isTrue()

    assertThat(secondTypeArgument?.type?.classifier)
      .isEqualTo(KmClassifier.Class("kotlin/collections/MutableMap"))
    assertThat(secondTypeArgument?.type?.isNullable)
      .isFalse()
    assertThat(secondTypeArgument?.type?.arguments?.map { it.type?.classifier })
      .containsExactly(KmClassifier.Class("kotlin/Int"), KmClassifier.Class("NameComplex"))
    assertThat(secondTypeArgument?.type?.arguments?.get(0)?.type?.isNullable)
      .isFalse()
    assertThat(secondTypeArgument?.type?.arguments?.get(1)?.type?.isNullable)
      .isFalse()
  }

  // TODO works ok if target has more properties than source
  // TODO gives an error if source and target have properties but no match can be found
  // TODO generates property validator signature which has target field type if it differs from source
  // TODO generates properly if source/target classes are inside another class
  // TODO generates properly if source/target classes are inside another interface

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
