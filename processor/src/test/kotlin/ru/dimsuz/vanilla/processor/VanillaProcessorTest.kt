package ru.dimsuz.vanilla.processor

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

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

    val validatorClass = result.classLoader.loadClass("DraftValidator").toImmutableKmClass()
    assertThat(validatorClass.functions.map { it.name })
      .containsExactly("firstName", "lastName")
  }

  // TODO matches properties with same name but different type
  // TODO generates property validator signature which has target field type if it differs from source

  private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
    return KotlinCompilation()
      .apply {
        workingDir = temporaryFolder.root,
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
