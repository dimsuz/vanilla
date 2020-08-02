plugins {
  kotlin("jvm")
}

group = "ru.dimsuz.vanilla.processor"
version = "1.0-SNAPSHOT"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(project(":annotations"))
  implementation("com.squareup:kotlinpoet:1.6.0")
  implementation("com.squareup:kotlinpoet-metadata:1.6.0")

  testImplementation("com.google.truth:truth:1.0.1")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.9")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview"
  }
}
