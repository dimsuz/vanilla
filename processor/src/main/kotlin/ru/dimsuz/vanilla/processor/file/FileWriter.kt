package ru.dimsuz.vanilla.processor.file

import com.squareup.kotlinpoet.FileSpec
import ru.dimsuz.vanilla.processor.either.Either
import ru.dimsuz.vanilla.processor.either.Left
import ru.dimsuz.vanilla.processor.either.Right
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

internal fun writeFile(
  processingEnv: ProcessingEnvironment,
  fileSpec: FileSpec
): Either<String, Unit> {
  val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
    ?: return Left("Can't find the target directory for generated Kotlin files.")
  fileSpec.writeTo(File(kaptKotlinGeneratedDir))
  return Right(Unit)
}
