plugins {
  kotlin("jvm") version "1.3.72"
}

allprojects {
  repositories {
    mavenCentral()
    jcenter()
  }
}

val ktlint: Configuration by configurations.creating

dependencies {
  ktlint("com.pinterest:ktlint:0.37.2")
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to ".", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Check Kotlin code style."
  classpath = ktlint
  group = "verification"
  main = "com.pinterest.ktlint.Main"
  args = listOf("**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Fix Kotlin code style deviations."
  classpath = ktlint
  group = "verification"
  main = "com.pinterest.ktlint.Main"
  args = listOf("-F", "**/*.kt")
}
