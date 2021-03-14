package ru.dimsuz.vanilla.processor.file

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

internal fun writeFile(
  processingEnv: ProcessingEnvironment,
  fileSpec: FileSpec
): Result<Unit, String> {
  val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
    ?: return Err("Can't find the target directory for generated Kotlin files.")
  fileSpec.writeTo(File(kaptKotlinGeneratedDir))
  return Ok(Unit)
}
